package com.example.imagegallery;

import android.widget.Filter;

import java.util.ArrayList;

public class SearchFilter extends Filter {
    ArrayList<String> data;
    AutoCompleteAdapter adapter;

    public SearchFilter(ArrayList<String> data, AutoCompleteAdapter adapter) {
        this.data = data;
        this.adapter = adapter;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {

        FilterResults filterResults = new FilterResults();
        ArrayList<String> resultImages = new ArrayList<>();
        for (String item : data) {
            if (item != null && item.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                resultImages.add(item);
            }
        }
        filterResults.values = resultImages;
        filterResults.count = resultImages.size();

        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        if (filterResults != null && filterResults.count > 0) {
            adapter.setImages((ArrayList<String>) filterResults.values);
            adapter.notifyDataSetChanged();
        }
        else{
            adapter.notifyDataSetInvalidated();
        }
    }
}
