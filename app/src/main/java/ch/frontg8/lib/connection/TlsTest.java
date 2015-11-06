package ch.frontg8.lib.connection;

import android.app.Activity;

import com.google.protobuf.InvalidProtocolBufferException;

import ch.frontg8.bl.Message;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;

public class TlsTest {
    Activity context;

    public TlsTest(Activity context) {
        this.context = context;
    }

    public void RunTlsTest() {

        Logger Log = new Logger(context, "DeveloperActivity");
        TlsClient tlsClient = new TlsClient("redmine.frontg8.ch", 40001, Log, context);
        tlsClient.connect();

        Frontg8Client.Data data = MessageHelper.buildDataMessage("frontg8 Test Message", "0", 0);

        // TODO: Encryption of data
        try {
            LibCrypto.generateNewKeys(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Frontg8Client.Encrypted encr = MessageHelper.buildEncryptedMessage(data.toByteString());

        //Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(encr.toByteArray()));
        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(MessageHelper.addMessageHeader(encr.toByteArray())));
        Log.TRACE("---\n");

        // SEND
        tlsClient.sendBytes(MessageHelper.addMessageHeader(encr.toByteArray()));

        // RECEIVE
        Frontg8Client.Notification notification = tlsClient.getNotificationMessage();
        Frontg8Client.Encrypted encrypted;
        Frontg8Client.Data message = null;
        if (notification != null ) {
            try {
                Log.TRACE("\nBundleCount: " + notification.getBundleCount());
                encrypted = notification.getBundle(0);
                message = Frontg8Client.Data.parseFrom(encrypted.getEncryptedData());
                Log.TRACE("----------------\nMessage: " + message.getMessageData().toStringUtf8() +"\n");
            } catch (InvalidProtocolBufferException e) {
                Log.TRACE(e.getMessage());
                e.printStackTrace();
            }
        }

        // SEND
        byte[] requestMessage = MessageHelper.addMessageHeader(MessageHelper.buildMessageRequestMessage("0").toByteArray());
        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(requestMessage));
        tlsClient.sendBytes(requestMessage);

        // RECEIVE
        notification = tlsClient.getNotificationMessage();
        if (notification != null ) {
            try {
                Log.TRACE("\nBundleCount: " + notification.getBundleCount());
                encrypted = notification.getBundle(0);
                message = Frontg8Client.Data.parseFrom(encrypted.getEncryptedData());
                Log.TRACE("\nMessage: " + message.getMessageData().toStringUtf8());
            } catch (InvalidProtocolBufferException e) {
                Log.TRACE(e.getMessage());
                e.printStackTrace();
            }
        }
        tlsClient.close();
    }
}
