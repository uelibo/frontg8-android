package ch.frontg8.lib.data;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.lib.connection.ConnectionService;
import ch.frontg8.lib.crypto.KeystoreHandler;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.dbstore.ContactsDataSource;
import ch.frontg8.lib.message.InvalidMessageException;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;
import ch.frontg8.view.MainActivity;

public class DataService extends Service {

    final HashSet<Messenger> mContactClients = new HashSet<>();
    final HashMap<UUID, Messenger> mMessageClients = new HashMap<>();
    final HashMap<UUID, Contact> contacts = new HashMap<>();
    final ContactsDataSource dataSource = new ContactsDataSource(this);
    NotificationManager NM;
    Context thisContext;
    KeystoreHandler ksHandler;
    Messenger mConService;
    private Messenger mConMessenger;
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mConService = new Messenger(binder);

            try {
                Message msg1 = Message.obtain(null, ConnectionService.MessageTypes.MSG_REGISTER_CLIENT);
                msg1.replyTo = mConMessenger;
                mConService.send(msg1);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mConService = null;
        }
    };
    private Messenger mDataMessenger;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("DS", "Create");
        NM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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
    public void onDestroy() {
        Log.d("DS", "Destroy");
        super.onDestroy();
        dataSource.close();
        unbindService(mConnection);
    }

    void sendNotificationDisconnected() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //android.support.v7.app.NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.connecting64, getResources().getString(R.string.notificationButtonConnect), pi).build();
        Notification notification = getDefaultNotificationBuilder()
                .setContentIntent(pi)
                .setContentText(getResources().getString(R.string.notificationDisConnected))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setLights(Color.RED, 3000, 3000)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC).build();

        NM.notify(NotificationIds.NOT_CONNECTION_LOST, notification);
    }

    android.support.v4.app.NotificationCompat.Builder getDefaultNotificationBuilder() {
        return new NotificationCompat.Builder(thisContext)
                .setCategory(Notification.CATEGORY_MESSAGE) //TODO allow other notifications
                .setContentTitle(getResources().getString(R.string.app_name))
                .setSmallIcon(R.drawable.logo_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo_icon))
                .setAutoCancel(true);
    }

    private class Decrypting extends AsyncTask<UUID, UUID, Boolean> {

        @Override
        protected Boolean doInBackground(UUID... uuids) {
            Log.d("DS", "Loading messages for contacts now!");
            for (Frontg8Client.Encrypted enc : dataSource.getEncryptedMessagesByUUID(uuids[0])) {
                try {
                    ch.frontg8.bl.Message data = new ch.frontg8.bl.Message(MessageHelper.getDataMessage(LibCrypto.decryptMSG(enc.getEncryptedData().toByteArray(), uuids[0], ksHandler)._2));
                    contacts.get(uuids[0]).addMessage(data);
                    //TODO check concurrency
//                    Messenger messenger = mMessageClients.get(uuids[0]);
//                    try {
//                        if (messenger != null) {
//                            messenger.send(Message.obtain(null, MessageTypes.MSG_UPDATE, data));
//                        }
//                    } catch (RemoteException e) {
//                        mMessageClients.remove(uuids[0]);
//                    }
                } catch (InvalidMessageException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
    }

    public class NotificationIds {
        public static final int NOT_CONNECTION_LOST = 1;
        public static final int NOT_NEW_MESSAGE = 2;
    }
}
