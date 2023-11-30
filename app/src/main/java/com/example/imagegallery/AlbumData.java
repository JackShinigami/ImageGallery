package com.example.imagegallery;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

//<--implement Parcelable-->

public class AlbumData implements Parcelable {
    private ArrayList<ImageObject> images;
    private String albumName;
    private long createdDate;
    private long lastModifiedDate;

    private int thumbnailPath;


    public AlbumData(String albumName, ArrayList<ImageObject> images, int thumbnailPath){
        long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        this.albumName = albumName;
        this.images = images;
        this.createdDate = now;
        this.lastModifiedDate = now;
        this.thumbnailPath = thumbnailPath;
    }



    public AlbumData(String albumName){
        long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        this.albumName = albumName;
        this.images = new ArrayList<>();
        this.createdDate = now;
        this.lastModifiedDate = now;
        this.thumbnailPath = R.drawable.icon_albums;
    }

    public AlbumData(String albumName, int thumbnailPath){
        long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
        this.albumName = albumName;
        this.images = new ArrayList<>();
        this.createdDate = now;
        this.lastModifiedDate = now;
        this.thumbnailPath = thumbnailPath;

    }

    protected AlbumData(Parcel in) {
        images = in.createTypedArrayList(ImageObject.CREATOR);
        albumName = in.readString();
        createdDate = in.readLong();
        lastModifiedDate = in.readLong();
    }

    public static final Creator<AlbumData> CREATOR = new Creator<AlbumData>() {
        @Override
        public AlbumData createFromParcel(Parcel in) {
            return new AlbumData(in);
        }

        @Override
        public AlbumData[] newArray(int size) {
            return new AlbumData[size];
        }
    };

    public boolean addImage(ImageObject image){

        for(ImageObject imageObject : this.images){
            if(imageObject.getFilePath().equals(image.getFilePath())){
                return false;
            }
        }

        this.images.add(image);
        return true;
    }

    public void removeImage(ImageObject image){
        for(ImageObject imageObject : this.images){
            if(imageObject.getFilePath().equals(image.getFilePath())){
                this.images.remove(imageObject);
                return;
            }
        }
    }

    public void setImages(ArrayList<ImageObject> images){
        this.images = images;
    }

    public ArrayList<ImageObject> getImages(){
        return this.images;
    }

    public void setAlbumName(String albumName){
        this.albumName = albumName;
    }

    public String getAlbumName(){
        return this.albumName;
    }


    public long getCreatedDate(){
        return this.createdDate;
    }

    public int getThumbnailPath(){
        return this.thumbnailPath;
    }

    public void setThumbnailPath(int thumbnailPath){
        this.thumbnailPath = thumbnailPath;
    }

    public void setLastModifiedDate(){

        this.lastModifiedDate = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
    }

    public long getLastModifiedDate(){
        return this.lastModifiedDate;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedList(images);
        dest.writeString(albumName);
        dest.writeSerializable(createdDate);
        dest.writeSerializable(lastModifiedDate);

    }


    public boolean deleteImage(ImageObject imageObject) {
        for(ImageObject image : this.images){
            if(image.getFilePath().equals(imageObject.getFilePath())){
                this.images.remove(image);
                return true;
            }
        }
        return false;
    }

    public static void sortAlbumByName(ArrayList<AlbumData> albums, boolean ascending) {
        if(ascending) {
            albums.sort((o1, o2) -> o1.getAlbumName().compareTo(o2.getAlbumName()));
        }
        else {
            albums.sort((o1, o2) -> o2.getAlbumName().compareTo(o1.getAlbumName()));
        }
    }

    public static void sortAlbumByDate(ArrayList<AlbumData> albums, boolean ascending) {

        if(ascending){
            albums.sort(Comparator.comparing(o -> new Date(o.getCreatedDate())));
        }
        else {
            albums.sort((o1, o2) -> new Date(o2.getCreatedDate()).compareTo(new Date(o1.getCreatedDate())));

        }
    }

    public void cleanUnexistingImages(Context context){
        ArrayList<ImageObject> newImages = new ArrayList<>();
        for(ImageObject image : this.images){
            File file = new File(image.getFilePath());
            if(file.exists()){
                newImages.add(image);
            }
        }
        this.images = newImages;
        SharedPreferencesManager.saveAlbumData(context, this);
    }
}
