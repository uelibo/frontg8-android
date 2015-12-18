package ch.frontg8.lib.data;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
import ch.frontg8.view.MessageActivity;

class ConIncomingHandler extends Handler {
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
                case ConnectionService.MessageTypes.MSG_CONNECTION_LOST:
                    service.sendNotificationDisconnected();
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

    private void notifyMessageObservers(ch.frontg8.bl.Message data, HashMap<UUID, Messenger> messengers, UUID uuid) {
        try {
            Messenger messenger = messengers.get(uuid);
            if (messenger != null) {
                messenger.send(Message.obtain(null, MessageTypes.MSG_UPDATE, data));
            }
        } catch (RemoteException ignore) {
            messengers.remove(uuid);
        }
    }

    private boolean isInForeground() {
        DataService service = mService.get();
        if (service != null) {
            String packetName = service.getPackageName();
            ActivityManager am = (ActivityManager) service.getSystemService(Context.ACTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                final List<ActivityManager.RunningAppProcessInfo> processInformation = am.getRunningAppProcesses();
                ActivityManager.RunningAppProcessInfo processInfo = processInformation.get(0);
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return (Arrays.asList(processInfo.pkgList).get(0)).contains(packetName);
                }
            } else {
                List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
                ComponentName componentInfo;
                componentInfo = taskInfo.get(0).topActivity;
                return componentInfo.getPackageName().contains(packetName);
            }
        }
        return false;
    }

    private class Decrypting extends AsyncTask<Frontg8Client.Encrypted, UUID, Boolean> {
        private final DataService service = mService.get();

        @Override
        protected Boolean doInBackground(final Frontg8Client.Encrypted... messages) {
            if (service != null) {
                Iterator<Frontg8Client.Encrypted> it = Arrays.asList(messages).iterator();
                Frontg8Client.Encrypted message;
                while (it.hasNext()) {
                    Log.d("CIH", "Handling message");
                    message = it.next();
                    Tuple<UUID, Frontg8Client.Data> decryptedMSG;
                    try {
                        decryptedMSG = MessageHelper.getDecryptedContent(message, service.ksHandler);
                        Log.v("CIH", "Decrypted");

                        if (decryptedMSG._2 != null) {
                            Contact contact = service.contacts.get(decryptedMSG._1);
                            contact.addMessage(new ch.frontg8.bl.Message(decryptedMSG._2));
                            contact.incrementUnreadMessageCounter();
                            service.dataSource.insertMessage(contact, message);

                            // Send updates to interested parties
                            Log.d("CIH", "Notifying");
                            notifyContactObservers(contact, service.mContactClients);
                            notifyMessageObservers(new ch.frontg8.bl.Message(decryptedMSG._2), service.mMessageClients, decryptedMSG._1);

                            if (!isInForeground()) {
                                Intent intent = new Intent(service, MessageActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("contactId", decryptedMSG._1);
                                bundle.putSerializable("contactName", contact.getName());
                                intent.putExtras(bundle);
                                PendingIntent pi = PendingIntent.getActivity(service, 0, intent, 0);

                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                                Notification notification = service.getDefaultNotificationBuilder()
                                        .setContentText(contact.getName() + ": " + decryptedMSG._2.getMessageData().toStringUtf8())
                                        .setContentIntent(pi)
                                        .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                                        .setLights(Color.YELLOW, 3000, 3000)
                                        .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).build();

                                service.NM.notify(DataService.NotificationIds.NOT_NEW_MESSAGE, notification);
                            }
                        }
                    } catch (InvalidMessageException ignore) {
                        Log.d("CIH", "Could not construct msg from decrypted content!");
                    }
                    if (message != null) {
                        byte[] hash = LibCrypto.getSHA256Hash(message.getEncryptedData().toByteArray());
                        final StringBuilder builder = new StringBuilder();
                        for (byte b : hash) {
                            builder.append(String.format("%02x", b));
                        }
                        String hashString = builder.toString();
                        LibConfig.setLastMessageHash(service.thisContext, hashString);
                    }
                }
                return true;
            }
            return false;
        }
    }
}
