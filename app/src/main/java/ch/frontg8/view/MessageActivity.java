package ch.frontg8.view;

import android.app.SearchManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import ch.frontg8.R;
import ch.frontg8.bl.Message;

public class MessageActivity extends AppCompatActivity {
    MyCustomAdapter dataAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        final ArrayList<Message> messageList = new ArrayList<>();
        messageList.add(new Message("Foo"));
        messageList.add(new Message("Bar"));
        messageList.add(new Message("Baz"));

        dataAdapter = new MyCustomAdapter(this, R.layout.rowlayout_message, messageList);
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

    private class MyCustomAdapter extends ArrayAdapter<Message> {

        private final ArrayList<Message> originalMessageList;
        private final ArrayList<Message> filteredMessageList;

        public MyCustomAdapter(Context context, int textViewResourceId, ArrayList<Message> messageList) {
            super(context, textViewResourceId, messageList);
            this.originalMessageList = new ArrayList<Message>();
            this.filteredMessageList = new ArrayList<Message>();
            this.originalMessageList.addAll(messageList);
            this.filteredMessageList.addAll(messageList);
        }

        @Override
        public Filter getFilter() {
            return new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    filteredMessageList.clear();

                    if (constraint != null) {
                        for (Message m : originalMessageList) {
                            if (m.getMessage().toLowerCase(Locale.getDefault()).contains(constraint)) {
                                filteredMessageList.add(m);
                            }
                        }

                        filterResults.values = filteredMessageList;
                        filterResults.count = filteredMessageList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    clear();
                    addAll(filteredMessageList);
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

            final Message message = filteredMessageList.get(position);

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.rowlayout_message, null);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.textView);
            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);

            checkBox.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    message.setSelected(cb.isChecked());
                }
            });

            textView.setText(" ()");
            checkBox.setText(message.getMessage());
            checkBox.setChecked(message.isSelected());

            return convertView;
        }
    }
}
