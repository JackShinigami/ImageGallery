package com.example.imagegallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    public static enum SEARCH_TYPE{
        NAME,
        TAG
    }
    ArrayList<ImageObject> images = new ArrayList<>();
    FragmentManager fragmentManager = getSupportFragmentManager();
    AutoCompleteTextView search_edit_text;
    SEARCH_TYPE search_type = SEARCH_TYPE.NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        images = getIntent().getParcelableArrayListExtra("images");
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ImageFragment imageFragment = ImageFragment.newInstance(images, "Search");
        ft.replace(R.id.fragment_container, imageFragment);
        ft.commit();

        ArrayList<String> data = new ArrayList<>();
        if(search_type == SEARCH_TYPE.NAME) {
            for (ImageObject imageObject : images) {
                data.add(imageObject.getFileName());
            }
        }
        else{
        }

        search_edit_text = findViewById(R.id.search_edit_text);
        search_edit_text.setThreshold(1);
        search_edit_text.setAdapter(new AutoCompleteAdapter(data));

        search_edit_text.setOnItemClickListener((adapterView, view, i, l) -> {
            String name = adapterView.getItemAtPosition(i).toString();
            ArrayList<ImageObject> newImages = new ArrayList<>();
            for(ImageObject imageObject : images){
                if(search_type == SEARCH_TYPE.NAME) {
                    if (imageObject.getFileName().equals(name)) {
                        newImages.add(imageObject);
                    }
                }
            }
            FragmentTransaction ft1 = fragmentManager.beginTransaction();
            ImageFragment imageFragment1 = ImageFragment.newInstance(newImages, "Search");
            ft1.replace(R.id.fragment_container, imageFragment1);
            ft1.commit();
        });

    }
}