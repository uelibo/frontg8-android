package ch.frontg8.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.lib.crypto.KeystoreHandler;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.dbstore.ContactsDataSource;

public class ContactActivity extends AppCompatActivity {
    private Context thisContext;
    private ContactsDataSource datasource = new ContactsDataSource(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        thisContext = this;

        final Contact contact;
        final TextView title = (TextView) findViewById(R.id.textViewTitle);
        final TextView name = (TextView) findViewById(R.id.editPersonName);
        final TextView surname = (TextView) findViewById(R.id.editPersonSurname);
        final TextView publicKey = (TextView) findViewById(R.id.editPublickey);
        Button saveButton = (Button) findViewById(R.id.buttonSave);
        Button deleteButton = (Button) findViewById(R.id.buttonDelete);
        Button loadButton = (Button) findViewById(R.id.buttonLoadKey);

        Intent intent=this.getIntent();
        Bundle bundle=intent.getExtras();

        datasource.open();

        if (bundle == null) {
            title.setText("New Contact");
            contact = new Contact("");
            datasource.createContact(contact);
        } else {
            UUID contactId=(UUID)bundle.getSerializable("contactid");
            contact = datasource.getContactByUUID(contactId);
            title.append(" " + contact.getName() + " (" + contact.getContactId().toString() + ")");
        }

        name.setText(contact.getName());
        surname.setText(contact.getSurname());
        publicKey.setText(contact.getPublicKeyString());

        saveButton.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                Toast toast = Toast.makeText(thisContext, "Contact saved", Toast.LENGTH_SHORT);
                toast.show();
                contact.setName(name.getText().toString());
                contact.setSurname(surname.getText().toString());
                contact.setPublicKeyString(publicKey.getText().toString());
                datasource.updateContact(contact);
                // TODO: Handle invalid Publickey
                if (!contact.getPublicKeyString().isEmpty()) {
                    try {
                        LibCrypto.negotiateSessionKeys(contact.getContactId(), publicKey.getText().toString().getBytes(), new KeystoreHandler(thisContext), thisContext);
                    } catch (NoSuchProviderException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        deleteButton.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                datasource.deleteContact(contact);
                Toast toast = Toast.makeText(thisContext, "Contact deleted", Toast.LENGTH_SHORT);
                toast.show();
                //finish();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        datasource.close();
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
