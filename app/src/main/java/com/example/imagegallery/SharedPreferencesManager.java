package com.example.imagegallery;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

public class SharedPreferencesManager {
    private static final String SHARED_PREFERENCES_NAME = "Image_Gallery_Data";
    private static final String ALBUM_LIST = "albumList";
    public static void saveAlbumData(Context context, AlbumData albumData) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(albumData);
        editor.putString(albumData.getAlbumName(), json);
        editor.apply();
    }

    public static AlbumData loadAlbumData(Context context, String albumName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(albumName, null);
        AlbumData albumData = gson.fromJson(json, AlbumData.class);
        return albumData;
    }

    public static void saveAlbumNameList(Context context, ArrayList<String> albumList) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(albumList);
        editor.putString(ALBUM_LIST, json);
        editor.apply();
    }

    public static ArrayList<String> loadAlbumNameList(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(ALBUM_LIST, null);
        ArrayList<String> albumList = gson.fromJson(json, ArrayList.class);
        return albumList;
    }

    public static void deleteAlbumData(Context context, String albumName) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.remove(albumName);
        editor.apply();
    }
}
