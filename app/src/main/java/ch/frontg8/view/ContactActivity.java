package ch.frontg8.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.lib.TestDataHandler;

public class ContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        final Contact contact;

        TextView title = (TextView) findViewById(R.id.textViewTitle);
        TextView name = (TextView) findViewById(R.id.editPersonName);

        Intent intent=this.getIntent();
        Bundle bundle=intent.getExtras();

        if (bundle == null) {
            contact = new Contact("","");
            title.setText("New Contact");
        } else {
            String contactId=(String)bundle.getSerializable("contactid");
            contact= TestDataHandler.getContactById(contactId);
            title.append(" " + contact.getName() + " (" + contact.getContactId() + ")");
        }

        name.setText(contact.getName());

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
