package ch.frontg8.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import ch.frontg8.R;

public class CertImportActivity extends AppCompatActivity {
    private Context thisActivity = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cert_import);

        Button buttonImportCert = (Button) findViewById(R.id.buttonImportCert);
        TextView textViewLog = (TextView) findViewById(R.id.textViewLog);
        textViewLog.setText("");
        //textViewLog.append("");

        buttonImportCert.setOnClickListener(new AdapterView.OnClickListener() {
            public void onClick(View view) {

                Intent intent = new Intent(thisActivity, FilePickerActivity.class);
                startActivityForResult(intent, 1);

            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            // Do anything with file
        }
    }

}
