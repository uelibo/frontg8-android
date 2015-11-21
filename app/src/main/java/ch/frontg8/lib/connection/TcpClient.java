package ch.frontg8.lib.connection;

import android.os.AsyncTask;
import android.util.Log;

import javax.net.ssl.SSLContext;

public class TcpClient extends AsyncTask<byte[], byte[], TcpClient> {

    private final OnMessageReceived mMessageListener;
    private TlsClient tlsClient;
    private final Object lock = new Object();
    private byte[] mMSG = null;
    private boolean mRun = true;

    public TcpClient(OnMessageReceived listener, String serverName, int serverPort, Logger logger, SSLContext sslContext) {
        mMessageListener = listener;
        this.tlsClient = new TlsClient(serverName, serverPort, logger, sslContext);
    }

    public boolean isConnected() {
        return tlsClient.isConnected();
    }

    public void sendMessage(byte[] message) {
        Log.e("TCP Sending", " sending");
        synchronized (lock) {
            mMSG = message;
            lock.notify();
        }
    }

    private static int getLengthFromHeader(byte[] header) {
        return ((header[0] < 0 ? 256 + header[0] : header[0]) << 8) + (header[1] < 0 ? 256 + header[1] : header[1]);
    }

    @Override
    protected TcpClient doInBackground(byte[]... params) {
        Log.e("TCP RUNLOOP", "startet");
        try {
            Log.e("TCP Client", "C: Connecting...");
            //TODO Handle not connected
            tlsClient.connect();
            try {

                Thread sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (lock) {
                            while (mRun) {
                                if (mMSG != null) {
                                    try {
                                        tlsClient.sendBytes(mMSG);
                                    } catch (NotConnectedException e) {
                                        e.printStackTrace();
                                    }
                                    mMSG = null;
                                }

                                try {
                                    lock.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                //TODO think about non active wait
                            }
                        }
                    }
                });

                sendThread.start();

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
                    Log.e("TCP RUNLOOP", " Message received");
                }


            } catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            } finally {
                tlsClient.close();
            }
        } catch (Exception e) {
            Log.e("TCP", "C: Error", e);
        }

        return null;
    }

    public void stopClient() {
        mRun = false;
    }

    public interface OnMessageReceived {
        void messageReceived(byte[] message);
    }
}