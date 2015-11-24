package ch.frontg8.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.lib.connection.ConnectionService;
import ch.frontg8.lib.message.InvalidMessageException;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;
import ch.frontg8.view.model.ConnectionTestAdapter;


public class ConnectionTestActivity extends AppCompatActivity {

    private Context context;
    private ArrayList<String> arrayList;
    private ConnectionTestAdapter mAdapter;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private Messenger mService;

    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder binder) {
            mService = new Messenger(binder);
            Toast.makeText(ConnectionTestActivity.this, "Connected", Toast.LENGTH_SHORT).show();

            try {
                Message msg = Message.obtain(null, ConnectionService.MessageTypes.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    //TODO: ist hier, weil ich nicht weiss wo sonst... in landscape mode do disable fullscreen editing




    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConnectionService.MessageTypes.MSG_MSG:
                    List<Frontg8Client.Encrypted> messages = MessageHelper.getEncryptedMessagesFromNotification(MessageHelper.getNotificationMessage(((byte[]) msg.obj)));
                    for (Frontg8Client.Encrypted message : messages) {
                        try {
                            arrayList.add(new String(MessageHelper.getDecryptedContent(message, context)));
                        } catch (InvalidMessageException e) {
                            e.printStackTrace();
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_connection_test);

        arrayList = new ArrayList<>();

        final EditText editText = (EditText) findViewById(R.id.editText);
        Button send = (Button) findViewById(R.id.send_button);

        //relate the listView from java to the one created in xml
        ListView mList = (ListView) findViewById(R.id.list);
        mAdapter = new ConnectionTestAdapter(this, arrayList);
        mList.setAdapter(mAdapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString();

                //add the text in the arrayList
                arrayList.add("c: " + message);
                //TODO let this be done from the DS later on
                byte[] dataMSG = MessageHelper.buildFullEncryptedMessage(message.getBytes(), "0".getBytes(), 0, UUID.fromString("11111111-1111-1111-1111-111111111111"), context);

                try {
                    mService.send(Message.obtain(null, ConnectionService.MessageTypes.MSG_MSG, dataMSG));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                //refresh the list
                mAdapter.notifyDataSetChanged();
                editText.setText("");
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mService.send(Message.obtain(null, ConnectionService.MessageTypes.MSG_UNREGISTER_CLIENT));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService(mConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_connection_test, menu);
        return true;
    }
}