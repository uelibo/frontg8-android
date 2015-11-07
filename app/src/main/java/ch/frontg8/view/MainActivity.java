package ch.frontg8.view;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;
import ch.frontg8.lib.dbstore.ContactsDataSource;
import ch.frontg8.view.model.ContactAdapter;

public class MainActivity extends AppCompatActivity {
    private Context thisActivity = this;
    private ContactAdapter dataAdapter = null;
    private ContactsDataSource datasource = new ContactsDataSource(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        datasource.open();

        dataAdapter = new ContactAdapter(this, R.layout.rowlayout_contact, datasource.getAllContacts());
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(dataAdapter);
        listView.setTextFilterEnabled(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = (Contact) parent.getItemAtPosition(position);
                Intent intent = new Intent(thisActivity, MessageActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("contactid", contact.getContactId());
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        TextView title = (TextView) findViewById(R.id.textViewTitle);
        SharedPreferences preferences = getSharedPreferences(getString(R.string.preferences), MODE_PRIVATE);
        title.append(" of " + preferences.getString("edittext_preference_username", "paul"));
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("Resumed");
        //dataAdapter.replace(datasource.getAllContacts());
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
                Intent intentNewContact = new Intent(this, ContactActivity.class);
                startActivity(intentNewContact);
                return true;
            case R.id.action_settings:
                Intent intentSettings = new Intent(this, SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.action_aboutme:
                Intent intentAboutMe = new Intent(this, AboutMeActivity.class);
                startActivity(intentAboutMe);
                return true;
            case R.id.action_connection_test:
                Intent intentConnectionTest = new Intent(this, ConnectionTestActivity.class);
                startActivity(intentConnectionTest);
                return true;
            case R.id.action_developer:
                Intent intentDeveloper = new Intent(this, DeveloperActivity.class);
                startActivity(intentDeveloper);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void myContactButtonHandler(View v)
    {
        View parentRow = (View) v.getParent();
        ListView parent = (ListView) parentRow.getParent();
        final int position = parent.getPositionForView(parentRow);
        Contact contact = (Contact) parent.getItemAtPosition(position);
        Intent intent = new Intent(thisActivity, ContactActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("contactid", contact.getContactId());
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
