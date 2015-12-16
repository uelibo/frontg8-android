package ch.frontg8.lib.connection;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.ref.WeakReference;

import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.crypto.LibSSLContext;
import ch.frontg8.lib.message.MessageHelper;

public class ConnectionService extends Service {

    private TcpClient mTcpClient = null;
    private Messenger mMessenger;
    private Messenger mClient = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("CService", "Create");
        mMessenger = new Messenger(new IncomingHandler(this));
        connect();
    }

    private void connect() {
        Log.d("CS", "Connecting");
        mTcpClient = buildTCPClient();
        mTcpClient.execute();
        requestMessages();
    }

    private void requestMessages() {
        byte[] hash = LibConfig.getLastMessageHash(this);
        Log.d("DS", "Requesting messages with Hash: " + new String(hash));
        if (mTcpClient != null) {
            mTcpClient.sendMessage(MessageHelper.buildMessageRequestMessage(hash));
        }
    }

    @NonNull
    private TcpClient buildTCPClient() {
        return new TcpClient(new TcpClient.OnMessageReceived() {
            @Override
            public void messageReceived(byte[] message) {
                try {
                    if (mClient != null)
                        mClient.send(Message.obtain(null, MessageTypes.MSG_MSG, message));
                } catch (RemoteException e) {
                    mClient = null;
                }
            }

            @Override
            public void connectionLost() {
                mTcpClient = null;
                sendConnectionLost();
            }
        }, LibConfig.getServerName(this), LibConfig.getServerPort(this), LibSSLContext.getSSLContext("root", this));
    }

    private void sendConnectionLost() {
        try {
            if (mClient != null)
                mClient.send(Message.obtain(null, MessageTypes.MSG_CONNECTION_LOST));
        } catch (RemoteException e) {
            mClient = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        Log.d("CService", "Destroy");
        super.onDestroy();
        mTcpClient.stopClient();
        mTcpClient = null;
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
                    service.mClient = msg.replyTo;
                    break;
                case MessageTypes.MSG_UNREGISTER_CLIENT:
                    service.mClient = null;
                    break;
                case MessageTypes.MSG_MSG:
                    if (service.mTcpClient != null) {
                        service.mTcpClient.sendMessage((byte[]) msg.obj);
                    } else {
                        service.connect();
                        if (service.mTcpClient != null) {
                            service.mTcpClient.sendMessage((byte[]) msg.obj);
                        } else {
                            service.sendConnectionLost();
                        }
                    }
                    break;
                case MessageTypes.MSG_CONNECT:
                    if (service.mTcpClient == null) {
                        service.connect();
                        Log.d("CS", "Tried to connect");
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public static class MessageTypes {
        public static final int MSG_REGISTER_CLIENT = 1;
        public static final int MSG_UNREGISTER_CLIENT = 2;
        public static final int MSG_MSG = 3;
        public static final int MSG_CONNECTION_LOST = 4;
        public static final int MSG_CONNECT = 5;
    }
}

