package ch.frontg8.lib.connection;

import android.app.Activity;

import com.google.protobuf.ByteString;

import java.io.IOException;

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

        byte[] packet;
        packet = new byte[] {
            (byte)0x00, 0x14, 0x01, 0x00, 0x0a, 0x12, 0x30, 0x31,
            (byte)0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
            (byte)0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48
            };

        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(packet));

        Frontg8Client.MessageRequest request = Frontg8Client.MessageRequest.newBuilder().setHash(ByteString.copyFromUtf8("0")).build();
        Frontg8Client.Data data = Frontg8Client.Data.newBuilder().setMessageData(ByteString.copyFromUtf8("Hallo")).setSessionId(ByteString.copyFromUtf8("0")).setTimestamp(0).build();
        Frontg8Client.Encrypted encr = Frontg8Client.Encrypted.newBuilder().setEncryptedData(ByteString.copyFromUtf8("Hallo")).build();

        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(request.toByteArray()));
        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(encr.toByteArray()));
        Log.TRACE("---\n" + MessageHelper.byteArrayAsHexString(data.toByteArray()));
        Log.TRACE("---\n");

        Log.TRACE("sending packet ");
        try {
            tlsClient.socket.getOutputStream().write(packet);
            Log.TRACE("sending packet succeeded");
        } catch (IOException e1) {
            Log.TRACE("socket.getOutputStream().write >> IOException");
        }
        Log.TRACE("recving packet");
        byte[] recv = new byte[100];
        try {
            int recvLen = tlsClient.socket.getInputStream().read(recv, 0, recv.length);
            Log.TRACE(MessageHelper.byteArrayAsHexString(recv));
            String str = new String(recv, 0, recvLen);
            Log.TRACE("recv packet = " + str);
        } catch (IOException e1) {
            Log.TRACE("socket.getInputStream().read >> IOException");
        }

        try {
            Log.TRACE("closing socket");
            tlsClient.socket.close();
            Log.TRACE("socket closed");
        } catch (IOException e) {
            Log.TRACE("socket.close >> IOException");
        }
    }
}
