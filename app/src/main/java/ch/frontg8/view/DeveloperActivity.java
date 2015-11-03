package ch.frontg8.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.bl.Message;
import ch.frontg8.lib.connection.TlsTest;
import ch.frontg8.lib.dbstore.ContactsDataSource;

public class DeveloperActivity extends AppCompatActivity {
    private Activity thisActivity;
    private ContactsDataSource datasource = new ContactsDataSource(this);
    private TextView textViewLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);

        thisActivity = this;

        final TextView textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        Button ButtonClearDB = (Button) findViewById(R.id.buttonClearDB);
        Button ButtonLoadTestData = (Button) findViewById(R.id.buttonLoadTestData);
        Button ButtonTlsTest = (Button) findViewById(R.id.buttonTlsTest);

        datasource.open();

        ButtonClearDB.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                datasource.deleteAllContacts();
                textViewStatus.append("deleted.");
                Toast toast = Toast.makeText(thisActivity, "all data deleted", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        ButtonLoadTestData.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                Contact a = datasource.createContact(new Contact("Ueli", "Bosshard"));
                Contact b = datasource.createContact(new Contact("Tobias", "Stauber"));
                Contact c = datasource.createContact(new Contact("Flix"));
                Contact d = datasource.createContact(new Contact("Benny"));
                datasource.insertMessage(a, new Message("bla"));
                datasource.insertMessage(b, new Message("blb"));
                datasource.insertMessage(c, new Message("blc"));
                datasource.insertMessage(d, new Message("bld"));
                datasource.insertMessage(d, new Message("bld"));
                textViewStatus.append("data inserted.");
                Toast toast = Toast.makeText(thisActivity, "demo data inserted", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        ButtonTlsTest.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                textViewLog = (TextView) findViewById(R.id.textViewLog);
                textViewLog.setText("");
                textViewLog.setMovementMethod(new ScrollingMovementMethod());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TlsTest tlstest = new TlsTest(thisActivity);
                        tlstest.RunTlsTest();
                    }
                }).start();
            }
        });
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