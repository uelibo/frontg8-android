package ch.frontg8.lib.connection;

import android.app.Activity;

import com.google.protobuf.ByteString;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.crypto.KeyNotFoundException;
import ch.frontg8.lib.crypto.LibSSLContext;
import ch.frontg8.lib.message.InvalidMessageException;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.message.MessageType;
import ch.frontg8.lib.protobuf.Frontg8Client;

import static ch.frontg8.lib.crypto.LibCrypto.encryptMSG;
import static ch.frontg8.lib.message.MessageHelper.addMessageHeader;
import static ch.frontg8.lib.message.MessageHelper.buildDataMessage;
import static ch.frontg8.lib.message.MessageHelper.buildEncryptedMessage;
import static ch.frontg8.lib.message.MessageHelper.getDecryptedContent;

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

        // TODO: Encryption of data
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Log.TRACE(Arrays.toString(plainMessage.getBytes()));


        Frontg8Client.Data dataMessage = buildDataMessage(plainMessage,"0", 0);
        byte[] encryptedDataMessage = new byte[0];
        try {
            encryptedDataMessage = encryptMSG(uuid, dataMessage.toByteArray(),context);
        } catch (KeyNotFoundException e) {
            encryptedDataMessage = dataMessage.toByteArray();
            e.printStackTrace();
        }
        byte[] encryptedMessageSemi = addMessageHeader(buildEncryptedMessage(ByteString.copyFrom(encryptedDataMessage)), MessageType.Encrypted);

        byte[] encryptedMessage = MessageHelper.buildFullEncryptedMessage(plainMessage.getBytes(), "0".getBytes(), 0, uuid, context);



        Log.TRACE("---\n Is:     " + MessageHelper.byteArrayAsHexString(encryptedMessage));
        Log.TRACE("---\n Should: " + MessageHelper.byteArrayAsHexString(encryptedMessageSemi));
        Log.TRACE("---\n");

        // SEND Message
        tlsClient.sendBytes(encryptedMessage);

        // RECEIVE
        List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(tlsClient));
        Log.TRACE("Num of messages: " + messages.size());
        for (Frontg8Client.Encrypted message: messages) {

            // TODO: Decription of data
            String text = null;
            try {
                text = new String(getDecryptedContent(message, context));
            } catch (InvalidMessageException e) {
                Log.e("Invalid MSG");
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

            // TODO: Decription of data
            String text = null;
            try {
                text = new String(getDecryptedContent(message,context));
                Log.TRACE(text);
            } catch (InvalidMessageException e) {
                Log.e("Invalid MSG");
            }

        }

        tlsClient.close();
    }
}
