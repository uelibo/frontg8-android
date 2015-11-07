package ch.frontg8.lib.connection;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import javax.net.ssl.SSLContext;

import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.message.MessageType;
import ch.frontg8.lib.protobuf.Frontg8Client;

public class TcpClient {
    public class Constants {
        public static final String CLOSED_CONNECTION = "client_closed_connection";
    }

    private TlsClient tlsClient;

    // message to send to the server
    private String mServerMessage;
    // sends message received notifications
    private OnMessageReceived mMessageListener = null;
    // while this is true, the server will continue running
    private boolean mRun = false;
    // used to send messages
    private OutputStream output;
    private PrintWriter mBufferOut;
    // used to read messages from the server
    private InputStream input;
    private BufferedReader mBufferIn;

    /**
     * Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TcpClient(OnMessageReceived listener, String serverName, int serverPort, Logger logger, SSLContext sslContext) {
        mMessageListener = listener;
        this.tlsClient = new TlsClient(serverName, serverPort, logger, sslContext);
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(String message) {
//        if (mBufferOut != null && !mBufferOut.checkError()) {
        if (output != null) {
            //mBufferOut.println(message);
            Frontg8Client.Data data = MessageHelper.buildDataMessage(message, "0", 0);
            byte[] encryptedMessage = MessageHelper.buildEncryptedMessage(data);
            Log.e("TCP Client", MessageHelper.byteArrayAsHexString(encryptedMessage));
            try {
                output.write(encryptedMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //mBufferOut.print(encryptedMessage);
            //mBufferOut.flush();
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        // send mesage that we are closing the connection
        sendMessage(Constants.CLOSED_CONNECTION+"frontg8");

        mRun = false;

        if (mBufferOut != null) {
            mBufferOut.flush();
            mBufferOut.close();
        }

        mMessageListener = null;
        mBufferIn = null;
        mBufferOut = null;
        mServerMessage = null;
    }

    public void run() {

        mRun = true;

        try {
            Log.e("TCP Client", "C: Connecting...");
            tlsClient.connect();

            try {

                //sends the message to the server
                output = tlsClient.getOutputStream();
                mBufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(output)), true);

                //receives the message which the server sends back
                input = tlsClient.getInputStream();
                mBufferIn = new BufferedReader(new InputStreamReader(input));
                // send login name
                sendMessage("frontg8 test");

                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(tlsClient));
                    for (Frontg8Client.Encrypted message: messages) {
                        String text = MessageHelper.getDataMessageFromByteArray(message.getEncryptedData()).getMessageData().toStringUtf8();
                        mMessageListener.messageReceived(text);
                    }

                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + mServerMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                tlsClient.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}