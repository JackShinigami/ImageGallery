package com.example.imagegallery;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    public static enum SEARCH_TYPE{
        NAME,
        LOCATION
    }
    ArrayList<ImageObject> images = new ArrayList<>();
    ArrayList<ImageObject> newImages = new ArrayList<>();
    FragmentManager fragmentManager = getSupportFragmentManager();
    AutoCompleteTextView search_edit_text;
    SEARCH_TYPE search_type = SEARCH_TYPE.NAME;

    private static ArrayList<ImageObject> deleteImages = new ArrayList<>();
    private static boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        isRunning = true;
        images = getIntent().getParcelableArrayListExtra("images");
        Log.d("SearchActivity", "onCreate: " + images.size());


        FragmentTransaction ft = fragmentManager.beginTransaction();
        ImageFragment imageFragment = ImageFragment.newInstance(newImages, "Search");
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
            newImages = new ArrayList<>();
            for(ImageObject imageObject : images){
                if(search_type == SEARCH_TYPE.NAME) {
                    if (imageObject.getFileName().equals(name)) {
                        newImages.add(imageObject);
                    }
                }
            }

            search_edit_text.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(search_edit_text.getWindowToken(), 0);

            FragmentTransaction ft1 = fragmentManager.beginTransaction();
            ImageFragment imageFragment1 = ImageFragment.newInstance(newImages, "Search");
            ft1.replace(R.id.fragment_container, imageFragment1);
            ft1.commit();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(deleteImages.size() > 0){
            for(ImageObject imageObject : deleteImages){
                for(ImageObject imageObject1 : newImages){
                    if(imageObject.getFilePath().equals(imageObject1.getFilePath())){
                        newImages.remove(imageObject1);
                        break;
                    }
                }

                for(ImageObject imageObject1 : images){
                    if(imageObject.getFilePath().equals(imageObject1.getFilePath())){
                        images.remove(imageObject1);
                        break;
                    }
                }
            }
            deleteImages.clear();

            ArrayList<String> data = new ArrayList<>();
            if(search_type == SEARCH_TYPE.NAME) {
                for (ImageObject imageObject : images) {
                    data.add(imageObject.getFileName());
                }
            }
            else{
            }
            search_edit_text.setAdapter(new AutoCompleteAdapter(data));

            FragmentTransaction ft1 = fragmentManager.beginTransaction();
            ImageFragment imageFragment1 = ImageFragment.newInstance(newImages, "Search");
            ft1.replace(R.id.fragment_container, imageFragment1);
            ft1.commit();
        }
    }

    public static void addDeleteImage(ImageObject imageObject){
        deleteImages.add(imageObject);
    }

    public static boolean isSearchActivityRunning(){
        return isRunning;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        deleteImages.clear();
    }
}