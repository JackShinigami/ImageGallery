package com.example.imagegallery;

import java.util.ArrayList;

public class AlbumData {
    private ArrayList<ImageObject> images;
    private String albumName;

    public AlbumData(String albumName, ArrayList<ImageObject> images) {
        this.albumName = albumName;
        this.images = images;
    }

    public AlbumData(String albumName){
        this.albumName = albumName;
        this.images = new ArrayList<>();
    }

    public void addImage(ImageObject image){
        this.images.add(image);
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
}
