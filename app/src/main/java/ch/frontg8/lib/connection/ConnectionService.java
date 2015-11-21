package ch.frontg8.lib.connection;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.ArrayList;

import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.crypto.LibSSLContext;

public class ConnectionService extends Service {

    public ConnectionService() {
    }

    private Logger logger = new Logger();
    private TcpClient mTcpClient;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    ArrayList<Messenger> mClients = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        new ConnectTask(this).execute();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }


    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_GET_ALL = 3;
    public static final int MSG_MSG = 4;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_MSG:
                    mTcpClient.sendMessage((byte[]) msg.obj);
                    break;
                case MSG_GET_ALL:
                    byte[] hash = (byte[]) msg.obj;
                    //TODO: handle
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTcpClient.stopClient();
        mTcpClient = null;
    }

    public class ConnectTask extends AsyncTask<byte[], byte[], TcpClient> {
        private Context context;

        public ConnectTask(Context context) {
            this.context = context;
        }

        @Override
        protected TcpClient doInBackground(byte[]... message) {
            mTcpClient = new TcpClient(new TcpClient.OnMessageReceived() {
                @Override
                public void messageReceived(byte[] message) {
                    publishProgress(message);
                }
            }, LibConfig.getServerName(context), LibConfig.getServerPort(context), logger, LibSSLContext.getSSLContext("root", context));
            mTcpClient.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(byte[]... values) {
            super.onProgressUpdate(values);

            for (int i = mClients.size() - 1; i >= 0; i--) {
                try {
                    mClients.get(i).send(Message.obtain(null, MSG_MSG, values));
                } catch (RemoteException e) {
                    mClients.remove(i);
                }
            }
        }
    }
}

