package ch.frontg8.view.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ch.frontg8.R;
import ch.frontg8.bl.Contact;

public class ContactAdapter extends CustomAdapter<Contact> {
    public ContactAdapter(Context context, ArrayList<Contact> arrayList) {
        super(context, arrayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Contact contact = arrayList.get(position);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.rowlayout_contact, null);
        }

        TextView textViewContactName = (TextView) convertView.findViewById(R.id.textView);
        TextView textViewNumOfMessages = (TextView) convertView.findViewById(R.id.textView2);

        textViewContactName.setText(contact.getName());
        if (!contact.getSurname().equals("")) {
            textViewContactName.append(" " + contact.getSurname());
        }

        if (contact.getUnreadMessageCounter() > 0) {
            textViewNumOfMessages.setText(String.format(" (%1$d)", contact.getUnreadMessageCounter()));
        } else {
            textViewNumOfMessages.setText(null);
        }

        return convertView;
    }

    public void replace(Contact contact) {
        int index = arrayList.indexOf(contact);
        if (index >= 0) {
            arrayList.set(index, contact);
            notifyDataSetChanged();
        }
    }
}

