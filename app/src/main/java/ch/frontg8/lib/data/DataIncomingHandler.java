package ch.frontg8.lib.data;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import ch.frontg8.bl.Contact;
import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.connection.ConnectionService;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.helper.Tuple;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;

public class DataIncomingHandler extends Handler {
    private final WeakReference<DataService> mService;

    public DataIncomingHandler(DataService service) {
        mService = new WeakReference<>(service);
    }

    @Override
    public void handleMessage(Message msg) {
        DataService service = mService.get();
        if (service != null) {
            switch (msg.what) {
                case MessageTypes.MSG_GET_CONTACTS:
                    getContacts(msg, service);
                    break;
                case MessageTypes.MSG_UNREGISTER_CONTACTS:
                    service.mContactClients.remove(msg.replyTo);
                    break;
                case MessageTypes.MSG_GET_CONTACT_DETAILS:
                    getContactDetails(msg, service);
                    break;
                case MessageTypes.MSG_UPDATE_CONTACT:
                    updateContact(msg, service);
                    break;
                case MessageTypes.MSG_REMOVE_CONTACT:
                    removeContact(msg, service);
                    break;
                case MessageTypes.MSG_CONTAINS_SK:
                    respondToMSG(msg, Message.obtain(null, MessageTypes.MSG_UPDATE, LibCrypto.containsSKSandSKC((UUID) msg.obj, service.ksHandler)));
                    break;
                case MessageTypes.MSG_REGISTER_FOR_MESSAGES:
                    registerForMessages(msg, service);
                    break;
                case MessageTypes.MSG_UNREGISTER_FOR_MESSAGES:
                    UUID uuid1 = (UUID) msg.obj;
                    service.mMessageClients.remove(uuid1);
                    break;
                case MessageTypes.MSG_SEND_MSG:
                    sendMessage(msg, service);
                    break;
                case MessageTypes.MSG_REQUEST_MSG:
                    requestMessage(service);
                    break;
                case MessageTypes.MSG_GET_KEY:
                    respondToMSG(msg, Message.obtain(null, MessageTypes.MSG_UPDATE, LibCrypto.getMyPublicKeyBytes(service.ksHandler, service.thisContext)));
                    break;
                case MessageTypes.MSG_CHANGE_PW:
                    service.ksHandler.changePassword((String) msg.obj, service.thisContext);
                    break;
                case MessageTypes.MSG_GEN_NEW_KEYS:
                    LibCrypto.generateNewKeys(service.ksHandler, service.thisContext);
                    break;
                case MessageTypes.MSG_DEL_ALL_MSG:
                    deleteAllMessages(msg, service);
                    break;
                case MessageTypes.MSG_RESET:
                    resetEverything(service);
                    break;
                case MessageTypes.MSG_RESET_UNREAD:
                    UUID uuid2 = (UUID) msg.obj;
                    service.contacts.get(uuid2).resetUnreadMessageCounter();
                case MessageTypes.MSG_EXPORT_KEY:
                    exportKey(msg, service);
                    break;
                case MessageTypes.MSG_IMPORT_KEY:
                    importKey(msg, service);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void importKey(Message msg, DataService service) {
        String path2 = (String) msg.obj;
        try {
            service.ksHandler.importMyKey(path2, service.thisContext);
        } catch (IOException e) {
            try {
                msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_ERROR, e));
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void exportKey(Message msg, DataService service) {
        String path1 = (String) msg.obj;
        try {
            service.ksHandler.exportMyKey(path1, service.thisContext);
        } catch (IOException e) {
            try {
                msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_ERROR, e));
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void resetEverything(DataService service) {
        Log.d("DS", "ResetAll");
        service.dataSource.deleteAllContacts();
        service.dataSource.deleteAllMessages();
        service.contacts.clear();
        Log.d("DS", "service.contacts should be empty");
        service.ksHandler.resetOther();
        Toast toast = Toast.makeText(service.thisContext, "All data deleted", Toast.LENGTH_SHORT);
        toast.show();
    }

    private void deleteAllMessages(Message msg, DataService service) {
        UUID uuid = (UUID) msg.obj;
        Contact contact = service.contacts.get(uuid);
        contact.resetUnreadMessageCounter();
        contact.delAllMessages();
        service.dataSource.deleteAllMessagesOfUUID(uuid);
        service.dataSource.updateContact(contact);
        sendToAll(service, uuid);
    }

    private void sendToAll(DataService service, UUID uuid) {
        try {
            Messenger messenger = service.mMessageClients.get(uuid);
            if (messenger != null) {
                messenger.send(Message.obtain(null, MessageTypes.MSG_BULK_UPDATE, new ArrayList<ch.frontg8.bl.Message>()));
            }
        } catch (RemoteException e) {
            service.mMessageClients.remove(uuid);
        }
    }

    private void requestMessage(DataService service) {
        try {
            byte[] requestMSG = MessageHelper.buildMessageRequestMessage(LibConfig.getLastMessageHash(service.thisContext));
            service.mConService.send(Message.obtain(null, ConnectionService.MessageTypes.MSG_MSG, requestMSG));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void sendMessage(Message msg, DataService service) {
        try {
            Tuple<UUID, Frontg8Client.Data> content = (Tuple<UUID, Frontg8Client.Data>) msg.obj;
            byte[] encryptedMSG = MessageHelper.encryptAndPutInEncrypted(content._2, content._1, service.ksHandler);
            service.mConService.send(Message.obtain(null, ConnectionService.MessageTypes.MSG_MSG, encryptedMSG));
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
    }

    private void registerForMessages(Message msg, DataService service) {
        UUID uuid1 = (UUID) msg.obj;
        Contact contact1 = service.contacts.get(uuid1);
        try {
            msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_BULK_UPDATE, contact1.getMessages()));
            service.mMessageClients.put(uuid1, msg.replyTo);
        } catch (RemoteException e2) {
            e2.printStackTrace();
        }
        contact1.resetUnreadMessageCounter();
        service.dataSource.updateContact(contact1);
        sendToContactSubscribers(service, contact1);
    }

    private void sendToContactSubscribers(DataService service, Contact contact1) {
        Iterator<Messenger> iterator = service.mContactClients.iterator();
        while (iterator.hasNext()) {
            try {
                iterator.next().send(Message.obtain(null, MessageTypes.MSG_UPDATE, contact1));
            } catch (RemoteException e2) {
                iterator.remove();
            }
        }
    }

    private void respondToMSG(Message msg, Message obtain) {
        try {
            msg.replyTo.send(obtain);
        } catch (RemoteException e3) {
            e3.printStackTrace();
        }
    }

    private void removeContact(Message msg, DataService service) {
        Contact contact2 = (Contact) msg.obj;
        UUID uuid2 = contact2.getContactId();
        service.dataSource.deleteContact(contact2);
        service.contacts.remove(uuid2);
        sendToContactSubscribers(service, contact2);
    }

    private void updateContact(Message msg, DataService service) {
        Contact contact3 = (Contact) msg.obj;
        UUID uuid3 = contact3.getContactId();
        String publicKey = contact3.getPublicKeyString();
        if (publicKey != null && (service.contacts.get(uuid3) == null || !service.contacts.get(uuid3).getPublicKeyString().equals(publicKey))) {
            try {
                LibCrypto.negotiateSessionKeys(uuid3, publicKey, service.ksHandler, service.thisContext);
                Log.d("DS", "negotiated new Key");
                contact3.setValidPubkey(true);
            } catch (InvalidKeyException e3) {
                e3.printStackTrace();
                try {
                    msg.replyTo.send(Message.obtain(null, MessageTypes.MSG_ERROR, e3));
                } catch (RemoteException | NullPointerException e11) {
                    e11.printStackTrace();
                }
            }
        }
        if (service.contacts.containsKey(uuid3)) {
            service.dataSource.updateContact(contact3);
        } else {
            service.dataSource.createContact(contact3);
        }
        service.contacts.put(uuid3, contact3);
        sendToContactSubscribers(service, contact3);
    }

    private void getContactDetails(Message msg, DataService service) {
        UUID uuid4 = (UUID) msg.obj;
        respondToMSG(msg, Message.obtain(null, MessageTypes.MSG_UPDATE, service.contacts.get(uuid4)));
    }

    private void getContacts(Message msg, DataService service) {
        respondToMSG(msg, Message.obtain(null, MessageTypes.MSG_BULK_UPDATE, service.contacts));
        service.mContactClients.add(msg.replyTo);
    }

}

