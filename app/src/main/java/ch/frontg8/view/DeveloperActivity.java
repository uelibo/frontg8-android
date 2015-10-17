package ch.frontg8.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.bl.Message;
import ch.frontg8.lib.dbstore.ContactsDataSource;

public class DeveloperActivity extends AppCompatActivity {
    private ContactsDataSource datasource = new ContactsDataSource(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);

        Button ButtonClearDB = (Button) findViewById(R.id.buttonClearDB);
        Button ButtonLoadTestData = (Button) findViewById(R.id.buttonLoadTestData);

        datasource.open();

        ButtonClearDB.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                datasource.deleteAllContacts();
            }
        });

        ButtonLoadTestData.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                Contact a = datasource.createContact(new Contact("Ueli","Bosshard"));
                Contact b = datasource.createContact(new Contact("Tobias","Stauber"));
                Contact c = datasource.createContact(new Contact("Flix"));
                Contact d = datasource.createContact(new Contact("Benny"));
                datasource.insertMessage(a, new Message("bla"));
                datasource.insertMessage(b, new Message("blb"));
                datasource.insertMessage(c, new Message("blc"));
                datasource.insertMessage(d, new Message("bld"));
                datasource.insertMessage(d, new Message("bld"));
            }});

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_developer, menu);
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
