package ch.frontg8.view;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import ch.frontg8.R;
import ch.frontg8.lib.data.DataService;
import ch.frontg8.lib.data.MessageTypes;
import ch.frontg8.lib.filechooser.view.FileChooser;

public class CertImportActivity extends AppCompatActivity {
    private static final int REQUEST_PATH = 1;
    private static final int IMPORT_CERT = 2;
    private static final int IMPORT_KEYPAIR = 3;
    private static final int EXPORT_KEYPAIR = 4;
    private static final int MY_PERMISSIONS_STORAGE_READ = 8;
    private static final int MY_PERMISSIONS_STORAGE_WRITE = 9;

    // Messenger to get Contacts
    private final Messenger mMessenger = new Messenger(new IncomingHandler(this));
    private final Activity thisActivity = this;
    private TextView textViewLog;
    private Messenger mService;

    Button buttonImportCert;
    Button buttonImportKeypair;
    Button buttonExportKeypair;

    // Connection to DataService
    private final ServiceConnection mConnection = new ServiceConnection() {

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

        buttonImportCert = (Button) findViewById(R.id.buttonImportCert);
        buttonImportKeypair = (Button) findViewById(R.id.buttonImportKeypair);
        buttonExportKeypair = (Button) findViewById(R.id.buttonExportKeypair);
        textViewLog = (TextView) findViewById(R.id.textViewLog);

        disableButtons();

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

        verifyStoragePermissions();
    }

    private void disableButtons() {
        buttonImportCert.setEnabled(false);
        buttonImportKeypair.setEnabled(false);
        buttonExportKeypair.setEnabled(false);
        textViewLog.setText("no permissions to read/write files");
    }

    private void enableButtons() {
        buttonImportCert.setEnabled(true);
        buttonImportKeypair.setEnabled(true);
        buttonExportKeypair.setEnabled(true);
    }

    private void openFileChooser(int intentId, boolean chooseDir, boolean showFiles) {
        Intent intent = new Intent(thisActivity, FileChooser.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(FileChooser.CHOOSE_DIR, chooseDir);
        bundle.putSerializable(FileChooser.SHOW_FILES, showFiles);
//                bundle.putSerializable(FileChooser.FILE_EXTENSION, "pem");
        intent.putExtras(bundle);
        startActivityForResult(intent, intentId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cert_import, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);

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
                    textViewLog.setText(getString(R.string.CertImportActivity_ImportCertFrom, path));
                    sendMessage(MessageTypes.MSG_IMPORT_CACERT, path);
                    break;
                case IMPORT_KEYPAIR:
                    path = data.getStringExtra("GetPath") + "/" + data.getStringExtra("GetFileName");
                    textViewLog.setText(getString(R.string.CertImportActivity_ImportKeyFrom, path));
                    sendMessage(MessageTypes.MSG_IMPORT_KEY, path);
                    break;
                case EXPORT_KEYPAIR:
                    path = data.getStringExtra("GetPath") + "/exported-key.pem";
                    textViewLog.setText(getString(R.string.CertImportActivity_ExportKeyTo, path));
                    sendMessage(MessageTypes.MSG_EXPORT_KEY, path);
                    break;
                default:
                    textViewLog.setText(R.string.CertImportActivity_Error);
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
    private static class IncomingHandler extends Handler {
        WeakReference<CertImportActivity> activity;

        public IncomingHandler(CertImportActivity activity) {
            this.activity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            if (activity.get() != null) {
                switch (msg.what) {
                    case MessageTypes.MSG_ERROR:
                        activity.get().textViewLog.append("Got Error from Data-Service");
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }
    }

    // Check for Permissions
    // http://developer.android.com/training/permissions/requesting.html
    public void verifyStoragePermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                textViewLog.append("\nyou need to accept read/write access to import/export certificates");
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_STORAGE_WRITE);
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//                        MY_PERMISSIONS_STORAGE_READ);
            }
        } else {
            enableButtons();
            textViewLog.setText("");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_STORAGE_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    enableButtons();
                    textViewLog.setText("");
                } else {
                    // permission denied, boo!
                    textViewLog.append("\nno permissions :(");
                }
                return;
            }
        }
    }

}
