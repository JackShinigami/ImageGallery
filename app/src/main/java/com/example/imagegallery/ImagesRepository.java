package com.example.imagegallery;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

public class ImagesRepository {
    private static ImagesRepository instance;
    private final MutableLiveData<ArrayList<ImageObject>> ImagesBackup;

    private ImagesRepository() {
        // Private constructor prevents instantiation from other classes
        ImagesList = new MutableLiveData<>(new ArrayList<>());
        ImagesBackup = new MutableLiveData<>(new ArrayList<>());

    }
    public static synchronized ImagesRepository getInstance() {
        if (instance == null) {
            instance = new ImagesRepository();
        }
        return instance;
    }

    private final MutableLiveData<ArrayList<ImageObject>> ImagesList;

    public void setImagesList(ArrayList<ImageObject> list) {
        ImagesList.setValue(list);
    }

    public MutableLiveData<ArrayList<ImageObject>> getImagesList() {
        return ImagesList;
    }

    public void setImagesBackup(ArrayList<ImageObject> list) {
        ImagesBackup.setValue(list);
    }

    public MutableLiveData<ArrayList<ImageObject>> getImagesBackup() {
        return ImagesBackup;
    }



    public void addImage(ImageObject item) {
        ArrayList<ImageObject> currentList = ImagesList.getValue();
        if(currentList == null)
            currentList = new ArrayList<>();
        currentList.add(item);
        ImagesList.setValue(currentList);
    }

    public void deleteImage(ImageObject item) {
        ArrayList<ImageObject> currentList = ImagesList.getValue();
        if(currentList != null) {
            for(ImageObject image : currentList) {
                if(image.getFilePath().equals(item.getFilePath())) {
                    currentList.remove(image);
                    break;
                }
            }
            ImagesList.setValue(currentList);
        }
    }

}
