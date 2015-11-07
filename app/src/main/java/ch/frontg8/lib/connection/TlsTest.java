package ch.frontg8.lib.connection;

import android.app.Activity;
import android.content.SharedPreferences;

import java.util.List;

import ch.frontg8.R;
import ch.frontg8.lib.crypto.LibCrypto;
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

        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preferences), context.MODE_PRIVATE);
        String servername = preferences.getString("edittext_preference_hostname", "server.frontg8.ch");
        int serverport = Integer.parseInt(preferences.getString("edittext_preference_port", "40001"));

        TlsClient tlsClient = new TlsClient(servername, serverport, Log, context);
        tlsClient.connect();

        Frontg8Client.Data data = MessageHelper.buildDataMessage("frontg8 Test Message", "0", 0);

        // TODO: Encryption of data
        try {
            LibCrypto.generateNewKeys(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] encryptedMessage = MessageHelper.addMessageHeader(MessageHelper.buildEncryptedMessage(data.toByteString()).toByteArray(), MessageType.Encrypted);

        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(encryptedMessage));
        Log.TRACE("---\n");

        // SEND Message
        tlsClient.sendBytes(encryptedMessage);

        // RECEIVE
        List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(tlsClient));
        Log.TRACE("Num of messages: " + messages.size());
        for (Frontg8Client.Encrypted message: messages) {
            // TODO: Decription of data
            String text = MessageHelper.getDataMessageFromByteArray(message.getEncryptedData()).getMessageData().toStringUtf8();
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
            String text = MessageHelper.getDataMessageFromByteArray(message.getEncryptedData()).getMessageData().toStringUtf8();
            Log.TRACE(text);
        }

        tlsClient.close();
    }
}
