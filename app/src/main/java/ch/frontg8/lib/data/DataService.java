package ch.frontg8.lib.data;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import ch.frontg8.bl.Contact;
import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.connection.ConnectionService;
import ch.frontg8.lib.crypto.KeystoreHandler;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.dbstore.ContactsDataSource;
import ch.frontg8.lib.message.InvalidMessageException;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;

public class DataService extends Service {

    HashMap<UUID, Contact> contacts = new HashMap<>();
    Context thisContext;
    KeystoreHandler ksHandler;
    ContactsDataSource dataSource = new ContactsDataSource(this);

    protected Messenger mConMessenger;
    protected Messenger mDataMessenger;
    protected HashSet<Messenger> mContactClients = new HashSet<>();
    protected HashSet<Messenger> mMessageClients = new HashSet<>();

    Messenger mConService;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mConService = new Messenger(binder);
//            Toast.makeText(DataService.this, R.string.messageConnected, Toast.LENGTH_SHORT).show();

            try {
                Message msg1 = Message.obtain(null, ConnectionService.MessageTypes.MSG_REGISTER_CLIENT);
                msg1.replyTo = mConMessenger;
                mConService.send(msg1);

                //TODO: check why this contact is invalid
                Log.d("DS", "Requesting messages with Hash: " + new String (LibConfig.getLastMessageHash(thisContext)));
                byte[] requestMSG = MessageHelper.buildMessageRequestMessage(LibConfig.getLastMessageHash(thisContext));
                Message msg2 = Message.obtain(null, ConnectionService.MessageTypes.MSG_MSG, requestMSG);
                msg1.replyTo = mConMessenger;
                mConService.send(msg2);
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

        mConMessenger = new Messenger(new ConIncomingHandler(this));
        mDataMessenger = new Messenger(new DataIncomingHandler(this));
        thisContext = this;

        // Create ksHandler
        ksHandler = new KeystoreHandler(this);

        // Load contacts
        dataSource.open();
        for (Contact contact : dataSource.getAllContacts()) {
            UUID uuid = contact.getContactId();
            contacts.put(uuid, contact);

            new Decrypting().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uuid);
        }


        // Start ConnectionService
        Intent mIntent = new Intent(this, ConnectionService.class);
        bindService(mIntent, mConnection, Context.BIND_AUTO_CREATE);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mDataMessenger.getBinder();
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onDestroy() {
        Log.d("DS", "Destroy");
        super.onDestroy();
        dataSource.close();
        unbindService(mConnection);
    }

    private class Decrypting extends AsyncTask<UUID, UUID, Boolean> {

        @Override
        protected Boolean doInBackground(UUID... uuids) {
            Log.d("DS","Loading messages for contacts now!");
            for (Frontg8Client.Encrypted enc : dataSource.getEncryptedMessagesByUUID(uuids[0])) {
                try {
                    ch.frontg8.bl.Message data = new ch.frontg8.bl.Message(MessageHelper.getDataMessage(LibCrypto.decryptMSG(enc.getEncryptedData().toByteArray(), uuids[0], ksHandler)._2));
                    contacts.get(uuids[0]).addMessage(data);
//                    Iterator<Messenger> iterator = mMessageClients.iterator();
//                    while (iterator.hasNext()) {
//                        try {
//                            iterator.next().send(Message.obtain(null, MessageTypes.MSG_UPDATE, data));
//                        } catch (RemoteException e) {
//                            iterator.remove();
//                        }
//                    }
                } catch (InvalidMessageException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
    }
}
