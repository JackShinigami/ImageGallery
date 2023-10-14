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
    private ArrayList<String> albumNames;

    ImageObject(String filePath, long lastModifiedDate, String fileName) {
        this.filePath = filePath;
        this.lastModifiedDate = lastModifiedDate;
        this.fileName = fileName;
        this.albumNames = new ArrayList<>();
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
                    String fileName = file.getName().toLowerCase();
                    long date = file.lastModified();

                    if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif")) {
                        ImageObject image = new ImageObject(file.getAbsolutePath(), date, fileName);
                        image.albumNames = SharedPreferencesManager.loadImageAlbumInfo(context, image.filePath);
                        if (image.albumNames != null)
                            for (String albumName : image.albumNames)
                                Log.d("Album", albumName + " " + image.fileName);
                        else {
                            image.setAlbumNames(context, new ArrayList<String>());
                        }
                        images.add(image);

                    }
                }
            }
        }
    }



    public ArrayList<String> getAlbumNames() {
        return albumNames;
    }

    public void setAlbumNames(Context context, ArrayList<String> albumNames) {
        this.albumNames = albumNames;
        SharedPreferencesManager.saveImageAlbumInfo(context, this);
    }

    public void removeAlbumName(Context context, String albumName) {
        if(this.albumNames != null && this.albumNames.contains(albumName)) {
            this.albumNames.remove(albumName);
            SharedPreferencesManager.saveImageAlbumInfo(context, this);
        }
    }

    public void addAlbumName(Context context, String albumName) {
        if(this.albumNames == null)
            this.albumNames = new ArrayList<>();

        if(!this.albumNames.contains(albumName)) {
            this.albumNames.add(albumName);
            SharedPreferencesManager.saveImageAlbumInfo(context, this);
        }
    }

    public void deleteToTrash(Context context) {
        File file = new File(this.filePath);
        File externalStorage = Environment.getExternalStorageDirectory();
            if(this.albumNames != null) {
                for (String albumName : this.albumNames) {
                    AlbumData album = SharedPreferencesManager.loadAlbumData(context, albumName);
                    album.removeImage(this);
                    SharedPreferencesManager.saveAlbumData(context, album);
                }
                SharedPreferencesManager.deleteImageAlbumInfo(context, this);
            }

            File trash = new File(externalStorage, "Trash");
            if(!trash.exists())
                trash.mkdir();
            File newFile = new File(trash, this.fileName);

            SharedPreferencesManager.saveTrashFile(context, newFile.getAbsolutePath(), this.filePath);
            file.renameTo(newFile);
    }

    //delete file from trash
    public void deleteFile(Context context) {
        File file = new File(this.filePath);
        if(file.exists()) {
            file.delete();
            SharedPreferencesManager.deleteTrashFile(context, this.filePath);
            AlbumData album = SharedPreferencesManager.loadAlbumData(context, "Trash");
            album.removeImage(this);
            SharedPreferencesManager.saveAlbumData(context, album);
        }
    }

    public void restoreFile(Context context){
        File file = new File(this.filePath);
        File externalStorage = Environment.getExternalStorageDirectory();
        if(file.exists()) {
            String oldName = SharedPreferencesManager.loadTrashFile(context, this.filePath);
            String folderName;
            if  (oldName == null)
                folderName = "Pictures";
            else {
                folderName = oldName.substring(0, oldName.lastIndexOf("/"));
                folderName = folderName.substring(folderName.lastIndexOf("/") + 1);
            }
            File newFolder = new File(externalStorage, folderName);

            File newFile = new File(newFolder, this.fileName);
            SharedPreferencesManager.deleteTrashFile(context, this.filePath);
            file.renameTo(newFile);

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
        dest.writeStringList(albumNames);
    }
    protected ImageObject(android.os.Parcel in) {
        filePath = in.readString();
        lastModifiedDate = in.readLong();
        fileName = in.readString();
        albumNames = in.createStringArrayList();
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

        for(ImageObject image : images)
            Log.d("Date", new Date(image.lastModifiedDate).toString() + " " + image.fileName  );
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
