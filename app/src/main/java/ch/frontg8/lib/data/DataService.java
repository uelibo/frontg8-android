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
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.lib.config.LibConfig;
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
//
//    private Notification Notify(){
//        Notification notification = new Notification.Builder(thisContext)
//                .setAutoCancel(true)
//                .setContentTitle("My notification")
//                .setContentText("Look, white in Lollipop, else color!")
//                .setSmallIcon(getNotificationIcon())
//                .build();
//
//        return notification;
//    }
//
//    private int getNotificationIcon() {
////        boolean whiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
//        return R.drawable.icon;
//    }

    private HashMap<UUID, Contact> contacts = new HashMap<>();
    private Context thisContext;
    private KeystoreHandler ksHandler;
    private ContactsDataSource dataSource = new ContactsDataSource(this);

    protected final Messenger mConMessenger = new Messenger(new ConIncomingHandler());
    protected final Messenger mDataMessenger = new Messenger(new DataIncomingHandler());
    protected HashSet<Messenger> mContactClients = new HashSet<>();
    protected HashSet<Messenger> mMessageClients = new HashSet<>();

    private Messenger mConService;

    public DataService() {
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mConService = new Messenger(binder);
            Toast.makeText(DataService.this, R.string.MessageConnected, Toast.LENGTH_SHORT).show();

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
        Log.d("DS", "Create");
        thisContext = this;
        // Create ksHandler
        ksHandler = new KeystoreHandler(this);
        // Load contacts
        dataSource.open();
        for (Contact contact : dataSource.getAllContacts()) {
            contacts.put(contact.getContactId(), contact);
        }

        Intent mIntent = new Intent(this, ConnectionService.class);
        bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mDataMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();


    }

    //TODO speedup by passing msghandler directly
    class ConIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConnectionService.MessageTypes.MSG_MSG:
                    byte[] msgBytes = (byte[]) msg.obj;
                    try {
                        List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(msgBytes));

                        for (Frontg8Client.Encrypted message : messages) {

                            Tuple<UUID, Data> decryptedMSG = MessageHelper.getDecryptedContent(message, ksHandler);
                            if (decryptedMSG._2 != null) {
                                Contact contact = contacts.get(decryptedMSG._1);
                                contact.addMessage(new ch.frontg8.bl.Message(decryptedMSG._2));
                                contact.incrementUnreadMessageCounter();
                                dataSource.insertMessage(contact, message);
                                // Send updates to interested parties
                                notifyContactObservers(contact);
                                notifyMessageObservers(decryptedMSG._2);
                            }
                        }
                    } catch (RuntimeException re) {
                        Log.e("DS", "Could not construct msg!", re);
                    } catch (InvalidMessageException e) {
                        Log.e("DS", "Could not construct msg from decryptet content!", e);
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
                        msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_BULK_UPDATE, contacts));
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
                    contact = (Contact) msg.obj;
                    uuid = contact.getContactId();
                    String pubkey = contact.getPublicKeyString();
                    if (contacts.get(uuid) == null || !contacts.get(uuid).getPublicKeyString().equals(pubkey)) {
                        try {
                            LibCrypto.negotiateSessionKeys(uuid, pubkey, ksHandler, thisContext);
                            Log.d("DS", "negotiated new Key");
                            contact.setValidPubkey(true);
                        } catch (InvalidKeyException e) {
                            e.printStackTrace();
                            try {
                                msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_ERROR, e));
                            } catch (RemoteException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                    if (contacts.containsKey(uuid)) {
                        dataSource.updateContact(contact);
                    } else {
                        dataSource.createContact(contact);
                    }
                    contacts.put(uuid, contact);
                    notifyContactObservers(contact);
                    break;
                case MessageTypes.MSG_REMOVE_CONTACT:
                    contact = (Contact) msg.obj;
                    uuid = contact.getContactId();
                    dataSource.deleteContact(contact);
                    contacts.remove(uuid);
                    notifyContactObservers(contact);
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
                        ArrayList<Data> al = new ArrayList<>();
                        for (ch.frontg8.bl.Message m : contact.getMessages()) {
                            Frontg8Client.Encrypted enc = m.getEncryptedMessage();
                            if (enc != null) {
                                Tuple<UUID, byte[]> tup = LibCrypto.decryptMSG(enc.getEncryptedData().toByteArray(), uuid, ksHandler);
                                if (tup._2 != null) {
                                    Data dat = MessageHelper.getDataMessage(tup._2);
                                    al.add(dat);
                                }
                            }
                        }
                        msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_BULK_UPDATE, al));
                        mMessageClients.add(msg.replyTo);
                    } catch (RemoteException | InvalidMessageException e) {
                        e.printStackTrace();
                    }
                    contact.resetUnreadMessageCounter();
                    dataSource.updateContact(contact);
                    notifyContactObservers(contact);
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
                case MessageTypes.MSG_REQUEST_MSG:
                    try {
                        byte[] requestMSG = MessageHelper.buildMessageRequestMessage(LibConfig.getLastMessageHash(thisContext));
                        mConService.send(Message.obtain(null, ConnectionService.MessageTypes.MSG_MSG, requestMSG));
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
                case MessageTypes.MSG_DEL_ALL_MSGS:
                    uuid = (UUID) msg.obj;
                    contact = contacts.get(uuid);
                    contact.resetUnreadMessageCounter();
                    contact.delAllMessages();
                    dataSource.deleteAllMessagesOfUUID(uuid);
                    dataSource.updateContact(contact);
                    notifyBulkMessageObservers(new ArrayList<Data>());
                    break;
                case MessageTypes.MSG_RESET:
                    Log.d("DS", "ResetAll");
                    dataSource.deleteAllContacts();
                    dataSource.deleteAllMessages();
                    contacts.clear();
                    Log.d("DS", "contacts should be empty");
                    ksHandler.resetOther();
                    Toast toast = Toast.makeText(thisContext, "All data deleted", Toast.LENGTH_SHORT);
                    toast.show();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


    private void notifyContactObservers(Contact contact) {
        Iterator<Messenger> iter = mContactClients.iterator();
        while (iter.hasNext()) {
            try {
                iter.next().send(Message.obtain(null, MessageTypes.MSG_UPDATE, contact));
            } catch (RemoteException e) {
                iter.remove();
            }
        }
    }

    private void notifyMessageObservers(Data data) {
        Iterator<Messenger> iter = mMessageClients.iterator();
        while (iter.hasNext()) {
            try {
                iter.next().send(Message.obtain(null, MessageTypes.MSG_UPDATE, data));
            } catch (RemoteException e) {
                iter.remove();
            }
        }
    }

    private void notifyBulkMessageObservers(ArrayList<Data> al) {
        Iterator<Messenger> iter = mMessageClients.iterator();
        while (iter.hasNext()) {
            try {
                iter.next().send(Message.obtain(null, MessageTypes.MSG_BULK_UPDATE, al));
            } catch (RemoteException e) {
                iter.remove();
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d("DS", "Destroy");
        super.onDestroy();
        dataSource.close();
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
        //obj UUID
        public static final int MSG_REMOVE_CONTACT = 14;
        //obj UUID - will respond with boolean
        public static final int MSG_CONTAINS_SK = 15;
        //obj UUID - will respond with all Messages for this UUID,
        // it will set the unread-Counter in the Contact to 0
        // and add you to the list of active clients for message-updates
        public static final int MSG_REGISTER_FOR_MESSAGES = 16;
        // - Will remove you from the list of active clients for message-updates
        public static final int MSG_UNREGISTER_FOR_MESSAGES = 17;
        //obj Tuple<UUID, new Frontg8.Data>
        public static final int MSG_SEND_MSG = 18;
        // - Will return array of Frontg8Client.Data
        public static final int MSG_REQUEST_MSG = 19;
        // - Will respond with a Base64 String
        public static final int MSG_GET_KEY = 20;
        //obj String (new Password)
        public static final int MSG_CHANGE_PW = 21;
        // - Will change my key, generate new keys for all Contacts, and delete all Messages
        public static final int MSG_GEN_NEW_KEYS = 22;
        //obj UUID - will delete all messages for this contact
        public static final int MSG_DEL_ALL_MSGS = 23;
        // - Will reset keystore, database and contacts.
        public static final int MSG_RESET = 24;

        // Outgoing

        public static final int MSG_UPDATE = 30;

        public static final int MSG_BULK_UPDATE = 31;

        public static final int MSG_ERROR = 32;

    }
}
