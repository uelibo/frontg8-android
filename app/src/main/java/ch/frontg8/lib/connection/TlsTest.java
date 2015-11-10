package ch.frontg8.lib.connection;

import android.app.Activity;

import java.util.List;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.crypto.LibSSLContext;
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
        tlsClient.connect();

        String plainMessage = "frontg8 Test Message";
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

        // SEND Message & Encryption of data
        tlsClient.sendBytes(MessageHelper.buildFullEncryptedMessage(plainMessage.getBytes(), "0".getBytes(), 0, uuid, context));

        // RECEIVE
        List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(tlsClient));
        Log.TRACE("Num of messages: " + messages.size());
        for (Frontg8Client.Encrypted message: messages) {

            // Decription of data
            String text ="";
            try {
                text = new String(MessageHelper.getDecryptedContent(message, context));
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
        byte[] requestMessage = MessageHelper.addMessageHeader(MessageHelper.buildMessageRequestMessage("0").toByteArray(), MessageType.MessageRequest);
        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(requestMessage));
        tlsClient.sendBytes(requestMessage);

        // RECEIVE
        messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(tlsClient));
        Log.TRACE("Num of messages: " + messages.size());
        for (Frontg8Client.Encrypted message: messages) {

            // Decription of data
            String text = null;
            try {
                text = new String(MessageHelper.getDecryptedContent(message, context));
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
