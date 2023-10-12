package com.example.imagegallery;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "ArrayList<ImageObject>";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private ArrayList<ImageObject> images;
    //private String mParam2;

    public ImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImageFragment newInstance(ArrayList<ImageObject> images) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1, images);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView recyclerView;

    private ImageButton btnChangeGrid, btnSort, btnOptions;
    private int[] colNumbers = {2, 3, 4};
    private static int colNumberIndex = 0;

    private static boolean ascending = false;

    private enum SortType {
        DATE, NAME
    }
    private static SortType sortType = SortType.DATE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            images = getArguments().getParcelableArrayList(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View imageFragment = inflater.inflate(R.layout.fragment_image, container, false);
        recyclerView = imageFragment.findViewById(R.id.rv_items);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(imageFragment.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);

        MyAdapter adapter = new MyAdapter(images);
        adapter.setColNumber(colNumbers[colNumberIndex]);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(imageFragment.getContext(), colNumbers[colNumberIndex]));

        btnSort = imageFragment.findViewById(R.id.btnSort);
        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ascending) {
                    ascending = false;
                    if(SortType.DATE == sortType)
                        ImageObject.sortByDate(images, ascending);
                    else if(SortType.NAME == sortType)
                        ImageObject.sortByFileName(images, ascending);
                }
                else {
                    ascending = true;
                    if(SortType.DATE == sortType)
                        ImageObject.sortByDate(images, ascending);
                    else if(SortType.NAME == sortType)
                        ImageObject.sortByFileName(images, ascending);
                }

                adapter.notifyDataSetChanged();
            }
        });

        btnChangeGrid = imageFragment.findViewById(R.id.btnChangeGrid);
        btnChangeGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colNumberIndex = (colNumberIndex + 1) % colNumbers.length;
                adapter.setColNumber(colNumbers[colNumberIndex]);
                recyclerView.setLayoutManager(new GridLayoutManager(imageFragment.getContext(), adapter.getColNumber()));
            }
        });

        btnOptions = imageFragment.findViewById(R.id.btnOptions);
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(imageFragment.getContext(), btnOptions);

                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if(id == R.id.menu_date)
                        {
                            sortType = SortType.DATE;
                            ImageObject.sortByDate(images, ascending);
                            adapter.notifyDataSetChanged();
                        }
                        else if(id == R.id.menu_name)
                        {
                            sortType = SortType.NAME;
                            ImageObject.sortByFileName(images, ascending);
                            adapter.notifyDataSetChanged();
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
            });

        return imageFragment;
    }
}