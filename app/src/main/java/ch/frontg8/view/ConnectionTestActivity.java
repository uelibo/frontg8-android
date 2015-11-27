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
import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.lib.connection.ConnectionService;
import ch.frontg8.lib.data.DataService;
import ch.frontg8.lib.helper.Tuple;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;
import ch.frontg8.view.model.ConnectionTestAdapter;


public class ConnectionTestActivity extends AppCompatActivity {

    private Context context;
    private ArrayList<String> arrayList;
    private ConnectionTestAdapter mAdapter;
    private UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private Messenger mService;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mService = new Messenger(binder);
            Toast.makeText(ConnectionTestActivity.this, "Connected", Toast.LENGTH_SHORT).show();

            try {
                Message msg = Message.obtain(null, DataService.MessageTypes.MSG_REGISTER_FOR_MESSAGES, uuid);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };


    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DataService.MessageTypes.MSG_BULK_UPDATE:
                    ArrayList<ch.frontg8.bl.Message> msgs = (ArrayList<ch.frontg8.bl.Message>) msg.obj;
                    for ( ch.frontg8.bl.Message m : msgs){
                        arrayList.add(m.getMessage());
                    }
                    break;
                case DataService.MessageTypes.MSG_UPDATE:
                    Frontg8Client.Data data = (Frontg8Client.Data) msg.obj;
                    arrayList.add(data.getMessageData().toStringUtf8());
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
                Frontg8Client.Data dataMSG = MessageHelper.buildDataMessage(message.getBytes(), "0".getBytes(), 0);
                try {
                    mService.send(Message.obtain(null, DataService.MessageTypes.MSG_SEND_MSG,
                            new Tuple<>(uuid, dataMSG)));
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
            mService.send(Message.obtain(null, DataService.MessageTypes.MSG_UNREGISTER_FOR_MESSAGES));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService(mConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, DataService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_connection_test, menu);
        return true;
    }
}