package ch.frontg8.lib.data;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import ch.frontg8.bl.Contact;
import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.connection.ConnectionService;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.helper.Tuple;
import ch.frontg8.lib.message.InvalidMessageException;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;

/**
 * Created by tstauber on 12/4/15.
 */
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
                    byte[] msgBytes = (byte[]) msg.obj;
                    try {
                        List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(msgBytes));

                        Iterator<Frontg8Client.Encrypted> it = messages.iterator();
                        Frontg8Client.Encrypted message = null;
                        while (it.hasNext()) {
                            message = it.next();
                            Tuple<UUID, Frontg8Client.Data> decryptedMSG = MessageHelper.getDecryptedContent(message, service.ksHandler);
                            if (decryptedMSG._2 != null) {
                                Contact contact = service.contacts.get(decryptedMSG._1);
                                contact.addMessage(new ch.frontg8.bl.Message(decryptedMSG._2));
                                contact.incrementUnreadMessageCounter();
                                service.dataSource.insertMessage(contact, message);
                                // Send updates to interested parties
                                notifyContactObservers(contact, service.mContactClients);
                                notifyMessageObservers(new ch.frontg8.bl.Message(decryptedMSG._2), service.mMessageClients);
                            }
                        }
                        if (message != null) {
                            LibConfig.setLastMessageHash(service.thisContext, LibCrypto.getSHA256Hash(message.toByteArray()));
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

    private void notifyContactObservers(Contact contact, HashSet<Messenger> messengers) {
        Iterator<Messenger> iter = messengers.iterator();
        while (iter.hasNext()) {
            try {
                iter.next().send(Message.obtain(null, MessageTypes.MSG_UPDATE, contact));
            } catch (RemoteException e) {
                iter.remove();
            }
        }
    }

    private void notifyMessageObservers(ch.frontg8.bl.Message data, HashSet<Messenger> messengers) {
        Iterator<Messenger> iter = messengers.iterator();
        while (iter.hasNext()) {
            try {
                iter.next().send(Message.obtain(null, MessageTypes.MSG_UPDATE, data));
            } catch (RemoteException e) {
                iter.remove();
            }
        }
    }
}
