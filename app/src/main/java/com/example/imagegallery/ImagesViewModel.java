package com.example.imagegallery;

import android.widget.ArrayAdapter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class ImagesViewModel extends ViewModel {
    private final ImagesRepository repository;
    private final MutableLiveData<ArrayList<ImageObject>> ImagesAlbum;
    public ImagesViewModel() {
        repository = ImagesRepository.getInstance();
        ImagesAlbum = new MutableLiveData<>(new ArrayList<>());
    }

    public void setImagesList(ArrayList<ImageObject> list) {
        repository.setImagesList(list);
    }

    public void setImagesAlbum(ArrayList<ImageObject> list) {
        ImagesAlbum.setValue(list);
    }

    public MutableLiveData<ArrayList<ImageObject>> getImagesAlbum() {
        return ImagesAlbum;
    }

    public LiveData<ArrayList<ImageObject>> getImagesList() {
        return repository.getImagesList();
    }

    public void addImage(ImageObject item) {
        repository.addImage(item);
    }

    public void deleteImage(ImageObject item) {
        repository.deleteImage(item);
    }
}
