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

    public TcpClient(OnMessageReceived listener, String serverName, int serverPort, SSLContext sslContext) {
        mMessageListener = listener;
        this.tlsClient = new TlsClient(serverName, serverPort, sslContext);
    }

    public boolean isConnected() {
        return tlsClient.isConnected();
    }

    public void sendMessage(byte[] message) {
        Log.d("TCP Sending", " sending");
        synchronized (lock) {
            mMSG = message;
            lock.notifyAll();
        }
    }

    private static int getLengthFromHeader(byte[] header) {
        return ((header[0] < 0 ? 256 + header[0] : header[0]) << 8) + (header[1] < 0 ? 256 + header[1] : header[1]);
    }

    @Override
    protected TcpClient doInBackground(byte[]... params) {
        try {
            Log.d("TCP Client", "C: Connecting...");
            //TODO Handle not connected
            tlsClient.connect();
            try {
                Thread sendThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (lock) {
                            while (mRun) {
                                Log.v("TCP Client", "C: Sending");
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
                            }
                            Log.d("TCP", "Finished sendThread");
                        }
                    }
                });

                sendThread.start();

                Thread receiveThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isConnected()) {
                            Log.d("TCP Client", "Connected");
                        }
                        while (mRun) {
                            Log.v("TCP Client", "C: Receiving");
                            byte[] header;
                            byte[] data = new byte[0];
                            try {
                                header = tlsClient.getBytes(4);
                                int length = getLengthFromHeader(header);
                                data = tlsClient.getBytes(length);
                            } catch (NotConnectedException e) {
                                mRun = false;
                                //TODO message about connection broken and stop
                                e.printStackTrace();
                            }
                            if (data != null && data.length > 0) {
                                mMessageListener.messageReceived(data);
                                Log.v("TCP RUNLOOP", " Message received");
                            }
                        }
                        Log.d("TCP", "Finished receiveThread");
                    }
                });

                receiveThread.start();

                synchronized (lock) {
                    while (mRun) {
                        lock.wait();
                    }

                    try {
                        tlsClient.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
        Log.d("TCP Client", " Stop Client");
        mRun = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public interface OnMessageReceived {
        void messageReceived(byte[] message);
    }
}