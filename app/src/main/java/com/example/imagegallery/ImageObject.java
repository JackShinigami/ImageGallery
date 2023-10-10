package com.example.imagegallery;

import android.content.Context;
import android.os.Parcelable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

public class ImageObject implements Parcelable {
    private String filePath;

    ImageObject(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }
    public static void getImage(File folder, List<ImageObject> images) {
        File[] files = folder.listFiles();

        if(files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    getImage(file, images);
                } else {
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif"))
                        images.add(new ImageObject(file.getAbsolutePath()));
                }
            }
        }
    }
    //parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeString(filePath);
    }
    protected ImageObject(android.os.Parcel in) {
        filePath = in.readString();
    }

    public static final Creator<ImageObject> CREATOR = new Creator<ImageObject>() {
        @Override
        public ImageObject createFromParcel(android.os.Parcel in) {
            return new ImageObject(in);
        }

        @Override
        public ImageObject[] newArray(int size) {
            return new ImageObject[size];
        }
    };

    public void loadImage(Context context, ImageView imageView) {
        Glide.with(context)
                .load(new File(filePath))
                .into(imageView);
    }

    public void loadImage(Context context, ImageView imageView, int width, int height) {
        Glide.with(context)
                .load(new File(filePath))
                .override(width, height)
                .fitCenter()
                .into(imageView);
    }
}
