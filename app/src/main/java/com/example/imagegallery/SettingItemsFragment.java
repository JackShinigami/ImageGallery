package com.example.imagegallery;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.imagegallery.databinding.FragmentSettingMainListBinding;
import com.example.imagegallery.placeholder.SettingPlaceholderContent;

import java.util.List;

/**
 * A fragment representing a list of Items.
 */
public class SettingItemsFragment extends Fragment {

    private int mColumnCount = 1;

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";



    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static SettingItemsFragment newInstance(int columnCount) {
        SettingItemsFragment fragment = new SettingItemsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SettingItemsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_main_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

            // R.string.theme as R.string.defaultTheme
            // Dependency setting adapter
            SettingPlaceholderContent.addItem(new SettingPlaceholderContent.SettingPlaceholderItem("1", getString(R.string.theme), getString(R.string.defaultTheme)));
            SettingPlaceholderContent.addItem(new SettingPlaceholderContent.SettingPlaceholderItem("2", getString(R.string.language), getString(R.string.auto)));
            SettingItemRecyclerViewAdapter adapter = new SettingItemRecyclerViewAdapter(SettingPlaceholderContent.ITEMS);
            recyclerView.setAdapter(adapter);

        }
        return view;
    }
}