package ch.frontg8.lib.connection;

import android.app.Activity;

import java.util.List;

import javax.net.ssl.SSLContext;

import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.crypto.LibSSLContext;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;

public class TlsTest {
    private final Activity context;

    public TlsTest(Activity context) {
        this.context = context;
    }

    public void RunTlsTest() {
        Logger Log = new Logger(context, "DeveloperActivity");

        String serverName = LibConfig.getServerName(context);
        int serverPort = LibConfig.getServerPort(context);

        SSLContext sslContext = LibSSLContext.getSSLContext("root", context);
        TlsClient tlsClient = new TlsClient(serverName, serverPort, sslContext);
        try {
            tlsClient.connect();
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }

        // SEND Message Request Message
        byte[] requestMessage = MessageHelper.buildMessageRequestMessage("0".getBytes());
        Log.TRACE(MessageHelper.byteArrayAsHexString(requestMessage));
        try {
            tlsClient.sendBytes(requestMessage);
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }

        // RECEIVE
        List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(tlsClient));
        Log.TRACE("Num of messages: " + messages.size());

        tlsClient.close();
    }
}
