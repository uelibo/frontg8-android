package ch.frontg8.lib.connection;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLContext;

public class TcpClient {

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

    public boolean isConnected() {
        return tlsClient.isConnected();
    }

    public void sendMessage(byte[] message) {
        if (bufferedOutput != null) {
            try {
                bufferedOutput.write(message);
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
        // send message that we are closing the connection TODO: find, what t odo about this
//        sendMessage(Constants.CLOSED_CONNECTION);
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

    private static int getLengthFromHeader(byte[] header) {
        return ((header[0] < 0 ? 256 + header[0] : header[0]) << 8) + (header[1] < 0 ? 256 + header[1] : header[1]);
    }

    public void run() {
        mRun = true;
        try {
            Log.e("TCP Client", "C: Connecting...");
            tlsClient.connect();
            try {
                //sends the message to the server
                bufferedOutput = new BufferedOutputStream(tlsClient.getOutputStream());
                while (mRun) {
                    byte[] header;
                    byte[] data = new byte[0];
                    try {
                        header = tlsClient.getBytes(4);
                        int length = getLengthFromHeader(header);
                        data = tlsClient.getBytes(length);
                    } catch (NotConnectedException e) {
                        e.printStackTrace();
                    }
                    mMessageListener.messageReceived(data);
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
        void messageReceived(byte[] message);
    }
}