package ch.frontg8.lib.connection;

import android.app.Activity;
import android.content.SharedPreferences;

import java.util.List;
import java.util.UUID;
import javax.net.ssl.SSLContext;

import ch.frontg8.R;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.crypto.LibSSLContext;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.message.MessageType;
import ch.frontg8.lib.protobuf.Frontg8Client;

import static ch.frontg8.lib.crypto.LibCrypto.containsSKSandSKC;
import static ch.frontg8.lib.crypto.LibCrypto.decryptMSG;
import static ch.frontg8.lib.message.MessageHelper.buildEncryptedMessage;
import static ch.frontg8.lib.message.MessageHelper.getDecryptedContent;

public class TlsTest {
    Activity context;

    public TlsTest(Activity context) {
        this.context = context;
    }

    public void RunTlsTest() {
        Logger Log = new Logger(context, "DeveloperActivity");

        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preferences), context.MODE_PRIVATE);
        String servername = preferences.getString("edittext_preference_hostname", "server.frontg8.ch");
        int serverport = Integer.parseInt(preferences.getString("edittext_preference_port", "40001"));

        SSLContext sslContext = LibSSLContext.getSSLContext("root", context);
        TlsClient tlsClient = new TlsClient(servername, serverport, Log, sslContext);
        tlsClient.connect();

        String plainMessage = "frontg8 Test Message";

        // TODO: Encryption of data


        UUID uuid1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID uuid2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID uuid = null;
        if (containsSKSandSKC(uuid1,context)){
            uuid = uuid1;
        } else if (containsSKSandSKC(uuid2, context)){
            uuid = uuid2;
        }

//        byte[] encryptedMessage = MessageHelper.addMessageHeader(MessageHelper.buildEncryptedMessage(ByteString.copyFrom(encryptedContent)).toByteArray(), MessageType.Encrypted);
        byte[] encryptedMessage = buildEncryptedMessage(plainMessage.getBytes(), "0".getBytes(),0,uuid,context);

        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(encryptedMessage));
        Log.TRACE("---\n");

        // SEND Message
        tlsClient.sendBytes(encryptedMessage);

        // RECEIVE
        List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(tlsClient));
        Log.TRACE("Num of messages: " + messages.size());
        for (Frontg8Client.Encrypted message: messages) {
            // TODO: Decription of data


            String text = new String(getDecryptedContent(message, context));
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
            String text = MessageHelper.getDataMessage(message.getEncryptedData()).getMessageData().toStringUtf8();
            Log.TRACE(text);
        }

        tlsClient.close();
    }
}
