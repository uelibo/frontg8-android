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
import ch.frontg8.lib.TestDataHandler;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        final Contact contact;

        final TextView title = (TextView) findViewById(R.id.textViewTitle);
        final TextView name = (TextView) findViewById(R.id.editPersonName);
        final TextView surname = (TextView) findViewById(R.id.editPersonSurname);
        Button saveButton = (Button) findViewById(R.id.buttonSave);
        Button loadButton = (Button) findViewById(R.id.buttonLoadKey);

        Intent intent=this.getIntent();
        Bundle bundle=intent.getExtras();

        if (bundle == null) {
            contact = new Contact("");
            title.setText("New Contact");
        } else {
            UUID contactId=(UUID)bundle.getSerializable("contactid");
            contact= TestDataHandler.getContactById(contactId);
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
                }
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
