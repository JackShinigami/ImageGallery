package com.example.imagegallery;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AlbumHelper {

    private static Set<String> defaultAlbums;
    private static AlbumHelper albumHelper;

    private AlbumHelper(){
    }

    public static AlbumHelper getInstance(){
        if(albumHelper == null){
            albumHelper = new AlbumHelper();
            defaultAlbums = new HashSet<>();
        }
        return albumHelper;
    }

    public void addDefaultAlbum(String albumName){
        defaultAlbums.add(albumName);
    }

    public boolean isDefaultAlbum(String albumName){
        return defaultAlbums.contains(albumName);
    }

    public static void addImgaeToAlbum(Context context, ImageObject imageObject){
        ArrayList<String> albumNameList = SharedPreferencesManager.loadAlbumNameList(context);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, albumNameList);
        if(albumNameList == null){
            albumNameList = new ArrayList<>();
        }

        View view = LayoutInflater.from(context).inflate(R.layout.add_image_to_album, null);
        AutoCompleteTextView editName = view.findViewById(R.id.edit_album_name);
        editName.setAdapter(arrayAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        ArrayList<String> finalAlbumNameList = albumNameList;
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String albumName = editName.getText().toString();
                if(albumName.length() != 0){
                    if(finalAlbumNameList.contains(albumName)){
                        AlbumData albumData = SharedPreferencesManager.loadAlbumData(context, albumName);
                       if(albumData.addImage(imageObject)){
                           SharedPreferencesManager.saveAlbumData(context, albumData);
                           imageObject.addAlbumName(context,albumName);
                           Toast.makeText(context, "Image has been added to " + albumName, Toast.LENGTH_SHORT).show();
                       }
                       else{
                           Toast.makeText(context, "Image already exists in this album", Toast.LENGTH_SHORT).show();
                       }
                    }
                    else{
                        AlbumData albumData = new AlbumData(albumName);
                        albumData.addImage(imageObject);
                        SharedPreferencesManager.saveAlbumData(context, albumData);
                        imageObject.addAlbumName(context,albumName);
                        finalAlbumNameList.add(albumName);
                        SharedPreferencesManager.saveAlbumNameList(context, finalAlbumNameList);
                        Toast.makeText(context, "Image has been added to " + albumName, Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(context, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create();
        builder.show();
    }


    public static void removeImageFromAlbum(Context context, ImageObject imageObject){
        MainActivity mainActivity = (MainActivity) context;
        String albumName = mainActivity.getCurrentFragementName();
        AlbumData albumData = SharedPreferencesManager.loadAlbumData(context, albumName);

        if(albumData.deleteImage(imageObject)){
            SharedPreferencesManager.saveAlbumData(context, albumData);
            Toast.makeText(context, "Image has been deleted from " + albumName, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context, "Image does not exist in this album", Toast.LENGTH_SHORT).show();
        }
    }

    public static ArrayList<AlbumData> createDefaultAlbum(Context context){
        ArrayList<AlbumData> albums = new ArrayList<>();

        File externalStorage = Environment.getExternalStorageDirectory();
        File trashDirectory = new File(externalStorage, "Trash");
        ArrayList<ImageObject> trashImages = new ArrayList<>();
        if(!trashDirectory.exists()) {
            trashDirectory.mkdir();
        }
        else{
            trashImages = new ArrayList<>();
            ImageObject.getImage(context, trashDirectory, trashImages);
        }

        AlbumHelper albumHelper = AlbumHelper.getInstance();

        AlbumData trash = new AlbumData("Trash", trashImages, R.drawable.ic_trash);
        albumHelper.addDefaultAlbum(trash.getAlbumName());
        albums.add(trash);
        AlbumData favorite = SharedPreferencesManager.loadAlbumData(context, "Favorites");
        if(favorite == null){
            favorite = new AlbumData("Favorites", R.drawable.ic_favorite);
            albumHelper.addDefaultAlbum(favorite.getAlbumName());
            SharedPreferencesManager.saveAlbumData(context, favorite);
        }
        albums.add(favorite);

        SharedPreferencesManager.saveAlbumData(context, new AlbumData("Trash", trashImages));
        return albums;
    }

    public static void setAlbumPassword(Context context, String albumName){
        if(SharedPreferencesManager.hasSetPassword(context, albumName)){
            checkAlbumPassword(context, albumName, new PasswordCheckCallBack() {
                @Override
                public void onPasswordChecked(boolean isPasswordCorrect) {
                    if(isPasswordCorrect){
                        View view = LayoutInflater.from(context).inflate(R.layout.set_password, null);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setView(view);
                        EditText editPassword = view.findViewById(R.id.edit_password);
                        EditText editConfirmPassword = view.findViewById(R.id.retype_password);

                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(editPassword.getText().toString().equals(editConfirmPassword.getText().toString())){
                                    SharedPreferencesManager.saveAlbumPassword(context, albumName, editPassword.getText().toString());
                                    Toast.makeText(context, "Password has been set", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(context, "Password does not match", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        builder.setNegativeButton("Cancel", null);
                        builder.create();
                        builder.show();
                    }
                }
            });
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.set_password, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(view);
            EditText editPassword = view.findViewById(R.id.edit_password);
            EditText editConfirmPassword = view.findViewById(R.id.retype_password);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(editPassword.getText().toString().equals(editConfirmPassword.getText().toString())){
                        SharedPreferencesManager.saveAlbumPassword(context, albumName, editPassword.getText().toString());
                        Toast.makeText(context, "Password has been set", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(context, "Password does not match", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            builder.setNegativeButton("Cancel", null);
            builder.create();
            builder.show();
        }

    }

    public static void checkAlbumPassword(Context context, String albumName, PasswordCheckCallBack passwordCheckCallBack){

        if(SharedPreferencesManager.hasSetPassword(context, albumName)){
            View view = LayoutInflater.from(context).inflate(R.layout.set_password, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(view);
            EditText editPassword = view.findViewById(R.id.edit_password);
            TextView title = view.findViewById(R.id.txtTitle);
            LinearLayout retypePassword = view.findViewById(R.id.retype_password_layout);
            title.setText("Enter Current Password");
            retypePassword.setVisibility(View.GONE);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(SharedPreferencesManager.checkAlbumPassword(context, albumName, editPassword.getText().toString())){
                        Toast.makeText(context, "Password is correct", Toast.LENGTH_SHORT).show();
                        passwordCheckCallBack.onPasswordChecked(true);
                    }
                    else{
                        Toast.makeText(context, "Password is incorrect", Toast.LENGTH_SHORT).show();
                        passwordCheckCallBack.onPasswordChecked(false);

                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    passwordCheckCallBack.onPasswordChecked(false);
                }
            });
            builder.create();
            builder.show();


        }
        else{
            passwordCheckCallBack.onPasswordChecked(true);
        }


    }


}
