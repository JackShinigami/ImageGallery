package com.example.imagegallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {

    private ArrayList<AlbumData> albums;
    private ArrayList<AlbumData> defaultAlbums;

    public static boolean ascending = false;

    private enum  SortType{
        NAME, DATE
    }

    private SortType sortType = SortType.DATE;

    public AlbumFragment() {
        // Required empty public constructor
    }

    public static AlbumFragment newInstance() {
        AlbumFragment fragment = new AlbumFragment();
        return fragment;
    }

    private RecyclerView rvAlbums, rvUitilities;
    private AlbumAdapter adapter, adapterUtilities;
    private ImageView btnAddAlbum;

    private ImageView btnSort, btnOptions;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        albums = new ArrayList<>();
        defaultAlbums = AlbumHelper.createDefaultAlbum(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View albumFragment = inflater.inflate(R.layout.fragment_album, container, false);

        ArrayList<String> albumNameList = SharedPreferencesManager.loadAlbumNameList(getContext());
        if(albumNameList != null) {
            for (String albumName : albumNameList) {
                AlbumData album = SharedPreferencesManager.loadAlbumData(getContext(), albumName);
                albums.add(album);
                Log.d("AlbumFragment", "onCreate: " );
            }
        }

        //My albums section
        AlbumData.sortAlbumByDate(albums, ascending);

        rvAlbums = albumFragment.findViewById(R.id.rv_albums);
        rvAlbums.setLayoutManager(new LinearLayoutManager(albumFragment.getContext()));

        adapter = new AlbumAdapter(albumFragment.getContext(), albums);
        rvAlbums.setAdapter(adapter);

        //Utilities section
        rvUitilities = albumFragment.findViewById(R.id.rv_utilities);
        rvUitilities.setLayoutManager(new LinearLayoutManager(albumFragment.getContext()));
        adapterUtilities = new AlbumAdapter(albumFragment.getContext(), defaultAlbums);
        rvUitilities.setAdapter(adapterUtilities);


        btnAddAlbum = albumFragment.findViewById(R.id.btnAddAlbum);
        btnAddAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewAlbum();
            }
        });

        btnSort = albumFragment.findViewById(R.id.btnSort);
        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ascending){

                    if(SortType.DATE == sortType)
                        AlbumData.sortAlbumByDate(albums, ascending);
                    else
                        AlbumData.sortAlbumByName(albums, ascending);
                    btnSort.setImageResource(R.drawable.ic_arrow_up);
                    ascending = false;
                }
                else{

                    if(SortType.DATE == sortType)
                        AlbumData.sortAlbumByDate(albums, ascending);
                    else
                        AlbumData.sortAlbumByName(albums, ascending);
                    btnSort.setImageResource(R.drawable.ic_arrow_down);
                    ascending = true;
                }

                adapter.notifyDataSetChanged();
            }
        });

        if(ascending)
            btnSort.setImageResource(R.drawable.ic_arrow_down);
        else
            btnSort.setImageResource(R.drawable.ic_arrow_up);

        btnOptions = albumFragment.findViewById(R.id.btnOptions);
        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(albumFragment.getContext(), btnOptions);
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id == R.id.menu_date)
                        {
                            sortType = SortType.DATE;
                            AlbumData.sortAlbumByDate(albums, ascending);
                            adapter.notifyDataSetChanged();
                        }
                        else if(id == R.id.menu_name)
                        {
                            sortType = SortType.NAME;
                            AlbumData.sortAlbumByName(albums, ascending);
                            adapter.notifyDataSetChanged();
                        }
                        return false;
                    }
                });
                popupMenu.show();

            }
        });

        return albumFragment;

    }

    @Override
    public void onPause() {
        super.onPause();
        ArrayList<String> albumNameList = new ArrayList<>();

        for(AlbumData album : albums) {
            String name = album.getAlbumName();
            if(!name.equals("All Images") && !name.equals("Trash")) {
                albumNameList.add(name);
                SharedPreferencesManager.saveAlbumData(getContext(), album);
                Log.println(Log.DEBUG, "AlbumFragment", "onPause: " + name);
            }
        }

        SharedPreferencesManager.saveAlbumNameList(getContext(), albumNameList);
    }

    private void AddNewAlbum() {
        View addAlbumView = getLayoutInflater().inflate(R.layout.add_album, null);
        EditText txtName = addAlbumView.findViewById(R.id.edit_album_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(addAlbumView);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String albumName = txtName.getText().toString();

                if(albumName.length() != 0) {
                    for(AlbumData album : albums) {
                        if(album.getAlbumName().equals(albumName)) {
                            Toast.makeText(getContext(), "Album name already exists", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    AlbumData album = new AlbumData(albumName);
                    adapter.addAlbum(album);
                    Toast.makeText(getContext(), "Album added", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(), "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), "Album not added", Toast.LENGTH_SHORT).show();
            }
        });

        builder.create();
        builder.show();
    }

}
