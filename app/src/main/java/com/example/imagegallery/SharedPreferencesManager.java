package com.example.imagegallery;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;


import com.google.gson.Gson;

import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalTime;
import java.util.ArrayList;

public class SharedPreferencesManager {
    private static final String SHARED_PREFERENCES_NAME = "ImageGalleryData_21112003";
    private static final String ALBUM_LIST = "albumList21112003";
    private static final String CURRENT_STATE = "currentState21112003";
    private static final String CURRENT_THEME = "currentTheme21112003";
    private static final String CURRENT_IMAGES = "currentImages21112003";
    private static final String CURRENT_NAME = "currentName21112003";
    private static final String CURRENT_LANGUAGE = "currentLanguage21112003";
    private static final String CURRENT_ITEM_POSITION = "currentItemPosition21112003";

    private static final String IMAGE_LIST = "imageList21112003";
    private static final String ALBUM_PASSWORD = "albumPassword21112003";

    public static Bundle image_Album = new Bundle();
    public static Bundle trash_list = new Bundle();
    public static Bundle love_images = new Bundle();
    public static  Bundle album_passwords = new Bundle();
    private static Bundle image_tags = new Bundle();
    private static final String TRASH_LIST = "trashList21112003";
    private static final String LOVE_INFO = "loveInfo21112003";
    private static final String IS_SET_SECURITY_QUESTION = "isSetSecurityQuestion21112003";
    private static final String ENTER_WRONG_ANSWER = "enterWrongAnswer21112003";
    private static final String TIME_ENTER_WRONG_ANSWER = "timeEnterWrongAnswer21112003";

    private static final String IMAGE_TAGS = "imageTags21112003";

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
        AlbumData albumData;
        try{
            albumData = gson.fromJson(json, AlbumData.class);
        }
        catch (Exception e){
            albumData = null;
        }
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


    public static void saveImageAlbumInfo(Context context, String filePath, ArrayList<String> albumNames) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();

        image_Album.putStringArrayList(filePath, albumNames);

        String json = gson.toJson(image_Album);
        editor.putString(IMAGE_LIST, json);
        editor.apply();
    }

    public static ArrayList<String> loadImageAlbumInfo(Context context, String filePath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(IMAGE_LIST, null);
        if(json == null)
            return new ArrayList<>();

        image_Album = gson.fromJson(json, Bundle.class);
        ArrayList<String> albumNames = image_Album.getStringArrayList(filePath);
        if(albumNames == null)
            return new ArrayList<>();

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

    public static void saveTrashFile(Context context, String newPath, ImageObject imageObject) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();

        String oldObject = gson.toJson(imageObject);
        trash_list.putString(newPath, oldObject);

        String json = gson.toJson(trash_list);
        editor.putString(TRASH_LIST, json);
        editor.apply();
    }

    public static ImageObject loadTrashFile(Context context, String newPath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(TRASH_LIST, null);
        if(json == null)
            return null;
        trash_list = gson.fromJson(json, Bundle.class);
        String oldObject = trash_list.getString(newPath);
        ImageObject res = gson.fromJson(oldObject, ImageObject.class);
        return res;
    }

    public static void deleteTrashFile(Context context, String newPath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(TRASH_LIST, null);
        if(json == null)
            return;
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

    public static void saveThemeState(Context context, int state) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(CURRENT_THEME, state);
        editor.apply();
    }

    public static int loadThemeState(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int state = sharedPreferences.getInt(CURRENT_THEME, 0);
        return state;
    }

    public static void saveLanguageState(Context context, int state) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(CURRENT_LANGUAGE, state);
        editor.apply();
    }

    public static int loadLanguageState(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int state = sharedPreferences.getInt(CURRENT_LANGUAGE, 0);
        return state;
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
        if(json == null)
            return new ArrayList<>();
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

    public static void saveLovedImages(Context context, String filepath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        love_images.putBoolean(filepath, true);
        Gson gson = new Gson();
        String json = gson.toJson(love_images);
        editor.putString(LOVE_INFO, json);
        editor.apply();
    }

    public static boolean isLovedImages(Context context, String filepath) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(LOVE_INFO, null);

        if(json == null)
            return false;

        love_images = gson.fromJson(json, Bundle.class);

        if (love_images.getBoolean(filepath))
            return true;
        return false;
    }

    public static void deleteLovedImages(Context context, String filepath){
        SharedPreferences.Editor sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        love_images.remove(filepath);
        Gson gson = new Gson();
        String json = gson.toJson(love_images);
        sharedPreferences.putString(LOVE_INFO, json);
        sharedPreferences.apply();
    }

    public static void saveAlbumPassword(Context context, String ALbumName, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        album_passwords.putString(ALbumName, hashedPassword);
        Gson gson = new Gson();
        String json = gson.toJson(album_passwords);
        editor.putString(ALBUM_PASSWORD, json);
        editor.apply();
        //todo update album name for album password when  change album name or delete album name
    }

    public static boolean hasSetPassword(Context context, String ALbumName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(ALBUM_PASSWORD, null);
        if(json == null)
            return false;
        album_passwords = gson.fromJson(json, Bundle.class);
        String storedHashedPassword = album_passwords.getString(ALbumName);
        if(storedHashedPassword == null)
            return false;
        return true;
    }

    public static boolean checkAlbumPassword(Context context, String ALbumName, String enteredPassword) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(ALBUM_PASSWORD, null);
        if(json == null)
            return false;
        album_passwords = gson.fromJson(json, Bundle.class);
        String storedHashedPassword = album_passwords.getString(ALbumName);

        if(storedHashedPassword == null)
            return false;

        if (BCrypt.checkpw(enteredPassword, storedHashedPassword)) {
            return true;
        } else {
            return false;
        }

    }

    public static void updateAlbumNameInPassword(Context context, String oldAlbumName, String newAlbumName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(ALBUM_PASSWORD, null);
        if(json == null)
            return;
        album_passwords = gson.fromJson(json, Bundle.class);
        String storedHashedPassword = album_passwords.getString(oldAlbumName);
        if(storedHashedPassword == null)
            return;
        album_passwords.putString(newAlbumName, storedHashedPassword);
        album_passwords.remove(oldAlbumName);
        json = gson.toJson(album_passwords);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ALBUM_PASSWORD, json);
        editor.apply();
    }

    public static void deleteAlbumPassword(Context context, String ALbumName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(ALBUM_PASSWORD, null);
        if(json == null)
            return;
        album_passwords = gson.fromJson(json, Bundle.class);
        album_passwords.remove(ALbumName);
        json = gson.toJson(album_passwords);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ALBUM_PASSWORD, json);
        editor.apply();

    }


    public static void saveSecurityQuestion(Context context, String question, String answer) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        String hashedAnswer = BCrypt.hashpw(answer, BCrypt.gensalt());
        editor.putString(question, hashedAnswer);
        editor.apply();

        Gson gson = new Gson();
        String json = gson.toJson(true);
        editor.putString(IS_SET_SECURITY_QUESTION, json);
        editor.apply();
    }

    public static boolean checkSecurityQuestion(Context context, String question, String answer){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String hashedAnswer = sharedPreferences.getString(question, null);
        if(hashedAnswer == null)
            return false;
        if (BCrypt.checkpw(answer, hashedAnswer)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSecurityQuestionSet(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String json = sharedPreferences.getString(IS_SET_SECURITY_QUESTION, null);
        if(json == null)
            return false;
        Gson gson = new Gson();
        boolean isSet = gson.fromJson(json, Boolean.class);
        return isSet;
    }

    public static void saveEnterWrongAnswerTimes(Context context,int count){
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        editor.putInt(ENTER_WRONG_ANSWER, count);
        editor.apply();
    }

    public static int getEnterWrongAnswerTimes(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        int count = sharedPreferences.getInt(ENTER_WRONG_ANSWER, 0);
        return count;
    }

    public static void saveTimeEnterWrongAnswer(Context context){
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        long time = System.currentTimeMillis();
        editor.putLong(TIME_ENTER_WRONG_ANSWER, time);
        editor.apply();
    }

    public static long getTimeEnterWrongAnswer(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        long time = sharedPreferences.getLong(TIME_ENTER_WRONG_ANSWER, 0);
        return time;
    }

    public static void saveTagsForImage(Context context, String filePath, ArrayList<String> tags){
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        image_tags.putStringArrayList(filePath, tags);
        String json = gson.toJson(image_tags);
        editor.putString(IMAGE_TAGS, json);
        editor.apply();
    }
    public static void addTagForImage(Context context, String filePath, String tag){
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        ArrayList<String> tags = image_tags.getStringArrayList(filePath);
        if(tags == null)
            tags = new ArrayList<>();
        tags.add(tag);
        image_tags.putStringArrayList(filePath, tags);
        String json = gson.toJson(image_tags);
        editor.putString(IMAGE_TAGS, json);
        editor.apply();
    }

    public static ArrayList<String> loadTagsForImage(Context context, String filePath){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(IMAGE_TAGS, null);
        if(json == null)
            return null;
        image_tags = gson.fromJson(json, Bundle.class);
        ArrayList<String> tags = image_tags.getStringArrayList(filePath);
        return tags;
    }

    public static void removeTagForImage(Context context, String filePath, String tag){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        ArrayList<String> tags = image_tags.getStringArrayList(filePath);
        if(tags == null)
            return;
        tags.remove(tag);
        image_tags.putStringArrayList(filePath, tags);
        String json = gson.toJson(image_tags);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(IMAGE_TAGS, json);
        editor.apply();
    }

    public static void deleteTagsForImage(Context context, String filePath){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        image_tags.remove(filePath);
        String json = gson.toJson(image_tags);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(IMAGE_TAGS, json);
        editor.apply();
    }

    public static void addTimeEditedImage(Context context, String filePath){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(filePath, LocalTime.now().toString());
        editor.apply();
    }

    public static String getTimeEditedImage(Context context, String filePath){
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        String time = sharedPreferences.getString(filePath, null);
        return time;
    }
}
