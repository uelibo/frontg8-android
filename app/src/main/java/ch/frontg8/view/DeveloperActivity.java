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

import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.bl.Message;
import ch.frontg8.lib.connection.TlsTest;
import ch.frontg8.lib.crypto.LibCrypto;
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

        Button buttonKeyGen = (Button) findViewById(R.id.buttonKeyGen);
        Button buttonClearDB = (Button) findViewById(R.id.buttonClearDB);
        Button buttonLoadTestData = (Button) findViewById(R.id.buttonLoadTestData);
        Button buttonTlsTest = (Button) findViewById(R.id.buttonTlsTest);

        datasource.open();

        buttonKeyGen.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                try {
                    LibCrypto.generateNewKeys(thisActivity);
                    Toast toast = Toast.makeText(thisActivity, "Keys generated", Toast.LENGTH_SHORT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        buttonClearDB.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                datasource.deleteAllContacts();
                datasource.deleteAllMessages();
                Toast toast = Toast.makeText(thisActivity, "all data deleted", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        buttonLoadTestData.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                Contact a = datasource.createContact(new Contact(UUID.fromString("11111111-1111-1111-1111-111111111111"), "The", "Other"));
                Contact b = datasource.createContact(new Contact("Tobias", "Stauber"));
                Contact c = datasource.createContact(new Contact("Ueli", "Bosshard"));
                Contact d = datasource.createContact(new Contact("Flix"));
                Contact e = datasource.createContact(new Contact("Benny"));
                datasource.insertMessage(a, new Message("bla"));
                datasource.insertMessage(b, new Message("blb"));
                datasource.insertMessage(c, new Message("blc"));
                datasource.insertMessage(d, new Message("bld"));
                datasource.insertMessage(d, new Message("bld"));
                Toast toast = Toast.makeText(thisActivity, "demo data inserted", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        buttonTlsTest.setOnClickListener(new AdapterView.OnClickListener() {
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