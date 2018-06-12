package com.example.anarg.openmap2;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.HashMap;

public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

    private ArrayList<HashMap<Integer,String>> autoCompleteList;

    public AutoCompleteAdapter(Context context, int textViewResourceId,ArrayList<HashMap<Integer,String>> data) {
        super(context, textViewResourceId);
        autoCompleteList=data;
    }


    @Override
    public int getCount() {
        return super.getCount();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {

//                    autoCompleteList = new ArrayList<HashMap<Integer, String>>();
//                    for (int i = 0; i < cityZipList.size(); i++) {
//                        if (cityZipList.get(i).get("city").startsWith(
//                                constraint.toString()) || cityZipList.get(i).get("zip").startsWith(
//                                constraint.toString())) {
//                            autoCompleteList.add(cityZipList.get(i));
//                        }
//                    }

                    // Now assign the values and count to the FilterResults
                    // object
                    filterResults.values = autoCompleteList;

                    filterResults.count = autoCompleteList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}

