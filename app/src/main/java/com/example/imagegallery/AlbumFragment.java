package com.example.imagegallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {

    private static final String ARG_PARAM1 = "ArrayList<AlbumData>";
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

    public static AlbumFragment newInstance(ArrayList<AlbumData> albums) {

        AlbumFragment fragment = new AlbumFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1, albums);
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView rvAlbums, rvUtilities;
    private AlbumAdapter adapter, adapterUtilities;
    private ImageView btnAddAlbum;

    private ImageView btnSort, btnOptions;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if(getArguments() != null){
            defaultAlbums = getArguments().getParcelableArrayList(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((MainActivity) getContext()).setCurrentFragment(MainActivity.FragmentType.ALBUM_FRAGMENT);

        View albumFragment = inflater.inflate(R.layout.fragment_album, container, false);
        albums = new ArrayList<>();
        ArrayList<String> albumNameList = SharedPreferencesManager.loadAlbumNameList(getContext());
        if(albumNameList != null) {
            for (String albumName : albumNameList) {
                AlbumData album = SharedPreferencesManager.loadAlbumData(getContext(), albumName);
                albums.add(album);
            }
        }

        //My albums section
        AlbumData.sortAlbumByDate(albums, ascending);

        rvAlbums = albumFragment.findViewById(R.id.rv_albums);
//        RecyclerView.ItemDecoration itemDecoration = new
//                DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
//        rvAlbums.addItemDecoration(itemDecoration);
        rvAlbums.setHasFixedSize(true);
        rvAlbums.setItemViewCacheSize(20);

        adapter = new AlbumAdapter(albumFragment.getContext(), albums);
        rvAlbums.setAdapter(adapter);
        rvAlbums.setLayoutManager(new GridLayoutManager(getContext(), 3));



        //Utilities section
        rvUtilities = albumFragment.findViewById(R.id.rv_utilities);
        //rvUtilities.addItemDecoration(itemDecoration);
        rvUtilities.setHasFixedSize(true);
        rvUtilities.setItemViewCacheSize(20);

        adapterUtilities = new AlbumAdapter(albumFragment.getContext(), albums);
        rvUtilities.setAdapter(adapterUtilities);
        rvUtilities.setLayoutManager(new GridLayoutManager(getContext(), 3));


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
                popupMenu.inflate(R.menu.album_popup_menu);
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

   private void AddNewAlbum() {
        View addAlbumView = getLayoutInflater().inflate(R.layout.add_album, null);
        EditText txtName = addAlbumView.findViewById(R.id.edit_album_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(addAlbumView);
        String strAdd = getString(R.string.add);
        builder.setPositiveButton(strAdd, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String albumName = txtName.getText().toString();

                if(albumName.length() != 0) {
                    for(AlbumData album : albums) {
                        if(album.getAlbumName().equals(albumName)) {
                            Toast.makeText(getContext(), getString(R.string.album_name_exists), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    AlbumData album = new AlbumData(albumName);
                    adapter.addAlbum(album);
                    Toast.makeText(getContext(), getString(R.string.album_added), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(), getString(R.string.album_name_empty), Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getContext(), getString(R.string.album_not_added), Toast.LENGTH_SHORT).show();
            }
        });

        builder.create();
        builder.show();
    }

    public void setDefaultAlbums(ArrayList<AlbumData> defaultAlbums) {
        this.defaultAlbums = defaultAlbums;
        adapterUtilities = new AlbumAdapter(getContext(), defaultAlbums);
        rvUtilities.setAdapter(adapterUtilities);
    }
}
