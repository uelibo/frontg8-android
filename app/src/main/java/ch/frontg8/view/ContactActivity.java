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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.lib.crypto.KeystoreHandler;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.data.DataService;

public class ContactActivity extends AppCompatActivity {
    private Context thisContext;
    private UUID contactId;
    private Contact contact;
    private TextView title;
    private TextView name;
    private TextView surname;
    private TextView publicKey;

    // Messenger to get Contacts
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mService;

    // Connection to DataService
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mService = new Messenger(binder);
            Toast.makeText(ContactActivity.this, "Connected", Toast.LENGTH_SHORT).show();

            if (contactId != null) {
                try {
                    Message msg = Message.obtain(null, DataService.MessageTypes.MSG_GET_CONTACT_DETAILS, contactId);
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

    public ContactActivity() {
    }

    // Handler for Messages from DataService
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DataService.MessageTypes.MSG_UPDATE:
                    contact = (Contact) msg.obj;

                    Log.d("Debug", "got contact " + contact.getName()
                            + " " + contact.getSurname()
                            + " " + contact.hasValidPubKey());

                    title.append(" " + contact.getName() + " (" + contact.getContactId().toString() + ")");
                    name.setText(contact.getName());
                    surname.setText(contact.getSurname());
                    publicKey.setText(contact.getPublicKeyString());

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
        thisContext = this;

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
            contact = new Contact();
            title.setText("New Contact");
            name.setText(contact.getName());
            surname.setText(contact.getSurname());
            publicKey.setText(contact.getSurname());
        } else {
            contactId = (UUID) bundle.getSerializable("contactid");
        }

        saveButton.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                Toast toast = Toast.makeText(thisContext, "Contact saved", Toast.LENGTH_SHORT);
                toast.show();
                contact.setName(name.getText().toString());
                contact.setSurname(surname.getText().toString());
                contact.setPublicKeyString(publicKey.getText().toString());
                // TODO: UPDATE
                //datasource.updateContact(contact);
                // TODO: Handle invalid Publickey
                if (!contact.getPublicKeyString().isEmpty()) {
                    try {
                        LibCrypto.negotiateSessionKeys(contact.getContactId(), publicKey.getText().toString().getBytes(), new KeystoreHandler(thisContext), thisContext);
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        deleteButton.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                // TODO: DELETE
                //datasource.deleteContact(contact);
                Toast toast = Toast.makeText(thisContext, "Contact deleted", Toast.LENGTH_SHORT);
                toast.show();
                // TODO: How to go back to MainActivity?
                //finish();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Debug", "ContactActivity Resumed");
        //dataAdapter.replace(datasource.getAllContacts());

        // bind again to DataService:
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

}
