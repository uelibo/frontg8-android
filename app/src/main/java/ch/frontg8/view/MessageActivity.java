package ch.frontg8.view;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.bl.Message;
import ch.frontg8.lib.TestDataHandler;
import ch.frontg8.view.model.MessageAdapter;

public class MessageActivity extends AppCompatActivity {
    private Context thisActivity = this;
    private MessageAdapter dataAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Intent intent=this.getIntent();
        Bundle bundle=intent.getExtras();
        Contact contact=(Contact)bundle.getSerializable("contact");

        TextView tv = (TextView) findViewById(R.id.textView2);
        if (contact != null) {
            tv.append(" of " + contact.getName());
        }


        final ArrayList<Message> messageList = TestDataHandler.getMessages();
        dataAdapter = new MessageAdapter(this, R.layout.rowlayout_message, messageList);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(dataAdapter);
        listView.setTextFilterEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                dataAdapter.getFilter().filter(newText.toLowerCase());
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                dataAdapter.getFilter().filter(query.toLowerCase());
                return true;
            }
        });

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
