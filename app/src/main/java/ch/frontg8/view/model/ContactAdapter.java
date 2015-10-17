package ch.frontg8.view.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;

public class ContactAdapter extends ArrayAdapter<Contact> {
    private Context thisActivity;
    private final ArrayList<Contact> originalContactList;
    private final ArrayList<Contact> filteredContactList;

    public ContactAdapter(Context context, int textViewResourceId, ArrayList<Contact> contactList) {
        super(context, textViewResourceId, contactList);
        this.thisActivity = context;
        this.originalContactList = new ArrayList<>();
        this.filteredContactList = new ArrayList<>();
        this.originalContactList.addAll(contactList);
        this.filteredContactList.addAll(contactList);
    }

    public List<Contact> getAll() {
        List<Contact> contacts = new ArrayList<>(getCount());
        for (int i = 0; i < getCount(); ++i) {
            contacts.add(getItem(i));
        }
        return contacts;
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
            LayoutInflater layoutInflater = (LayoutInflater) thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.rowlayout_contact, null);
        }

        TextView textViewContactName = (TextView) convertView.findViewById(R.id.textView);
        TextView textViewNumOfMessages = (TextView) convertView.findViewById(R.id.textView2);

        textViewContactName.setText(contact.getName());
        if (!contact.getSurname().equals("")) {
            textViewContactName.append(" " + contact.getSurname());
        }
        textViewNumOfMessages.setText(" (" + contact.getMessages().size() + ")");

        return convertView;
    }
}

