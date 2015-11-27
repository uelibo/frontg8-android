package ch.frontg8.view.model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ch.frontg8.R;
import ch.frontg8.bl.Message;

public class MessageAdapter extends CustomAdapter<Message> {
    public MessageAdapter(Context context, ArrayList<Message> arrayList) {
        super(context, arrayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final Message message = arrayList.get(position);

        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.rowlayout_message, null);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.textView);
        textView.setText(message.getMessage());

        return convertView;
    }
}
