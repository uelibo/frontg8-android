package ch.frontg8.view;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import ch.frontg8.R;
import ch.frontg8.bl.Message;
import ch.frontg8.view.model.MessageAdapter;

public class MessageActivity extends AppCompatActivity {
    private Context thisActivity = this;
    private MessageAdapter dataAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        final ArrayList<Message> messageList = new ArrayList<>();
        messageList.add(new Message("Foo"));
        messageList.add(new Message("Bar"));
        messageList.add(new Message("Baz"));

        dataAdapter = new MessageAdapter(this, R.layout.rowlayout_message, messageList);
        ListView listView = (ListView) findViewById(R.id.listView);

        listView.setAdapter(dataAdapter);
        listView.setTextFilterEnabled(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Message message = (Message) parent.getItemAtPosition(position);
                //message.setSelected(!message.isSelected());
                dataAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
