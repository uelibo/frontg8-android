package ch.frontg8.lib.connection;

import android.app.Activity;

import java.util.List;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.crypto.KeystoreHandler;
import ch.frontg8.lib.crypto.LibSSLContext;
import ch.frontg8.lib.helper.Tuple;
import ch.frontg8.lib.message.InvalidMessageException;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.message.MessageType;
import ch.frontg8.lib.protobuf.Frontg8Client;

public class TlsTest {
    Activity context;

    public TlsTest(Activity context) {
        this.context = context;
    }

    public void RunTlsTest() {
        Logger Log = new Logger(context, "DeveloperActivity");

        String serverName = LibConfig.getServerName(context);
        int serverPort = LibConfig.getServerPort(context);

        SSLContext sslContext = LibSSLContext.getSSLContext("root", context);
        TlsClient tlsClient = new TlsClient(serverName, serverPort, Log, sslContext);
        try {
            tlsClient.connect();
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }

        String plainMessage = "frontg8 Test Message";
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

        // SEND Message & Encryption of data
        try {
            tlsClient.sendBytes(MessageHelper.putInDataEncryptAndPutInEncrypted(plainMessage.getBytes(), "0".getBytes(), 0, uuid, new KeystoreHandler(context)));
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }

        // RECEIVE
        List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(tlsClient));
        Log.TRACE("Num of messages: " + messages.size());
        for (Frontg8Client.Encrypted message: messages) {

            // Decription of data
            String text ="";
            try {
                Tuple<UUID, Frontg8Client.Data> mes = MessageHelper.getDecryptedContent(message, new KeystoreHandler(context));
                text = mes._1.toString() + mes._2.getMessageData().toStringUtf8();
            } catch (InvalidMessageException e) {
                Log.TRACE("WARNING: Could not decrypt message");

                try {
                    Frontg8Client.Data data = MessageHelper.getDataMessage(message.getEncryptedData().toByteArray());
                    text = data.getMessageData().toStringUtf8();
                } catch (InvalidMessageException f) {
                    f.printStackTrace();
                }
            }

            Log.TRACE(text);
        }

        // SEND Message Request Message
        byte[] requestMessage = MessageHelper.buildMessageRequestMessage("0".getBytes());
        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(requestMessage));
        try {
            tlsClient.sendBytes(requestMessage);
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }

        // RECEIVE
        messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(tlsClient));
        Log.TRACE("Num of messages: " + messages.size());
        for (Frontg8Client.Encrypted message: messages) {

            // Decription of data
            String text = null;
            try {
                Tuple<UUID, Frontg8Client.Data> mes = MessageHelper.getDecryptedContent(message, new KeystoreHandler(context));
                text = mes._1.toString() + mes._2.getMessageData().toStringUtf8();
            } catch (InvalidMessageException e) {
                Log.TRACE("WARNING: Could not decrypt message");
                try {
                    Frontg8Client.Data data = MessageHelper.getDataMessage(message.getEncryptedData().toByteArray());
                    text = data.getMessageData().toStringUtf8();
                } catch (InvalidMessageException f) {
                    f.printStackTrace();
                }
            }
            Log.TRACE(text);

        }

        tlsClient.close();
    }
}
