package ch.frontg8.view;

import android.app.Activity;
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

import ch.frontg8.R;
import ch.frontg8.lib.data.DataService;
import ch.frontg8.lib.data.MessageTypes;
import ch.frontg8.lib.filechooser.view.FileChooser;

public class CertImportActivity extends AppCompatActivity {
    private static final int REQUEST_PATH = 1;
    private static final int IMPORT_CERT = 2;
    private static final int IMPORT_KEYPAIR = 3;
    private static final int EXPORT_KEYPAIR = 4;
    // Messenger to get Contacts
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private Activity thisActivity = this;
    private TextView textViewLog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cert_import);

        Button buttonImportCert = (Button) findViewById(R.id.buttonImportCert);
        Button buttonImportKeypair = (Button) findViewById(R.id.buttonImportKeypair);
        Button buttonExportKeypair = (Button) findViewById(R.id.buttonExportKeypair);
        textViewLog = (TextView) findViewById(R.id.textViewLog);

        buttonImportCert.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                openFileChooser(IMPORT_CERT, false, true);
            }
        });

        buttonImportKeypair.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                openFileChooser(IMPORT_KEYPAIR, false, true);
            }
        });

        buttonExportKeypair.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                openFileChooser(EXPORT_KEYPAIR, true, false);
            }
        });
    }

    private void openFileChooser(int identId, boolean chooseDir, boolean showFiles) {
        Intent intent = new Intent(thisActivity, FileChooser.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(FileChooser.CHOOSE_DIR, chooseDir);
        bundle.putSerializable(FileChooser.SHOW_FILES, showFiles);
//                bundle.putSerializable(FileChooser.FILE_EXTENSION, "pem");
        intent.putExtras(bundle);
        startActivityForResult(intent, identId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cert_import, menu);
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

    // Listen for results.
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // See which child activity is calling us back.
        String path;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case IMPORT_CERT:
                    path = data.getStringExtra("GetPath") + "/" + data.getStringExtra("GetFileName");
                    textViewLog.setText("Import CA Cert from: " + path);
                    sendMessage(MessageTypes.MSG_IMPORT_CACERT, path);
                    break;
                case IMPORT_KEYPAIR:
                    path = data.getStringExtra("GetPath") + "/" + data.getStringExtra("GetFileName");
                    textViewLog.setText("Import Keypair from: " + path);
                    sendMessage(MessageTypes.MSG_IMPORT_KEY, path);
                    break;
                case EXPORT_KEYPAIR:
                    path = data.getStringExtra("GetPath") + "/exported-key.pem";
                    textViewLog.setText("Export Keypair to: " + path);
                    sendMessage(MessageTypes.MSG_EXPORT_KEY, path);
                    break;
                default:
                    textViewLog.setText("Something went wrong...");
            }
        }
    }

    private void sendMessage(int messageType, String value) {
        try {
            Message message = Message.obtain(null, messageType, value);
            message.replyTo = mMessenger;
            mService.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(thisActivity.getClass().getSimpleName(), "onResume");

        // bind again to DataService:
        Intent intent = new Intent(this, DataService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    // Handler for Messages from DataService
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MessageTypes.MSG_ERROR:
                    textViewLog.append("Got Error from Data-Service");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }


}
