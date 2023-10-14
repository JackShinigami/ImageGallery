package com.example.imagegallery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

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
}
