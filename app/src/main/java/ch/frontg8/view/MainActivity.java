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
import android.widget.RelativeLayout;

import java.io.Serializable;
import java.util.ArrayList;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.view.model.ContactAdapter;

public class MainActivity extends AppCompatActivity {
    private Context thisActivity = this;
    private ContactAdapter dataAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<Contact> contactList = new ArrayList<>();
        contactList.add(new Contact("Ueli"));
        contactList.add(new Contact("Tobi"));
        contactList.add(new Contact("Flix"));
        contactList.add(new Contact("Paul"));
        contactList.add(new Contact("Benny"));

        dataAdapter = new ContactAdapter(this, R.layout.rowlayout_contact, contactList);
        ListView listView = (ListView) findViewById(R.id.listView);

        listView.setAdapter(dataAdapter);
        listView.setTextFilterEnabled(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = (Contact) parent.getItemAtPosition(position);
                Intent intent = new Intent(thisActivity, MessageActivity.class);
                startActivity(intent);
                //dataAdapter.notifyDataSetChanged();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

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
        switch (id) {
            case R.id.action_newcontact:
                Intent intent = new Intent(this, ContactActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                //Intent intent3 = new Intent(this, SettingsActivity.class);
                //startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void myContactButtonHandler(AdapterView<?> parent, View v, int position, long id)
    {
        ListView lvItems = (ListView) findViewById(R.id.listView);
        RelativeLayout vwParentRow = (RelativeLayout)v.getParent();

        //Contact currentContact = (Contact) parent.getItemAtPosition(position);

        Intent intent = new Intent(this, ContactActivity.class);
        //Contact currentContact = dataAdapter.getAll().get(pos);
        //intent.putExtra(getString(R.string.editcontact), (Serializable) currentContact;
        startActivity(intent);
    }

    public void myMessageButtonHandler(AdapterView<?> parent, View v, int position, long id)
    {
        ListView lvItems = (ListView) findViewById(R.id.listView);
        RelativeLayout vwParentRow = (RelativeLayout)v.getParent();

        Intent intent = new Intent(this, MessageActivity.class);
        startActivity(intent);
    }

}
