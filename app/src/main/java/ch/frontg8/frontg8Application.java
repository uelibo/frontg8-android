package ch.frontg8;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

import ch.frontg8.lib.connection.ConnectionService;

/**
 * Created by tstauber on 11/23/15.
 */
public class frontg8Application extends Application {

//    private Messenger mService;
//    ServiceConnection mServiceConnection = new ServiceConnection() {
//
//        public void onServiceConnected(ComponentName className, IBinder binder) {
//            mService = new Messenger(binder);
//
//            try {
//                Message msg = Message.obtain(null, ConnectionService.MessageTypes.MSG_REGISTER_CLIENT);
//                msg.replyTo = mMessenger;
//                mService.send(msg);
//            } catch (RemoteException e) {
//                e.printStackTrace();
//            }
//        }
//
//        public void onServiceDisconnected(ComponentName className) {
//            mService = null;
//        }
//    };;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        Intent intent = new Intent(this, ConnectionService.class);
//        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

}