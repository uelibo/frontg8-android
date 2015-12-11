package ch.frontg8.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.lib.data.DataService;
import ch.frontg8.lib.data.MessageTypes;

public class ContactActivity extends AppCompatActivity {
    private Context thisActivity;
    private UUID contactId;
    private Contact contact;
    private String scannedKey;
    private TextView title;
    private TextView name;
    private TextView surname;
    private TextView publicKey;
    static final String STATE_SCANNEDKEY = "scannedKey";
    // Messenger to get Contacts
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mService;

    // Connection to DataService
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mService = new Messenger(binder);

            if (contactId != null) {
                // only update if it's an existing contact
                requestContact();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }

        private void requestContact() {
            Log.d("ContactActivity", "Request Contact");
            try {
                Message msg = Message.obtain(null, MessageTypes.MSG_GET_CONTACT_DETAILS, contactId);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    };

    public ContactActivity() {
    }

    // Handler for Messages from DataService
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageTypes.MSG_UPDATE:
                    contact = (Contact) msg.obj;

                    Log.d("Debug", "got contact " + contact.getName()
                            + " " + contact.getSurname()
                            + " " + contact.hasValidPubKey());

                    if (scannedKey == null) {
                        title.setText(getString(R.string.titleEditContact) + " " + contact.getName()
                                + " " + contact.getSurname()
                                + " (" + contact.getContactId().toString() + ")");
                        name.setText(contact.getName());
                        surname.setText(contact.getSurname());
                        publicKey.setText(contact.getPublicKeyString());
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
        setContentView(R.layout.activity_contact);
        thisActivity = this;

        title = (TextView) findViewById(R.id.textViewTitle);
        name = (TextView) findViewById(R.id.editPersonName);
        surname = (TextView) findViewById(R.id.editPersonSurname);
        publicKey = (TextView) findViewById(R.id.editPublickey);
        Button saveButton = (Button) findViewById(R.id.buttonSave);
        Button deleteButton = (Button) findViewById(R.id.buttonDelete);
        Button loadButton = (Button) findViewById(R.id.buttonLoadKey);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle == null) {
            // Create new Contact
            contact = new Contact();
            title.setText(R.string.titleNewContact);
            name.setText(contact.getName());
            surname.setText(contact.getSurname());
            publicKey.setText(contact.getPublicKeyString());
        } else {
            contactId = (UUID) bundle.getSerializable("contactid");
        }

        saveButton.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                // Update Contact
                contact.setName(name.getText().toString());
                contact.setSurname(surname.getText().toString());
                contact.setPublicKeyString(publicKey.getText().toString());
                try {
                    android.os.Message msg = android.os.Message.obtain(null, MessageTypes.MSG_UPDATE_CONTACT, contact);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                    Toast.makeText(thisActivity, R.string.messageContactSaved, Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });

        deleteButton.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                // Delete Contact
                try {
                    android.os.Message msg = android.os.Message.obtain(null, MessageTypes.MSG_REMOVE_CONTACT, contact);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                    Toast.makeText(thisActivity, R.string.messageContactDeleted, Toast.LENGTH_SHORT).show();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                finish();
            }
        });

        loadButton.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                // scan qr-code with external app
                try {
                    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
                    startActivityForResult(intent, 0);
                } catch (Exception e) {
                    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                    startActivity(marketIntent);
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(thisActivity.getClass().getSimpleName(), "ContactActivity Resumed");
        //dataAdapter.replace(dataSource.getAllContacts());

        // bind to DataService
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
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                scannedKey = data.getStringExtra("SCAN_RESULT");
                publicKey.setText(scannedKey);
            }
            if (resultCode == RESULT_CANCELED) {
                //handle cancel
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        savedInstanceState.putString(STATE_SCANNEDKEY, scannedKey);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        scannedKey = savedInstanceState.getString(STATE_SCANNEDKEY);
    }


}
