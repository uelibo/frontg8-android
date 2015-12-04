package ch.frontg8.view;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.bl.Message;
import ch.frontg8.lib.connection.TlsTest;
import ch.frontg8.lib.crypto.KeystoreHandler;
import ch.frontg8.lib.crypto.LibCrypto;
import ch.frontg8.lib.data.DataService;
import ch.frontg8.lib.dbstore.ContactsDataSource;

public class DeveloperActivity extends AppCompatActivity {
    private Activity thisActivity;
    private ContactsDataSource datasource = new ContactsDataSource(this);
    private TextView textViewLog;
    // Messenger to get Contacts
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Messenger mService;

    // Connection to DataService
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            mService = new Messenger(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mService = null;
        }

    };

    // Handler for Messages from DataService
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer);

        thisActivity = this;

        Button buttonKeyGen = (Button) findViewById(R.id.buttonKeyGen);
        Button buttonClearDB = (Button) findViewById(R.id.buttonClearDB);
        Button buttonLoadTestData = (Button) findViewById(R.id.buttonLoadTestData);
        Button buttonTlsTest = (Button) findViewById(R.id.buttonTlsTest);
        Button buttonShowDB = (Button) findViewById(R.id.buttonShowDB);

        datasource.open();

        buttonKeyGen.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                try {
                    LibCrypto.generateNewKeys(new KeystoreHandler(thisActivity), thisActivity);
                    Toast toast = Toast.makeText(thisActivity, "Keys generated", Toast.LENGTH_SHORT);
                    toast.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        buttonClearDB.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                try {
                    mService.send(android.os.Message.obtain(null, DataService.MessageTypes.MSG_RESET));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                //datasource.deleteAllContacts();
                //datasource.deleteAllMessages();
                Toast toast = Toast.makeText(thisActivity, "all data deleted", Toast.LENGTH_SHORT);
                toast.show();
            }
        });

        buttonLoadTestData.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                String keyA = "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBf72KrjMTpU60csP6cVefgJocZpj+OOTF8sNueIPU8krHCycTozNoycoguqLkI6jU66pTXtnx/nxgXprVqg6bEyMBB5oCXoPNQSrb8GBkL5p764is9dn27q57cJ/Mw1zp1W/cNKJj2uWtuyFxXcwEhjVh8Vja47BaJCbFg7drjrzTxZM=";
                String keyB = "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBGp6vB5n7L2+6QdKY5ETFnY0QhIFIUqtVW0QaSDEUYJoLceBjKLyiLiQhiYo8tANXsBlrB+F/wQPARoYbaaFKX/cBUTYioTcVWpa4r2lupMyBwZ7x3v8cznfY4aSRWcQKIOtQxpHm7sDQWnViAVmKI4Xgw50ZE0ONxgIjNBioosO3K6I=";
                String keyC = "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBRg1Zd0pBm6ST8dULpAuwWxq+llhdguXfRkuf5stzPVYOgwgjO4j3qDe6z5tgK/iS9wMSAjqYuzvr6UkQAJtAXHABEoheuPgoKp2PI3Nozn8E+zsH0RQWhKHV+XiUp4tv6TAPuyTelHVsNJYOCjS5IkzCGROQqEVzMonlyRN2HNXvaIs=";
                ArrayList<Contact> contacts = new ArrayList<>();
                contacts.add(new Contact(UUID.fromString("11111111-1111-1111-1111-111111111111"), "The", "Other", keyA, 2, false));
                contacts.add(new Contact(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Tobias", "Stauber", keyB, 1, false));
                contacts.add(new Contact(UUID.fromString("33333333-3333-3333-3333-333333333333"), "Ueli", "Bosshard", keyC, 1, false));
                contacts.add(new Contact(UUID.fromString("44444444-4444-4444-4444-444444444444"), "Flix", "", "", 0, false));
                contacts.add(new Contact(UUID.fromString("55555555-5555-5555-5555-555555555555"), "Benny", "", "", 0, false));

                try {
                    for (Contact c: contacts) {
                        mService.send(android.os.Message.obtain(null, DataService.MessageTypes.MSG_UPDATE_CONTACT, c));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

//                datasource.insertMessage(a, new Message("bla"));
//                datasource.insertMessage(b, new Message("blb"));
//                datasource.insertMessage(c, new Message("blc"));
//                datasource.insertMessage(d, new Message("bld"));
//                datasource.insertMessage(d, new Message("bld"));

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

        buttonShowDB.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                textViewLog = (TextView) findViewById(R.id.textViewLog);
                textViewLog.setText("");
                datasource.open();
                ArrayList<Contact> contacts = datasource.getAllContacts();
                for (Contact c: contacts) {
                    textViewLog.append("Contact: "
                            + c.getName() + " "
                            + c.getSurname() + " "
                            + c.hasValidPubKey() + "\n"
                            );
                    for (Message m: c.getMessages()) {
                        textViewLog.append("- Message: " + m.getMessage() + "\n");
                    }
                }
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

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
        datasource.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Debug", "DeveloperActivity Resumed");

        // bind to DataService
        Intent intent = new Intent(this, DataService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

}