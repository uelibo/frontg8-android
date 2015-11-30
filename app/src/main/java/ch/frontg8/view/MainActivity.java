package ch.frontg8.view;

import android.app.SearchManager;
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
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.data.DataService;
import ch.frontg8.view.model.ContactAdapter;

public class MainActivity extends AppCompatActivity {
    private Context thisActivity = this;
    private ContactAdapter dataAdapter = null;

    // Messenger to get Contacts
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mService;

    // Connection to DataService
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mService = new Messenger(binder);
//            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();

            try {
                Message msg = Message.obtain(null, DataService.MessageTypes.MSG_GET_CONTACTS);
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

    // Handler for Messages from DataService
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DataService.MessageTypes.MSG_BULK_UPDATE:
                    ArrayList<Contact> contacts =
                            new ArrayList<Contact>(((HashMap<UUID, Contact>) msg.obj).values());
                    for (Contact c: contacts) {
                        dataAdapter.add(c);
                        Log.d("Debug", "got contact " + c.getName()
                        + " " + c.getSurname()
                        + " " + c.hasValidPubKey());
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataAdapter = new ContactAdapter(this, new ArrayList<Contact>());
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(dataAdapter);
        listView.setTextFilterEnabled(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = (Contact) parent.getItemAtPosition(position);
                Intent intent = new Intent(thisActivity, MessageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("contactid", contact.getContactId());
                bundle.putSerializable("contactname", contact.getName());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        TextView title = (TextView) findViewById(R.id.textViewTitle);
        title.append(" " + LibConfig.getUsername(this));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Debug", "MainActivity Resumed");

        // bind again to DataService:
        dataAdapter.clear();
        Intent intent = new Intent(this, DataService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

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
            case R.id.action_newcontact:
                Intent intentNewContact = new Intent(this, ContactActivity.class);
                startActivity(intentNewContact);
                return true;
            case R.id.action_settings:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.action_aboutme:
                Intent intentAboutMe = new Intent(this, AboutMeActivity.class);
                startActivity(intentAboutMe);
                return true;
            case R.id.action_connection_test:
                Intent intentConnectionTest = new Intent(this, ConnectionTestActivity.class);
                startActivity(intentConnectionTest);
                return true;
            case R.id.action_developer:
                Intent intentDeveloper = new Intent(this, DeveloperActivity.class);
                startActivity(intentDeveloper);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void myContactButtonHandler(View v)
    {
        View parentRow = (View) v.getParent();
        ListView parent = (ListView) parentRow.getParent();
        final int position = parent.getPositionForView(parentRow);
        Contact contact = (Contact) parent.getItemAtPosition(position);
        Intent intent = new Intent(thisActivity, ContactActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("contactid", contact.getContactId());
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
