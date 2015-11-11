package ch.frontg8.lib.connection;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import javax.net.ssl.SSLContext;

import ch.frontg8.lib.crypto.LibSSLContext;

public class ConnectionService extends Service {
    public ConnectionService() {
    }

    public static final String SERVERIP = "server.frontg8.ch";
    public static final int SERVERPORT = 40001;
    private Logger logger = new Logger();
    private TlsClient mTlsClient;
    private final IBinder myBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("I am in on create");

        SSLContext sslContext = LibSSLContext.getSSLContext("root", this);
        mTlsClient = new TlsClient(SERVERIP, SERVERPORT, logger, sslContext);
        try {
            mTlsClient.connect();
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        System.out.println("I am in Ibinder onBind method");
        return myBinder;
    }

    public class LocalBinder extends Binder {
        public ConnectionService getService() {
            System.out.println("I am in Localbinder ");
            return ConnectionService.this;
        }
    }

    public void IsBoundable() {
        Toast.makeText(this, "I bind like butter", Toast.LENGTH_LONG).show();
    }

    public void sendBytes(byte[] packet) {
        try {
            if (!mTlsClient.isConnected()) {
                mTlsClient.connect();
            }
            mTlsClient.sendBytes(packet);
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }
    }

    public byte[] getBytes(int length) {
        byte[] data = new byte[0];
        try {
            if (!mTlsClient.isConnected()) {
                mTlsClient.connect();
            }
            data = mTlsClient.getBytes(length);
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTlsClient.close();
        mTlsClient = null;
    }
}

