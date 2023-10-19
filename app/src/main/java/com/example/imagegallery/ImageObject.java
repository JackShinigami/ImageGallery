package com.example.imagegallery;

import android.content.Context;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
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

    public static void getImage(Context context, File folder, ArrayList<ImageObject> images) {
        File[] files = folder.listFiles();

        if(files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.getName().equals("cache") || file.getName().equals(".thumbnails"))
                        continue;
                    getImage(context, file, images);
                } else {
                    String fileName = file.getName();
                    String fileNameLower = fileName.toLowerCase();
                    long date = file.lastModified();

                    if (fileNameLower.endsWith(".jpg") || fileNameLower.endsWith(".png") || fileNameLower.endsWith(".jpeg") || fileNameLower.endsWith(".gif") || fileNameLower.endsWith(".webp")) {
                        ImageObject image = new ImageObject(file.getAbsolutePath(), date, fileName);
                        images.add(image);

                    }
                }
            }
        }
    }



    public void setAlbumNames(Context context, ArrayList<String> albumNames) {
        SharedPreferencesManager.saveImageAlbumInfo(context, this.filePath, albumNames);
    }

    public ArrayList<String> getAlbumNames(Context context) {
        return SharedPreferencesManager.loadImageAlbumInfo(context, this.filePath);
    }

    public void removeAlbumName(Context context, String albumName) {
        ArrayList<String> albumNames = SharedPreferencesManager.loadImageAlbumInfo(context, this.filePath);
        if(albumNames != null) {
            if(albumNames.contains(albumName)) {
                albumNames.remove(albumName);
                SharedPreferencesManager.saveImageAlbumInfo(context, this.filePath, albumNames);
            }
        }
    }

    public void addAlbumName(Context context, String albumName) {
        ArrayList<String> albumNames = SharedPreferencesManager.loadImageAlbumInfo(context, this.filePath);
        if(albumNames != null) {
            if(!albumNames.contains(albumName)) {
                albumNames.add(albumName);
                SharedPreferencesManager.saveImageAlbumInfo(context, this.filePath, albumNames);
            }
        }
    }

    public void deleteToTrash(Context context) {
        File file = new File(this.filePath);
        File externalStorage = Environment.getExternalStorageDirectory();
        ArrayList<String> albumNames = getAlbumNames(context);
        if (albumNames != null && albumNames.size() > 0 ){
            for (String albumName : albumNames) {
                AlbumData album = SharedPreferencesManager.loadAlbumData(context, albumName);
                if (album != null) {
                    album.removeImage(this);
                    SharedPreferencesManager.saveAlbumData(context, album);
                }
            }
            SharedPreferencesManager.deleteImageAlbumInfo(context, this);
        }

        File trash = new File(externalStorage, "Trash");
        if (!trash.exists())
            trash.mkdir();
        File newFile = new File(trash, this.fileName);

        SharedPreferencesManager.saveTrashFile(context, newFile.getAbsolutePath(), this);
        SharedPreferencesManager.saveImageAlbumInfo(context, newFile.getAbsolutePath(), albumNames);

        file.renameTo(newFile);
    }

    //delete file from trash
    public void deleteFile(Context context) {
        File file = new File(this.filePath);
        if(file.exists()) {
            file.delete();
            ImageObject oldObject = SharedPreferencesManager.loadTrashFile(context, this.filePath);
            SharedPreferencesManager.deleteLovedImages(context, oldObject.filePath);
            SharedPreferencesManager.deleteTrashFile(context, this.filePath);
            AlbumData album = SharedPreferencesManager.loadAlbumData(context, "Trash");
            album.removeImage(this);
            SharedPreferencesManager.saveAlbumData(context, album);
            SharedPreferencesManager.deleteImageAlbumInfo(context, this);
        }
    }

    public void restoreFile(Context context){
        File file = new File(this.filePath);
        File externalStorage = Environment.getExternalStorageDirectory();
        if(file.exists()) {
            ImageObject oldObject = SharedPreferencesManager.loadTrashFile(context, this.filePath);
            String oldFilePath;
            if  (oldObject == null)
                oldFilePath = externalStorage.getPath() + "/Pictures/" + this.fileName;
            else {
                oldFilePath = oldObject.filePath;
            }

            File newFile = new File(oldFilePath);
            file.renameTo(newFile);

            ArrayList<String> albumNames = SharedPreferencesManager.loadImageAlbumInfo(context, this.filePath);

            if(albumNames != null && albumNames.size() > 0) {
                for (String albumName : albumNames) {
                    AlbumData album = SharedPreferencesManager.loadAlbumData(context, albumName);
                    if(album != null) {
                        album.addImage(oldObject);
                        SharedPreferencesManager.saveAlbumData(context, album);
                    }
                    else
                    {
                        albumNames.remove(albumName);
                    }
                }

                SharedPreferencesManager.saveImageAlbumInfo(context, oldFilePath, albumNames);
            }

            SharedPreferencesManager.deleteImageAlbumInfo(context, this);
            SharedPreferencesManager.deleteTrashFile(context, this.filePath);


            AlbumData album = SharedPreferencesManager.loadAlbumData(context, "Trash");
            album.removeImage(this);
            SharedPreferencesManager.saveAlbumData(context, album);
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
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_error)
                .into(imageView);
    }

    public void loadImage(Context context, ImageView imageView, int width, int height) {
        Glide.with(context)
                .load(new File(filePath))
                .override(width, height)
                .fitCenter()
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_error)
                .into(imageView);
    }

    public static void sortByDate(ArrayList<ImageObject> images, boolean ascending) {
        if(ascending) {
            images.sort(Comparator.comparing(o -> new Date (o.lastModifiedDate)));
        }
        else {
            images.sort((o1, o2) -> new Date(o2.lastModifiedDate).compareTo(new Date(o1.lastModifiedDate)));
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

    public boolean isLoved(Context context) {
        Boolean loved = SharedPreferencesManager.isLovedImages(context, this.filePath);
        return loved;
    }

    public void setLoved(Context context, boolean loved) {
        if(loved) {
            SharedPreferencesManager.saveLovedImages(context, this.filePath);
            this.addAlbumName(context, "Favorites");
        }
        else {
            SharedPreferencesManager.deleteLovedImages(context, this.filePath);
            this.removeAlbumName(context, "Favorites");
        }
    }
}
