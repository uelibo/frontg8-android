package ch.frontg8.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import ch.frontg8.R;
import ch.frontg8.lib.filechooser.view.FileChooser;

public class CertImportActivity extends AppCompatActivity {
    private Activity thisActivity = this;
    private static final int REQUEST_PATH = 1;
    private TextView textViewLog;

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
                openFileChooser(false, true);
            }
        });

        buttonImportKeypair.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                openFileChooser(false, true);
            }
        });

        buttonExportKeypair.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {
                openFileChooser(true, false);
            }
        });
    }

    private void openFileChooser(boolean chooseDir, boolean showFiles){
        Intent intent = new Intent(thisActivity, FileChooser.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(FileChooser.CHOOSE_DIR, chooseDir);
        bundle.putSerializable(FileChooser.SHOW_FILES, showFiles);
//                bundle.putSerializable(FileChooser.FILE_EXTENSION, "pem");
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_PATH);
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
        if (requestCode == REQUEST_PATH && resultCode == RESULT_OK) {
            String curFileName = data.getStringExtra("GetFileName");
            String curDirName = data.getStringExtra("GetPath");
            textViewLog.setText(curDirName + "/" + curFileName);
        }
    }


}
