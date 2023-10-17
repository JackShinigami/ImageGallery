package com.example.imagegallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class AlbumHelper {

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
            for (ImageObject imageFile : trashImages) {
                Log.d("TRASH", imageFile.getFilePath());
            }
        }


        AlbumData trash = new AlbumData("Trash", R.drawable.ic_trash, true);
        albums.add(trash);
        AlbumData favorite = SharedPreferencesManager.loadAlbumData(context, "Favorites");

        albums.add(favorite);

        SharedPreferencesManager.saveAlbumData(context, new AlbumData("Trash", trashImages));
        return albums;
    }

}
