package ch.frontg8.lib.connection;

import android.app.Activity;

import com.google.protobuf.InvalidProtocolBufferException;

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

        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(encr.toByteArray()));
        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(MessageHelper.addMessageHeader(encr.toByteArray())));
        Log.TRACE("---\n");

        // SEND
        tlsClient.sendBytes(MessageHelper.addMessageHeader(encr.toByteArray()));

        byte[] recv1 = tlsClient.getBytes(4);
        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(recv1));

        int length = recv1[0] * 256 + recv1[1] ;
        //int proto = recv1[2];

        byte[] recv2 = tlsClient.getBytes(length);
        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(recv2));

        try {
            Frontg8Client.Notification notification = Frontg8Client.Notification.parseFrom(recv2);
            Frontg8Client.Encrypted encrypted = notification.getBundle(0);
            Frontg8Client.Data message= Frontg8Client.Data.parseFrom(encrypted.getEncryptedData());
            Log.TRACE("\nMessage: " + message.getMessageData().toStringUtf8());
            } catch (InvalidProtocolBufferException e) {
            Log.TRACE(e.getMessage());
            e.printStackTrace();
        }

        tlsClient.close();
    }
}
