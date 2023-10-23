package com.example.imagegallery;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    public static enum SEARCH_TYPE{
        NAME,
        LOCATION
    }
    ArrayList<ImageObject> images = new ArrayList<>();
    ArrayList<ImageObject> newImages = new ArrayList<>();
    ArrayList<String> data = new ArrayList<>();
    FragmentManager fragmentManager = getSupportFragmentManager();
    AutoCompleteTextView search_edit_text;
    TextView txtSearchOptions;
    SEARCH_TYPE search_type = SEARCH_TYPE.NAME;

    private static ArrayList<ImageObject> deleteImages = new ArrayList<>();
    private static boolean isRunning = false;

    private Thread background;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            if(msg.what == 0)
            {
                background.interrupt();
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        isRunning = true;
        images = getIntent().getParcelableArrayListExtra("images");
        Log.d("SearchActivity", "onCreate: " + images.size());

        background = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (ImageObject imageObject : images) {
                        try {
                            ExifInterface exif = new ExifInterface(imageObject.getFilePath());

                            float[] latLong = new float[2];
                            if (exif.getLatLong(latLong)) {
                                imageObject.setLatLong(latLong);
                                Log.d("ImageObject", imageObject.getFilePath() + " " + imageObject.getAddress(getApplicationContext()));
                            } else {
                                Log.d("ImageObject", "lat: null long: null");
                                imageObject.setLatLong(null);
                            }

                        } catch (Exception e) {
                            Log.e("Exif", e.toString());
                        }
                    }
                }
                catch (Exception e)
                {
                    Log.e("Thread", e.toString());
                }
                handler.sendEmptyMessage(0);
            }
        });
        background.start();

        FragmentTransaction ft = fragmentManager.beginTransaction();
        ImageFragment imageFragment = ImageFragment.newInstance(newImages, "Search");
        ft.replace(R.id.fragment_container, imageFragment);
        ft.commit();

        txtSearchOptions = findViewById(R.id.txtSearchOptions);
        txtSearchOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(SearchActivity.this, txtSearchOptions);
                popupMenu.getMenuInflater().inflate(R.menu.search_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        if(id == R.id.search_name)
                        {
                            search_type = SEARCH_TYPE.NAME;
                            txtSearchOptions.setText("Name");
                            updateDataSearch();
                        }
                        else if(id == R.id.search_location)
                        {
                            search_type = SEARCH_TYPE.LOCATION;
                            txtSearchOptions.setText("Location");
                            updateDataSearch();
                        }

                        return false;
                    }
                });
                popupMenu.show();
            }
        });


        search_edit_text = findViewById(R.id.search_edit_text);
        search_edit_text.setThreshold(1);
        updateDataSearch();
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
                else if(search_type == SEARCH_TYPE.LOCATION){
                    if(imageObject.getAddress(this).equals(name)){
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

    private void updateDataSearch()
    {
        data.clear();
        if(search_type == SEARCH_TYPE.NAME) {
            for (ImageObject imageObject : images) {
                data.add(imageObject.getFileName());
            }
        }
        else if(search_type == SEARCH_TYPE.LOCATION){
            for (ImageObject imageObject : images) {
                String address = imageObject.getAddress(this);
                if(!data.contains(address) ){
                    data.add(address);
                }
            }
        }
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

            updateDataSearch();
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