package ch.frontg8.view.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

import ch.frontg8.bl.FilterText;

abstract public class CustomAdapter<T extends FilterText> extends BaseAdapter implements Filterable {
    final Context context;
    private final ArrayList<T> mOriginalValues;
    ArrayList<T> arrayList;

    CustomAdapter(Context context, ArrayList<T> arrayList) {
        this.arrayList = arrayList;
        this.mOriginalValues = new ArrayList<>(arrayList); // saves the original data in mOriginalValues
        this.context = context;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public T getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void add(T element) {
        arrayList.add(element);
        mOriginalValues.add(element);
        notifyDataSetChanged();
    }

    public void clear() {
        arrayList.clear();
        mOriginalValues.clear();
        notifyDataSetChanged();
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);

    @Override
    public Filter getFilter() {
        return new Filter() {

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                arrayList = (ArrayList<T>) results.values; // has the filtered values
                notifyDataSetChanged();  // notifies the data with new filtered values
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();        // Holds the results of a filtering operation in values
                List<T> FilteredArrList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    // set the Original result to return
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                } else {
                    constraint = constraint.toString().toLowerCase();
                    for (int i = 0; i < mOriginalValues.size(); i++) {
                        T data = mOriginalValues.get(i);
                        if (data.getFilterValue().toLowerCase().contains(constraint.toString())) {
                            FilteredArrList.add(data);
                        }
                    }
                    // set the Filtered result to return
                    results.count = FilteredArrList.size();
                    results.values = FilteredArrList;
                }
                return results;
            }
        };
    }
}

