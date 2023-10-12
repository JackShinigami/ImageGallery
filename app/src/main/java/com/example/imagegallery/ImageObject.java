package com.example.imagegallery;

import android.content.Context;
import android.os.Parcelable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;


public class ImageObject implements Parcelable {
    private String filePath;
    private long lastModifiedDate;
    private String fileName;
    ImageObject(String filePath, long lastModifiedDate, String fileName) {
        this.filePath = filePath;
        this.lastModifiedDate = lastModifiedDate;
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }
    public long getLastModifiedDate() {
        return lastModifiedDate;
    }
    public String getFileName() {
        return fileName;
    }

    public static void getImage(File folder, ArrayList<ImageObject> images) {
        File[] files = folder.listFiles();

        if(files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.getName().equals("cache") || file.getName().equals(".thumbnails"))
                        continue;
                    getImage(file, images);
                } else {
                    String fileName = file.getName().toLowerCase();
                    long date = file.lastModified();
                    if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif"))
                        images.add(new ImageObject(file.getAbsolutePath(), date, fileName));
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
        dest.writeLong(lastModifiedDate);
        dest.writeString(fileName);
    }
    protected ImageObject(android.os.Parcel in) {
        filePath = in.readString();
        lastModifiedDate = in.readLong();
        fileName = in.readString();
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

    public static void sortByDate(ArrayList<ImageObject> images, boolean ascending) {
        if(ascending) {
            images.sort(Comparator.comparing(o -> o.lastModifiedDate));
        }
        else {
            images.sort((o1, o2) -> (int) (o2.lastModifiedDate - o1.lastModifiedDate));
        }
    }

    public static void sortByFileName(ArrayList<ImageObject> images, boolean ascending) {
        if(ascending) {
            images.sort(Comparator.comparing(o -> o.fileName));
        }
        else {
            images.sort((o1, o2) -> o2.fileName.compareTo(o1.fileName));
        }
    }
}
