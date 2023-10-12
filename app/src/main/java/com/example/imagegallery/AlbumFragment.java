package com.example.imagegallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            albums = getArguments().getParcelableArrayList(ARG_PARAM1);
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

        return albumFragment;
    }
}
