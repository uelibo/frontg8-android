package ch.frontg8.lib.connection;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Iterator;

import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.crypto.LibSSLContext;

public class ConnectionService extends Service {

    private TcpClient mTcpClient = null;
    private Messenger mMessenger;
    HashSet<Messenger> mClients = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("CService", "Create");
        mMessenger = new Messenger(new IncomingHandler(this));

        if (mTcpClient == null) {
            Log.d("CService", "mTcpClient was null");
        } else {
            Log.d("CService", "mTcpClient was " + mTcpClient.toString());
        }

        mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
            @Override
            public void messageReceived(byte[] message) {
                Iterator<Messenger> iter = mClients.iterator();
                while (iter.hasNext()) {
                    try {
                        iter.next().send(Message.obtain(null, MessageTypes.MSG_MSG, message));
                    } catch (RemoteException e) {
                        iter.remove();
                    }
                }
            }
        }, LibConfig.getServerName(this), LibConfig.getServerPort(this), LibSSLContext.getSSLContext("root", this));
        mTcpClient.execute();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }


    static class IncomingHandler extends Handler {
        private final WeakReference<ConnectionService> mService;

        public IncomingHandler(ConnectionService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            ConnectionService service = mService.get();
            switch (msg.what) {
                case MessageTypes.MSG_REGISTER_CLIENT:
                    service.mClients.add(msg.replyTo);
                    break;
                case MessageTypes.MSG_UNREGISTER_CLIENT:
                    service.mClients.remove(msg.replyTo);
                    break;
                case MessageTypes.MSG_MSG:
                    service.mTcpClient.sendMessage((byte[]) msg.obj);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.d("CService", "Destroy");
        super.onDestroy();
        mTcpClient.stopClient();
        mTcpClient = null;
        // TODO remove stuff
    }

    public static class MessageTypes {
        public static final int MSG_REGISTER_CLIENT = 1;
        public static final int MSG_UNREGISTER_CLIENT = 2;
        public static final int MSG_MSG = 3;
    }
}

