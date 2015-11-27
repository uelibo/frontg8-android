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

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import ch.frontg8.bl.Contact;
import ch.frontg8.lib.connection.ConnectionService;
import ch.frontg8.lib.crypto.KeystoreHandler;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.dbstore.ContactsDataSource;
import ch.frontg8.lib.helper.Tuple;
import ch.frontg8.lib.message.InvalidMessageException;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;
import ch.frontg8.lib.protobuf.Frontg8Client.Data;

public class DataService extends Service {

    private HashMap<UUID, Contact> contacts = new HashMap<>();
    private Context thisContext;
    private KeystoreHandler ksHandler;
    private ContactsDataSource datasource = new ContactsDataSource(this);

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
        thisContext = this;
        // Create ksHandler
        ksHandler = new KeystoreHandler(this);
        // Load contacts
        datasource.open();
        for (Contact contact : datasource.getAllContacts()) {
            contacts.put(contact.getContactId(), contact);
        }

        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    class ConIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConnectionService.MessageTypes.MSG_MSG:
                    byte[] msgBytes = (byte[]) msg.obj;
                    if (msgBytes != null) {
                        try {
                            List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(msgBytes));
                            for (Frontg8Client.Encrypted message : messages) {

                                //TODO what if could not decrypt
                                Tuple<UUID, Data> decryptedMSG = MessageHelper.getDecryptedContent(message, ksHandler);

                                //TODO write to DB

                                Contact contact = contacts.get(decryptedMSG._1);
                                contact.addMessage(new ch.frontg8.bl.Message(decryptedMSG._2));
                                contact.incrementUnreadMessageCounter();

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
                            }
                        } catch (RuntimeException re) {
                            Log.e("DS", "Could not construct msg!", re);
                        } catch (InvalidMessageException e) {
                            Log.e("DS", "Could not construct msg from decryptet content!", e);
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
            UUID uuid;
            Contact contact;
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
                    uuid = (UUID) msg.obj;
                    try {
                        msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_UPDATE, contacts.get(uuid)));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MessageTypes.MSG_UPDATE_CONTACT:
                    //TODO implement logic
                    //TODO check
                    contact = (Contact) msg.obj;
                    uuid = contact.getContactId();
                    String pubkey = contact.getPublicKeyString();
                    if (!contacts.get(uuid).getPublicKeyString().equals(pubkey)) {

                        try {
                            LibCrypto.negotiateSessionKeys(uuid, pubkey, ksHandler, thisContext);
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                            try {
                                msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_ERROR, e));
                            } catch (RemoteException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                    //TODO actualize, store and notify
                    break;
                case MessageTypes.MSG_CONTAINS_SK:
                    try {
                        msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_UPDATE, LibCrypto.containsSKSandSKC((UUID) msg.obj, ksHandler)));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case MessageTypes.MSG_REGISTER_FOR_MESSAGES:
                    uuid = (UUID) msg.obj;
                    contact = contacts.get(uuid);
                    try {
                        msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_UPDATE, contact.getMessages()));
                        mMessageClients.add(msg.replyTo);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    contact.resetUnreadMessageCounter();

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
                    ksHandler.changePassword((String) msg.obj, thisContext);
                    break;
                case MessageTypes.MSG_GEN_NEW_KEYS:
                    LibCrypto.generateNewKeys(ksHandler, thisContext);
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
        datasource.close();
        unbindService(mConnection);
        // TODO remove stuff
    }

    public static class MessageTypes {

        // Incomming

        //- Will add you to a List of active Clients for contact-updates
        public static final int MSG_GET_CONTACTS = 10;
        //- Unregister for Contacts
        public static final int MSG_UNREGISTER_CONTACTS = 11;
        //obj UUID - will respond with a contact
        public static final int MSG_GET_CONTACT_DETAILS = 12;
        //obj Contact.
        public static final int MSG_UPDATE_CONTACT = 13;
        //obj UUID - will respond with boolean
        public static final int MSG_CONTAINS_SK = 14;
        //obj UUID - will respond with all Messages for this UUID,
        // it will set the unread-Counter in the Contact to 0
        // and add you to the list of active clients for message-updates
        public static final int MSG_REGISTER_FOR_MESSAGES = 15;
        // - Will remove you from the list of active clients for message-updates
        public static final int MSG_UNREGISTER_FOR_MESSAGES = 16;
        //obj Tuple<UUID, new Frontg8.Data>
        public static final int MSG_SEND_MSG = 17;
        // - Will respond with a Base64 String
        public static final int MSG_GET_KEY = 18;
        //obj String (new Password)
        public static final int MSG_CHANGE_PW = 19;
        // - Will change my key, generate new keys for all Contacts, and delete all Messages
        public static final int MSG_GEN_NEW_KEYS = 20;

        // Outgoing

        public static final int MSG_UPDATE = 30;

        public static final int MSG_ERROR = 31;

    }
}
