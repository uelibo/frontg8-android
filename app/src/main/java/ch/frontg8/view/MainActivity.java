package ch.frontg8.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.SearchManager;
import android.support.v7.widget.SearchView;

import java.util.ArrayList;
import java.util.Locale;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;

public class MainActivity extends AppCompatActivity {
    MyCustomAdapter dataAdapter = null;

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

        dataAdapter = new MyCustomAdapter(this, R.layout.rowlayout_contact, contactList);
        ListView listView = (ListView) findViewById(R.id.listView);

        listView.setAdapter(dataAdapter);
        listView.setTextFilterEnabled(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact contact = (Contact) parent.getItemAtPosition(position);
                contact.setSelected(!contact.isSelected());
                dataAdapter.notifyDataSetChanged();
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        // Handle item selection
        switch (id) {
            case R.id.action_settings:
                //Intent intent3 = new Intent(this, SettingsActivity.class);
                //startActivity(intent3);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class MyCustomAdapter extends ArrayAdapter<Contact> {

        private final ArrayList<Contact> originalContactList;
        private final ArrayList<Contact> filteredContactList;

        public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Contact> contactList) {
            super(context, textViewResourceId, contactList);
            this.originalContactList = new ArrayList<Contact>();
            this.filteredContactList = new ArrayList<Contact>();
            this.originalContactList.addAll(contactList);
            this.filteredContactList.addAll(contactList);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    filteredContactList.clear();

                    if (constraint != null) {
                        for (Contact c : originalContactList) {
                            if (c.getName().toLowerCase(Locale.getDefault()).contains(constraint)) {
                                filteredContactList.add(c);
                            }
                        }

                        filterResults.values = filteredContactList;
                        filterResults.count = filteredContactList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    clear();
                    addAll(filteredContactList);
                    if (results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final Contact contact = filteredContactList.get(position);

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.rowlayout_contact, null);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.textView);
            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

            checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    contact.setSelected(cb.isChecked());
                }
            });

            textView.setText(" (" + contact.getCode() + ")");
            checkBox.setText(contact.getName());
            checkBox.setChecked(contact.isSelected());

            return convertView;
        }
    }

    public void myContactButtonHandler(View v)
    {
        ListView lvItems = (ListView) findViewById(R.id.listView);

        RelativeLayout vwParentRow = (RelativeLayout)v.getParent();

        Intent intent = new Intent(this, ContactActivity.class);
        startActivity(intent);
    }

    public void myMessageButtonHandler(View v)
    {
        ListView lvItems = (ListView) findViewById(R.id.listView);

        RelativeLayout vwParentRow = (RelativeLayout)v.getParent();

        Intent intent = new Intent(this, MessageActivity.class);
        startActivity(intent);
    }

}
