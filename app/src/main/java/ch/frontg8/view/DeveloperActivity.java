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
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.UUID;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.lib.config.LibConfig;
import ch.frontg8.lib.connection.TlsTest;
import ch.frontg8.lib.data.DataService;
import ch.frontg8.lib.data.MessageTypes;
import ch.frontg8.lib.dbstore.ContactsDataSource;
import ch.frontg8.lib.protobuf.Frontg8Client;

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

//        Button buttonKeyGen = (Button) findViewById(R.id.buttonKeyGen);
        Button buttonTlsTest = (Button) findViewById(R.id.buttonTlsTest);
        Button buttonShowConfig = (Button) findViewById(R.id.buttonShowConfig);
        Button buttonClearDB = (Button) findViewById(R.id.buttonClearDB);
        Button buttonLoadTestData = (Button) findViewById(R.id.buttonLoadTestData);
        Button buttonShowDB = (Button) findViewById(R.id.buttonShowDB);

        datasource.open();

//        buttonKeyGen.setOnClickListener(new AdapterView.OnClickListener() {
//            public void onClick(View view) {
//                try {
//                    LibCrypto.generateNewKeys(new KeystoreHandler(thisActivity), thisActivity);
//                    Toast.makeText(thisActivity, "Keys generated", Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        buttonShowConfig.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                textViewLog = (TextView) findViewById(R.id.textViewLog);
                textViewLog.setText("");
                textViewLog.append("Username: " + LibConfig.getUsername(thisActivity) + "\n");
                textViewLog.append("Servername: " + LibConfig.getServerName(thisActivity) + "\n");
                textViewLog.append("Serverport: " + LibConfig.getServerPort(thisActivity) + "\n");
                textViewLog.append("Lastmessage-Hash: " + new String (LibConfig.getLastMessageHash(thisActivity)) + "\n");
            }
        });

        buttonClearDB.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                try {
                    mService.send(android.os.Message.obtain(null, MessageTypes.MSG_RESET));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Toast.makeText(thisActivity, "all data deleted", Toast.LENGTH_SHORT).show();
            }
        });

        buttonLoadTestData.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                String keyN = "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBGp6vB5n7L2+6QdKY5ETFnY0QhIFIUqtVW0QaSDEUYJoLceBjKLyiLiQhiYo8tANXsBlrB+F/wQPARoYbaaFKX/cBUTYioTcVWpa4r2lupMyBwZ7x3v8cznfY4aSRWcQKIOtQxpHm7sDQWnViAVmKI4Xgw50ZE0ONxgIjNBioosO3K6I=";
                String keyT = "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQAOKH+erAfDUZ7q00rwKxL38D7HMZIbaXvam4JUXDl8JWZiPH32Ztg0KnOAaWg0gncmZYqEfvqw3nNUPLPQy6YMCoABnN/pFmR83qarPPXxnfzTgm7KhJvf6gnHeBziyaHLJj9PVdryHQIlb2V6f1YbTlEjkCuAk86oJZ6CExP/LAToe8=";
                String keyU = "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQAlNXG+1yARiiB4GlXsTeoIYcqF1UAfcD2OyfS2PKMUc382ragibK1pZv/gs5v2rRkAlYmqnj/pSWMTn2iz2G8PisAcbLnIxz7bjJzAr+jXdERyhW+I9y8FTxMg9d9nqWly7eMP2frIBjK5TzrTgrVZV8bjxnRXpq6nYCHMsq29O1SsAw=";
                String keyM = "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBTEx0CPC1J9S76UNu2zXa0RcyzQMsY2Ajm4OLXTHdC20qOHYCaAFo41E76mxraVnITDrUVOA8Lugnv8U7MpGgA0QAuLbHDMz8O6hmtG9KoRz6NZKrM1eOVDQWU5B2PH2znCGn9htAO4pn1SOk0Ag91AjbvTd8JYbpuxD9EeMx4DyS3Ek=";
                String keyV = "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQBotZ8vHmDtm4i1YTslt3tknPvZ/3fDDkfRC+nK4kkwBWF9CMRZgZ2PHheqCJgkED36v8K5wOcARkcuXhfuB8q0AEAyhQ6HgusnLy+HugytEfoQhNBGiE0QFlVSS1ZHzHxnm/aWdBZ58RYrkFWOun4Dnf6eDv+RFMVoYHZLREO2dRIp7w=";
                ArrayList<Contact> contacts = new ArrayList<>();
                contacts.add(new Contact(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Mr", "Nobody", keyN, 0, false));
                contacts.add(new Contact(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Tobias", "Stauber", keyT, 0, false));
                contacts.add(new Contact(UUID.fromString("33333333-3333-3333-3333-333333333333"), "Ueli", "Bosshard", keyU, 0, false));
                contacts.add(new Contact(UUID.fromString("66666666-6666-6666-6666-666666666666"), "MM", "", keyM, 0, false));
                contacts.add(new Contact(UUID.fromString("77777777-7777-7777-7777-777777777777"), "VR", "", keyV, 0, false));

                try {
                    for (Contact c : contacts) {
                        mService.send(android.os.Message.obtain(null, MessageTypes.MSG_UPDATE_CONTACT, c));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Toast.makeText(thisActivity, "demo data inserted", Toast.LENGTH_SHORT).show();
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
                for (Contact c : contacts) {
                    ArrayList<Frontg8Client.Encrypted> messages = datasource.getEncryptedMessagesByUUID(c.getContactId());
                    textViewLog.append(c.getName() + " "
                            + c.getSurname() + ", "
                            + "Key valid: " + c.hasValidPubKey() + ", "
                            + messages.size() + " Messages"
                            + "\n");
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