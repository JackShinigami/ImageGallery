package com.example.imagegallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchActivity extends AppCompatActivity {

    public static enum SEARCH_TYPE{
        NAME,
        LOCATION,
        TAG
    }
    ArrayList<ImageObject> images = new ArrayList<>();
    ArrayList<ImageObject> newImages = new ArrayList<>();
    ArrayList<String> data = new ArrayList<>();
    FragmentManager fragmentManager = getSupportFragmentManager();

    ImagesViewModel imagesViewModel;
    AutoCompleteTextView search_edit_text;
    ImageButton btnSearchOptions;
    SEARCH_TYPE search_type = SEARCH_TYPE.NAME;

    private static ArrayList<ImageObject> deleteImages = new ArrayList<>();
    private static boolean isRunning = false;
    private boolean loaded = false;
    private Thread background;
    private boolean isLoading = false;
    private boolean isGettingData = false;
    private boolean isSearching = false;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            if(msg.what == 0)
            {
                loaded = true;
                isLoading = false;
                background.interrupt();
                showPopupMenu();
            }
            else if(msg.what == 1)
            {
                search_edit_text.setAdapter(new AutoCompleteAdapter(data));
                dialog_loading.dismiss();
                isGettingData = false;
            }
            else if(msg.what == 2)
            {
                imagesViewModel.setImagesAlbum(newImages);
                imageFragment.setFragmentAdapter(newImages, getApplicationContext());
                dialog_loading.dismiss();
                isSearching = false;
            }
        };
    };
    private  TaskCompletionSource<Void> taskCompletionSource;
    private Dialog dialog_loading;
    private ImageFragment imageFragment;
    private final int MAXLOADING = 50;
    private int tagCount = 0;
    private PopupMenu popupMenu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        imagesViewModel = new ViewModelProvider(this).get(ImagesViewModel.class);
        isRunning = true;
        images = imagesViewModel.getImagesList().getValue();
        Log.d("SearchActivity", "onCreate: " + images.size());

        background = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AtomicInteger count = new AtomicInteger();
                    for (ImageObject imageObject : images) {
                        if(!isLoading)
                            break;
                        try {
                            if(!imageObject.isLocationLoaded()) {
                                imageObject.loadLatLong(getApplicationContext());
                            }

                        } catch (Exception e) {
                            Log.e("Exif", e.toString());
                        }
                    }

                    count.set(0);
                    for(ImageObject imageObject : images){
                        try {
                            if (!isLoading || count.get() >= MAXLOADING)
                                break;
                            taskCompletionSource = new TaskCompletionSource<>();
                            imageObject.getTags(getApplicationContext(), taskCompletionSource);
                            taskCompletionSource.getTask().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    count.getAndIncrement();
                                }
                            });
                            tagCount++;
                        }
                        catch (Exception e)
                        {
                            Log.e("Thread", e.toString());
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

        isLoading = true;
        background.start();

        imagesViewModel.setImagesAlbum(newImages);

        FragmentTransaction ft = fragmentManager.beginTransaction();
        imageFragment = ImageFragment.newInstance( "Search");
        ft.replace(R.id.fragment_container, imageFragment);
        ft.commit();

        btnSearchOptions = findViewById(R.id.btnSearchOptions);
        btnSearchOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu();
            }
        });


        search_edit_text = findViewById(R.id.search_edit_text);
        search_edit_text.setThreshold(1);
        updateDataSearch();

        search_edit_text.setOnItemClickListener((adapterView, view, i, l) -> {
            search_edit_text.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(search_edit_text.getWindowToken(), 0);
            dialog_loading = new Dialog(this);
            dialog_loading.setContentView(R.layout.dialog_loading);
            dialog_loading.setCancelable(false);
            dialog_loading.show();
            Thread searchThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        isSearching = true;
                        String name = adapterView.getItemAtPosition(i).toString();
                        newImages = new ArrayList<>();
                        int count = 0;
                        for(ImageObject imageObject : images){
                            if(!isSearching)
                                break;

                            if(search_type == SEARCH_TYPE.NAME) {
                                if (imageObject.getFileName().equals(name)) {
                                    newImages.add(imageObject);
                                }
                            }
                            else if(search_type == SEARCH_TYPE.LOCATION){
                                if(imageObject.getAddress(getApplicationContext()).equals(name)){
                                    newImages.add(imageObject);
                                }
                            }else if(search_type == SEARCH_TYPE.TAG){
                                if(count >= tagCount)
                                    break;
                                taskCompletionSource = new TaskCompletionSource<>();
                                try {
                                    if (imageObject.getTags(getApplicationContext(), taskCompletionSource).contains(name)) {
                                        newImages.add(imageObject);
                                    }
                                    count++;
                                }
                                catch (Exception e)
                                {
                                    Log.e("Search_Tag", e.toString());
                                }
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        Log.e("Thread", e.toString());
                    }
                    handler.sendEmptyMessage(2);
                }
            });

            searchThread.start();
        });
    }

    private void showPopupMenu()
    {
        PopupMenu popupMenu = new PopupMenu(SearchActivity.this, btnSearchOptions);
        popupMenu.getMenuInflater().inflate(R.menu.search_menu, popupMenu.getMenu());


        if(!loaded)
        {
            popupMenu.getMenu().findItem(R.id.search_location).setEnabled(false);
            popupMenu.getMenu().findItem(R.id.search_tag).setEnabled(false);
        }
        else {
            popupMenu.getMenu().findItem(R.id.search_location).setEnabled(true);
            popupMenu.getMenu().findItem(R.id.search_tag).setEnabled(true);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int id = menuItem.getItemId();
                if(id == R.id.search_name)
                {
                    search_type = SEARCH_TYPE.NAME;
                    btnSearchOptions.setImageResource(R.drawable.ic_search_by_name);
                    updateDataSearch();
                }
                else if(id == R.id.search_location)
                {
                    search_type = SEARCH_TYPE.LOCATION;
                    btnSearchOptions.setImageResource(R.drawable.ic_search_by_location);
                    updateDataSearch();
                }
                else if(id == R.id.search_tag)
                {
                    search_type = SEARCH_TYPE.TAG;
                    btnSearchOptions.setImageResource(R.drawable.ic_search_by_tag);
                    updateDataSearch();
                }
                return false;
            }
        });
        popupMenu.show();
    }
    private void updateDataSearch()
    {
        dialog_loading = new Dialog(this);
        dialog_loading.setContentView(R.layout.dialog_loading);
        dialog_loading.setCancelable(false);
        dialog_loading.show();
        Thread updateData = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    isGettingData = true;
                    data.clear();
                    int count = 0;

                    if(search_type == SEARCH_TYPE.NAME) {
                        for (ImageObject imageObject : images) {
                            if(!isGettingData)
                                break;
                            data.add(imageObject.getFileName());
                        }
                    }
                    else if(search_type == SEARCH_TYPE.LOCATION){
                        for (ImageObject imageObject : images) {
                            if(!isGettingData)
                                break;
                            String address = imageObject.getAddress(getApplicationContext());
                            if(!data.contains(address) ){
                                data.add(address);
                            }
                        }
                    }else if(search_type == SEARCH_TYPE.TAG){
                        for (ImageObject imageObject : images) {
                            if(!isGettingData || count >= tagCount)
                                break;
                            try {
                                taskCompletionSource = new TaskCompletionSource<>();
                                ArrayList<String> tags = imageObject.getTags(getApplicationContext(), taskCompletionSource);
                                for (String tag : tags) {
                                    if (!data.contains(tag)) {
                                        data.add(tag);
                                    }
                                }
                                count++;
                            }
                            catch (Exception e)
                            {
                                Log.e("Search_Tag", e.toString());

                            }
                        }
                        Log.d("SearchActivity", "updateDataSearch: " + data.size());
                    }
                }
                catch (Exception e)
                {
                    Log.e("Thread", e.toString());
                }
                handler.sendEmptyMessage(1);
            }
        });
        updateData.start();
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

            newImages = imagesViewModel.getImagesAlbum().getValue();
            imageFragment.setFragmentAdapter(newImages, getApplicationContext());
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
        isLoading = false;
        isSearching = false;
        isGettingData = false;
        deleteImages.clear();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isRunning = false;
        isLoading = false;
        isSearching = false;
        isGettingData = false;
        deleteImages.clear();
        finish();
    }
}