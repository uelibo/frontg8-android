package ch.frontg8;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Handle item selection
        switch (id) {
            case R.id.contacts:
                Intent intent = new Intent(this, ContactActivity.class);
                startActivity(intent);
                return true;
            case R.id.conversation:
                Intent intent2 = new Intent(this, ConversationActivity.class);
                startActivity(intent2);
                return true;
            case R.id.action_settings:
                //startActivity(new Intent(Settings.ACTION_SETTINGS));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }


        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}
        //return super.onOptionsItemSelected(item);
    }
}
