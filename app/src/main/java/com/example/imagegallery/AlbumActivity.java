package com.example.imagegallery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Html;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AlbumActivity extends AppCompatActivity {

    RecyclerView rvAlbums;
    AlbumAdapter adapter;
    public FragmentManager fragmentManager = getSupportFragmentManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        Intent intent = getIntent();
        Bundle bundleMainAlbum = intent.getExtras();
        ArrayList<ImageObject> imageObjects = bundleMainAlbum.getParcelableArrayList("images");
        //ArrayList<ImageObject> imageObjects = new ArrayList<>();

        //for (Parcelable image : images) {
        //    imageObjects.add((ImageObject) image);
        //}



        ArrayList<AlbumData> albumData = new ArrayList<>();
        AlbumData album = new AlbumData("All Images", imageObjects);
        albumData.add(album);


        rvAlbums = findViewById(R.id.rv_albums);
        rvAlbums.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AlbumAdapter(this, albumData);
        rvAlbums.setAdapter(adapter);
    }
}
