package com.example.imagegallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.os.Bundle;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    ArrayList<ImageObject> images = new ArrayList<>();
    FragmentManager fragmentManager = getSupportFragmentManager();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        images = getIntent().getParcelableArrayListExtra("images");
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ImageFragment imageFragment = ImageFragment.newInstance(images, "Search");
        ft.replace(R.id.fragment_container, imageFragment);
        ft.commit();
    }
}