package com.example.imagegallery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class AutoCompleteAdapter extends BaseAdapter implements Filterable {

    ArrayList<String> data;

    public AutoCompleteAdapter(ArrayList<String> data) {
        this.data = data;

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View myView = LayoutInflater.from(viewGroup.getContext()).inflate(android.R.layout.simple_dropdown_item_1line, viewGroup, false);
        TextView textView = myView.findViewById(android.R.id.text1);
        textView.setText(data.get(i));
        return myView;
    }

    @Override
    public Filter getFilter() {
        return new SearchFilter(data, this);
    }

    public void setImages(ArrayList<String> data) {
        this.data = data;
    }
}
