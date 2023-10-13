package com.example.imagegallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AlbumFragment extends Fragment {
    private static final String ARG_PARAM1 = "ArrayList<AlbumData>";

    private ArrayList<AlbumData> albums;

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

    private RecyclerView rvAlbums;
    private AlbumAdapter adapter;
    private ImageView btnAddAlbum;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            albums = getArguments().getParcelableArrayList(ARG_PARAM1);
        }
        Log.d("AlbumFragment", getContext().toString());
        ArrayList<String> albumNameList = SharedPreferencesManager.loadAlbumNameList(getContext());
        if(albumNameList != null) {
            for (String albumName : albumNameList) {
                AlbumData album = SharedPreferencesManager.loadAlbumData(getContext(), albumName);
                albums.add(album);

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View albumFragment = inflater.inflate(R.layout.fragment_album, container, false);

        rvAlbums = albumFragment.findViewById(R.id.rv_albums);
        rvAlbums.setLayoutManager(new LinearLayoutManager(albumFragment.getContext()));

        adapter = new AlbumAdapter(albumFragment.getContext(), albums);
        rvAlbums.setAdapter(adapter);

        btnAddAlbum = albumFragment.findViewById(R.id.btnAddAlbum);
        btnAddAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewAlbum();
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
            if(!name.equals("All Images")) {
                albumNameList.add(name);
                SharedPreferencesManager.saveAlbumData(getContext(), album);
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
