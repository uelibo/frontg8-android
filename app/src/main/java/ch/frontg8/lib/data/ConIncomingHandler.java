package ch.frontg8.lib.data;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import ch.frontg8.bl.Contact;
import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.connection.ConnectionService;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.helper.Tuple;
import ch.frontg8.lib.message.InvalidMessageException;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;

public class ConIncomingHandler extends Handler {
    private final WeakReference<DataService> mService;

    public ConIncomingHandler(DataService service) {
        mService = new WeakReference<>(service);
    }

    @Override
    public void handleMessage(Message msg) {
        DataService service = mService.get();
        if (service != null) {

            switch (msg.what) {
                case ConnectionService.MessageTypes.MSG_MSG:
                    Log.d("CIH", "Received message");
                    byte[] msgBytes = (byte[]) msg.obj;
                    try {
                        ArrayList<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(msgBytes));
                        Log.d("CIH", "Starting Decrypting");
                        Decrypting decrypting = new Decrypting();
                        Frontg8Client.Encrypted[] messagesArray = messages.toArray(new Frontg8Client.Encrypted[messages.size()]);
                        decrypting.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, messagesArray);
                    } catch (RuntimeException re) {
                        Log.e("CIH", "Could not construct msg!", re);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void notifyContactObservers(Contact contact, HashSet<Messenger> messengers) {
        Iterator<Messenger> iterator = messengers.iterator();
        while (iterator.hasNext()) {
            try {
                iterator.next().send(Message.obtain(null, MessageTypes.MSG_UPDATE, contact));
            } catch (RemoteException e) {
                iterator.remove();
            }
        }
    }

    private void notifyMessageObservers(ch.frontg8.bl.Message data, HashSet<Messenger> messengers) {
        Iterator<Messenger> iterator = messengers.iterator();
        while (iterator.hasNext()) {
            try {
                iterator.next().send(Message.obtain(null, MessageTypes.MSG_UPDATE, data));
            } catch (RemoteException e) {
                iterator.remove();
            }
        }
    }

    private class Decrypting extends AsyncTask<Frontg8Client.Encrypted, UUID, Boolean> {
        DataService service = mService.get();

        @Override
        protected Boolean doInBackground(final Frontg8Client.Encrypted... messages) {
//            android.os.Debug.waitForDebugger();
            Iterator<Frontg8Client.Encrypted> it = Arrays.asList(messages).iterator();
            Frontg8Client.Encrypted message;
            while (it.hasNext()) {
                Log.d("CIH", "Handling message");
                message = it.next();
                Tuple<UUID, Frontg8Client.Data> decryptedMSG;
                try {
                    decryptedMSG = MessageHelper.getDecryptedContent(message, service.ksHandler);

                    if (decryptedMSG._2 != null) {
                        Contact contact = service.contacts.get(decryptedMSG._1);
                        contact.addMessage(new ch.frontg8.bl.Message(decryptedMSG._2));
                        contact.incrementUnreadMessageCounter();
                        service.dataSource.insertMessage(contact, message);
                        // Send updates to interested parties
                        Log.d("CIH", "Notifying");
                        notifyContactObservers(contact, service.mContactClients);
                        notifyMessageObservers(new ch.frontg8.bl.Message(decryptedMSG._2), service.mMessageClients);
                    }
                } catch (InvalidMessageException ignore) {
                    Log.d("CIH", "Could not construct msg from decrypted content!");
                }
                if (message != null) {
                    LibConfig.setLastMessageHash(service.thisContext, LibCrypto.getSHA256Hash(message.toByteArray()));
                }
            }
            return true;
        }
    }
}
