package ch.frontg8.lib.data;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import ch.frontg8.bl.Contact;
import ch.frontg8.lib.connection.ConnectionService;
import ch.frontg8.lib.crypto.KeystoreHandler;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.helper.Tuple;
import ch.frontg8.lib.message.InvalidMessageException;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;
import ch.frontg8.lib.protobuf.Frontg8Client.Data;

public class DataService extends Service {

    //TODO make observable
    private HashMap<UUID, Contact> contacts = new HashMap<>();
    private Context thisContext;
    private KeystoreHandler ksHandler;

    protected final Messenger mConMessenger = new Messenger(new ConIncomingHandler());
    protected final Messenger mDataMessenger = new Messenger(new DataIncomingHandler());
    protected ArrayList<Messenger> mContactClients = new ArrayList<>();
    protected ArrayList<Messenger> mMessageClients = new ArrayList<>();

    private Messenger mConService;

    public DataService() {
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mConService = new Messenger(binder);

            try {
                Message msg = Message.obtain(null, ConnectionService.MessageTypes.MSG_REGISTER_CLIENT);
                msg.replyTo = mConMessenger;
                mConService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mConService = null;
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 1; //TODO implement
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mDataMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("DService", "Create");
        thisContext=this;
        // Create ksHandler
        ksHandler = new KeystoreHandler(this);
        //TODO load contacts

        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    class ConIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConnectionService.MessageTypes.MSG_MSG:
                    List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(((byte[]) msg.obj)));
                    for (Frontg8Client.Encrypted message : messages) {
                        try {

                            //TODO what if could not decrypt
                            Tuple<UUID, Data> decryptedMSG = MessageHelper.getDecryptedContent(message, ksHandler);

                            //TODO write to DB

                            Contact contact = contacts.get(decryptedMSG._1);
                            contact.addMessage(new ch.frontg8.bl.Message(decryptedMSG._2));
                            //TODO increment contact.unreadCounter

                            // Send updates to interested partys
                            for (Messenger mMessenger : mContactClients) {
                                try {
                                    mMessenger.send(Message.obtain(null, MessageTypes.MSG_UPDATE, contact));
                                } catch (RemoteException e) {
                                    mMessageClients.remove(mMessenger);
                                }
                            }
                            for (Messenger mMessenger : mMessageClients) {
                                try {
                                    mMessenger.send(Message.obtain(null, MessageTypes.MSG_UPDATE, decryptedMSG._2));
                                } catch (RemoteException e) {
                                    mMessageClients.remove(mMessenger);
                                }
                            }


                        } catch (InvalidMessageException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    class DataIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageTypes.MSG_GET_CONTACTS:
                    try {
                        msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_UPDATE, contacts));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    mContactClients.add(msg.replyTo);
                    break;
                case MessageTypes.MSG_UNREGISTER_CONTACTS:
                    mContactClients.remove(msg.replyTo);
                    break;
                case MessageTypes.MSG_GET_CONTACT_DETAILS:
                    //TODO implement logic
                    break;
                case MessageTypes.MSG_UPDATE_CONTACT:
                    //TODO implement logic
                    break;
                case MessageTypes.MSG_UPDATE_CONTACT_KEY:
                    //TODO implement logic
                    break;
                case MessageTypes.MSG_REGISTER_FOR_MESSAGES:
                    //TODO implement logic
                    mMessageClients.add(msg.replyTo);
                    break;
                case MessageTypes.MSG_UNREGISTER_FOR_MESSAGES:
                    mMessageClients.remove(msg.replyTo);
                    break;
                case MessageTypes.MSG_SEND_MSG:
                    try {
                        Tuple<UUID, Data> content = (Tuple<UUID, Data>) msg.obj;
                        byte[] encryptedMSG = MessageHelper.encryptAndPutInEncrypted(content._2, content._1, ksHandler);
                        mConService.send(Message.obtain(null, ConnectionService.MessageTypes.MSG_MSG, encryptedMSG));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MessageTypes.MSG_GET_KEY:
                    try {
                        msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_UPDATE, LibCrypto.getMyPublicKeyBytes(ksHandler, thisContext)));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MessageTypes.MSG_CHANGE_PW:
                    //TODO implement logic
                    break;
                case MessageTypes.MSG_GEN_NEW_KEYS:
                    //TODO implement logic
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    @Override
    public void onDestroy() {
        Log.e("DService", "Destroy");
        super.onDestroy();
        // TODO remove stuff
    }

    public static class MessageTypes {

        // Incomming

        //- Will add you to a List of active Clients for contact-updates
        public static final int MSG_GET_CONTACTS = 10;
        //- Unregister for Contacts
        public static final int MSG_UNREGISTER_CONTACTS = 11;
        //Bundle with UUID - will respond with a contact
        public static final int MSG_GET_CONTACT_DETAILS = 12;
        //Bundle with Contact.
        public static final int MSG_UPDATE_CONTACT = 13;
        //Bundle with UUID and new pubkey as string
        public static final int MSG_UPDATE_CONTACT_KEY = 14;
        //Bundle with UUID - will respond with all Messages for this UUID,
        // it will set the unread-Counter in the Contact to 0
        // and add you to the list of active clients for message-updates
        public static final int MSG_REGISTER_FOR_MESSAGES = 15;
        // - Will remove you from the list of active clients for message-updates
        public static final int MSG_UNREGISTER_FOR_MESSAGES = 16;
        //Bundle with UUID and new Frontg8.Data
        public static final int MSG_SEND_MSG = 17;
        // - Will respond with a Base64 String
        public static final int MSG_GET_KEY = 18;
        //Bundle with a String (new Password)
        public static final int MSG_CHANGE_PW = 19;
        // - Will change my key, generate new keys for all Contacts, and delete all Messages
        public static final int MSG_GEN_NEW_KEYS = 20;

        // Outgoing

        public static final int MSG_UPDATE = 30;

    }
}
