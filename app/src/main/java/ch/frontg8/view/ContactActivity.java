package ch.frontg8.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.lib.dbstore.ContactsDataSource;

public class ContactActivity extends AppCompatActivity {
    private ContactsDataSource datasource = new ContactsDataSource(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        final Contact contact;
        final TextView title = (TextView) findViewById(R.id.textViewTitle);
        final TextView name = (TextView) findViewById(R.id.editPersonName);
        final TextView surname = (TextView) findViewById(R.id.editPersonSurname);
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

        saveButton.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                if (contact != null) {
                    title.setText("saved.");
                    contact.setName(name.getText().toString());
                    contact.setSurname(surname.getText().toString());
                    datasource.updateContact(contact);
                }
            }
        });

        deleteButton.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                if (contact != null) {
                    datasource.deleteContact(contact);
                }
                title.setText("deleted.");
                //finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
