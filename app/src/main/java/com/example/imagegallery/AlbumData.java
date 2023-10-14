package com.example.imagegallery;

import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.time.LocalDateTime;
import java.util.ArrayList;

//<--implement Parcelable-->

public class AlbumData implements Parcelable {
    private ArrayList<ImageObject> images;
    private String albumName;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    public AlbumData(String albumName, ArrayList<ImageObject> images) {
        this.albumName = albumName;
        this.images = images;
        this.createdDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
    }

    public AlbumData(String albumName){
        this.albumName = albumName;
        this.images = new ArrayList<>();
    }

    protected AlbumData(Parcel in) {
        images = in.createTypedArrayList(ImageObject.CREATOR);
        albumName = in.readString();
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
        this.images.remove(image);
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


    public LocalDateTime getCreatedDate(){
        return this.createdDate;
    }

    public void setLastModifiedDate(){
        this.lastModifiedDate = LocalDateTime.now();
    }

    public LocalDateTime getLastModifiedDate(){
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
}
