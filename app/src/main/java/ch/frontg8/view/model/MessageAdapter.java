package ch.frontg8.view.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import ch.frontg8.R;
import ch.frontg8.bl.Message;

public class MessageAdapter extends ArrayAdapter<Message> {
    private Context thisActivity;

    private final ArrayList<Message> originalMessageList;
    private final ArrayList<Message> filteredMessageList;

    public MessageAdapter(Context context, int textViewResourceId, ArrayList<Message> messageList) {
        super(context, textViewResourceId, messageList);
        this.thisActivity = context;
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
            LayoutInflater layoutInflater = (LayoutInflater) thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.rowlayout_message, null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.textView);
        textView.setText(message.getMessage());

        return convertView;
    }
}
