package com.example.imagegallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.gson.Gson;

import java.util.ArrayList;

public class SharedPreferencesManager {
    private static final String SHARED_PREFERENCES_NAME = "ImageGalleryData_21112003";
    private static final String ALBUM_LIST = "albumList21112003";
    private static final String CURRENT_STATE = "currentState21112003";
    private static final String CURRENT_IMAGES = "currentImages21112003";
    private static final String CURRENT_NAME = "currentName21112003";
    private static final String CURRENT_ITEM_POSITION = "currentItemPosition21112003";

    private static final String IMAGE_LIST = "imageList21112003";
    public static Bundle image_Album = new Bundle();
    public static Bundle trash_list = new Bundle();
    private static final String TRASH_LIST = "trashList21112003";
    private static final String TRASH_IMAGES = "trashImages21112003";
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
        ArrayList<String> albumList = loadAlbumNameList(context);
        albumList.remove(albumName);
        saveAlbumNameList(context, albumList);
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.remove(albumName);
        editor.apply();
    }


    public static void saveImageAlbumInfo(Context context, ImageObject imageObject) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();

        image_Album.putStringArrayList(imageObject.getFilePath(), imageObject.getAlbumNames());
        String json = gson.toJson(image_Album);
        editor.putString(IMAGE_LIST, json);
        editor.apply();
    }

    public static ArrayList<String> loadImageAlbumInfo(Context context, String filePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(IMAGE_LIST, null);
        if(json == null)
            return null;
        image_Album = gson.fromJson(json, Bundle.class);
        ArrayList<String> albumNames = image_Album.getStringArrayList(filePath);
        return albumNames;
    }

    public static void deleteImageAlbumInfo(Context context, ImageObject imageObject) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(IMAGE_LIST, null);
        image_Album = gson.fromJson(json, Bundle.class);
        image_Album.remove(imageObject.getFilePath());
        json = gson.toJson(image_Album);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(IMAGE_LIST, json);
        editor.apply();
    }

    public static void saveTrashFile(Context context, String newPath, String oldPath) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();

        trash_list.putString(newPath, oldPath);
        String json = gson.toJson(trash_list);
        editor.putString(TRASH_LIST, json);
        editor.apply();
    }

    public static String loadTrashFile(Context context, String newPath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(TRASH_LIST, null);
        trash_list = gson.fromJson(json, Bundle.class);
        String oldPath = trash_list.getString(newPath);
        return oldPath;
    }

    public static void deleteTrashFile(Context context, String newPath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(TRASH_LIST, null);
        trash_list = gson.fromJson(json, Bundle.class);
        trash_list.remove(newPath);
        json = gson.toJson(trash_list);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TRASH_LIST, json);
        editor.apply();
    }


    public static void saveStateFragment(Context context, int state) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(CURRENT_STATE, state);
        editor.apply();
    }

    public static int loadStateFragment(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int state = sharedPreferences.getInt(CURRENT_STATE, 0);
        return state;
    }

    public static void saveCurrentImages(Context context, ArrayList<ImageObject> images) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(images);
        editor.putString(CURRENT_IMAGES, json);
        editor.apply();
    }

    public static ArrayList<ImageObject> loadCurrentImages(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(CURRENT_IMAGES, null);
        ArrayList<ImageObject> images = gson.fromJson(json, ArrayList.class);
        return images;
    }

    public static void saveCurrentName(Context context, String name) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(CURRENT_NAME, name);
        editor.apply();
    }

    public static String loadCurrentName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(CURRENT_NAME, null);
        return name;
    }

    public static void saveCurrentItemPosition(Context context, int position) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(CURRENT_ITEM_POSITION, position);
        editor.apply();
    }

    public static int loadCurrentItemPosition(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int position = sharedPreferences.getInt(CURRENT_ITEM_POSITION, 0);
        return position;
    }
}
