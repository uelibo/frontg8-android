package ch.frontg8.lib.connection;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.List;

import javax.net.ssl.SSLContext;

import ch.frontg8.lib.message.InvalidMessageException;
import ch.frontg8.lib.message.MessageHelper;
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
    private BufferedOutputStream bufferedOutput;

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
        if (bufferedOutput != null) {
            Frontg8Client.Data data = MessageHelper.buildDataMessage(message, "0", 0);
            byte[] encryptedMessage = MessageHelper.buildEncryptedMessage(data);
            Log.e("TCP Client", MessageHelper.byteArrayAsHexString(encryptedMessage));
            try {
                bufferedOutput.write(encryptedMessage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bufferedOutput.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Close the connection and release the members
     */
    public void stopClient() {

        // send mesage that we are closing the connection
        sendMessage(Constants.CLOSED_CONNECTION);

        mRun = false;

        if (bufferedOutput != null) {
            try {
                bufferedOutput.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                bufferedOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        mMessageListener = null;
        bufferedOutput = null;
        mServerMessage = null;
    }

    public void run() {

        mRun = true;

        try {
            Log.e("TCP Client", "C: Connecting...");
            tlsClient.connect();

            try {

                //sends the message to the server
                bufferedOutput = new BufferedOutputStream(tlsClient.getOutputStream());

                // send login name
                sendMessage("Client connected");

                //in this while the client listens for the messages sent by the server
                while (mRun) {

                    List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(tlsClient));
                    for (Frontg8Client.Encrypted message: messages) {
                        String text = null;
                        try {
                            text = MessageHelper.getDataMessage(message.getEncryptedData()).getMessageData().toStringUtf8();
                        } catch (InvalidMessageException e) {
                            Log.e("TcpClient", "Invalid MSG");
                        }
                        if (mMessageListener != null) {
                            mMessageListener.messageReceived(text);
                        }
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