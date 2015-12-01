package ch.frontg8.view;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.bl.Message;
import ch.frontg8.lib.data.DataService;
import ch.frontg8.lib.helper.Tuple;
import ch.frontg8.lib.message.MessageHelper;
import ch.frontg8.lib.protobuf.Frontg8Client;
import ch.frontg8.view.model.MessageAdapter;

public class MessageActivity extends AppCompatActivity {
    private Context thisActivity = this;
    private MessageAdapter dataAdapter = null;
    private UUID contactId;
    private String contactName;
    private ListView listView;

    // Messenger to get Contacts
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mService;

    // Connection to DataService
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mService = new Messenger(binder);
//            Toast.makeText(MessageActivity.this, "Connected", Toast.LENGTH_SHORT).show();

            if (contactId != null) {
                try {
                    android.os.Message msg = android.os.Message.obtain(null, DataService.MessageTypes.MSG_REGISTER_FOR_MESSAGES, contactId);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }
    };

    // Handler for Messages from DataService
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DataService.MessageTypes.MSG_BULK_UPDATE:
                    ArrayList<Message> messages =  (ArrayList<Message>) msg.obj;
                    for (Message m: messages) {
                        Log.d("Debug", "got message " + m.getMessage());
                        if (m.isEncrypted()) {
                            m = MessageHelper.decryptMessage(m, thisActivity);
                        }
                        dataAdapter.add(m);
                    }
                    scrollMyListViewToBottom();
                    break;
                case DataService.MessageTypes.MSG_UPDATE:
                    Frontg8Client.Data message = (Frontg8Client.Data) msg.obj;
                    Log.d("Debug", "got message (update): " + message.getMessageData().toStringUtf8());
                    dataAdapter.add(new Message(message));
                    scrollMyListViewToBottom();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Intent intent=this.getIntent();
        Bundle bundle=intent.getExtras();
        contactId=(UUID)bundle.getSerializable("contactid");
        contactName=(String)bundle.getSerializable("contactname");

        TextView title = (TextView) findViewById(R.id.textViewTitle);
        if (contactName != null) {
            title.append(" of " + contactName);
        } else {
            // TODO: Handle invalid contact
        }

        dataAdapter = new MessageAdapter(this, new ArrayList<Message>());
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(dataAdapter);
        listView.setTextFilterEnabled(true);

        final EditText textSend = (EditText) findViewById(R.id.editTextSend);
        Button buttonSend = (Button) findViewById(R.id.buttonSend);

        //TODO: Disable, if ! contact.hasValidPubKey
        buttonSend.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                if (contactId != null) {
                    try {
                        // Send Message
                        String messageId = UUID.randomUUID().toString();
                        int timestamp = (int) System.currentTimeMillis() / 1000;
                        Frontg8Client.Data message = MessageHelper.buildDataMessage(textSend.getText().toString().getBytes(), messageId.getBytes(), timestamp);
                        Tuple<UUID, Frontg8Client.Data> content = new Tuple<>(contactId, message);
                        android.os.Message msg = android.os.Message.obtain(null, DataService.MessageTypes.MSG_SEND_MSG, content);
                        msg.replyTo = mMessenger;
                        mService.send(msg);
                        textSend.getText().clear();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                dataAdapter.getFilter().filter(newText.toLowerCase());
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                dataAdapter.getFilter().filter(query.toLowerCase());
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_clear_message_history:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Debug", "MessageActivity Resumed");

        // bind again to DataService:
        Intent intent = new Intent(this, DataService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            mService.send(android.os.Message.obtain(null, DataService.MessageTypes.MSG_UNREGISTER_FOR_MESSAGES));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        unbindService(mConnection);
    }

    private void scrollMyListViewToBottom() {
        listView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                listView.setSelection(dataAdapter.getCount() - 1);
            }
        });
    }

}
