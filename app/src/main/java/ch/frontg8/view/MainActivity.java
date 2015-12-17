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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.data.DataService;
import ch.frontg8.lib.data.MessageTypes;
import ch.frontg8.view.model.ContactAdapter;

public class MainActivity extends AppCompatActivity {
    // Messenger to get Contacts
    private final Messenger mMessenger = new Messenger(new IncomingHandler(this));
    private final Context thisActivity = this;
    private ContactAdapter dataAdapter = null;
    private Messenger mService;

    // Connection to DataService
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mService = new Messenger(binder);
            try {
                Message msg = Message.obtain(null, MessageTypes.MSG_GET_CONTACTS);
                msg.replyTo = mMessenger;
                mService.send(msg);
                mService.send(Message.obtain(null, MessageTypes.MSG_CONNECT));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            try {
                mService.send(Message.obtain(null, MessageTypes.MSG_UNREGISTER_CONTACTS));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mService = null;
        }
    };

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
                if (contact.hasValidPubKey()) {
                    openMessageActivityOfContact(contact);
                } else {
                    Toast.makeText(thisActivity, R.string.MainActivity_MessageNoPublicKeyForThisContact, Toast.LENGTH_SHORT).show();
                }
            }
        });

        TextView title = (TextView) findViewById(R.id.textViewTitle);
        title.append(" " + LibConfig.getUsername(this));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(thisActivity.getClass().getSimpleName(), "onResume");

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
            case R.id.action_importcerts:
                Intent intentImportCerts = new Intent(this, CertImportActivity.class);
                startActivity(intentImportCerts);
                return true;
            case R.id.action_developer:
                Intent intentDeveloper = new Intent(this, DeveloperActivity.class);
                startActivity(intentDeveloper);
                return true;
            case R.id.action_about:
                Intent intentAbout = new Intent(this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openMessageActivityOfContact(Contact contact) {
        Intent intent = new Intent(thisActivity, MessageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("contactId", contact.getContactId());
        bundle.putSerializable("contactName", contact.getName());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void myContactButtonHandler(View v) {
        View parentRow = (View) v.getParent();
        ListView parent = (ListView) parentRow.getParent();
        final int position = parent.getPositionForView(parentRow);
        Contact contact = (Contact) parent.getItemAtPosition(position);
        Intent intent = new Intent(thisActivity, ContactActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("contactId", contact.getContactId());
        intent.putExtras(bundle);
        startActivity(intent);
    }

    // Handler for Messages from DataService
    private static class IncomingHandler extends Handler {
        WeakReference<MainActivity> mainActivity;

        public IncomingHandler(MainActivity activity) {
            mainActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mainActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MessageTypes.MSG_BULK_UPDATE:
                        ArrayList<Contact> contacts =
                                new ArrayList<>(((HashMap<UUID, Contact>) msg.obj).values());
                        for (Contact c : contacts) {
                            activity.dataAdapter.add(c);
                            Log.d(activity.thisActivity.getClass().getSimpleName(), "got contact " + c.getName()
                                    + " " + c.getSurname()
                                    + " " + c.hasValidPubKey());
                        }
                        break;
                    case MessageTypes.MSG_UPDATE:
                        Contact contact = (Contact) msg.obj;
                        activity.dataAdapter.replace(contact);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }

}
