package com.example.imagegallery;

import static java.lang.Thread.sleep;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;


public class ImageObject implements Parcelable {
    private String filePath;
    private long lastModifiedDate;
    private String fileName;
    private float[] latLong = null;

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

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.getName().equals("cache") || file.getName().equals(".thumbnails"))
                        continue;
                    getImage(context, file, images);
                } else {
                    String fileName = file.getName();
                    String fileNameLower = fileName.toLowerCase();
                    long date = file.lastModified();

                    if (fileNameLower.endsWith(".jpg") || fileNameLower.endsWith(".png") || fileNameLower.endsWith(".jpeg") || fileNameLower.endsWith(".gif") || fileNameLower.endsWith(".webp") || fileNameLower.endsWith(".heic")) {
                        ImageObject image = new ImageObject(file.getAbsolutePath(), date, fileName);
                        images.add(image);
                    }
                }
            }
        }
    }

    public Uri getImageUri() {
        return Uri.fromFile(new File(filePath));
    }

    private String hashImage(Bitmap image)
    {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(byteArray);

            // Chuyển đổi giá trị băm thành một chuỗi hex
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch (Exception e)
        {
            Log.e("Hash", e.getMessage());
            return null;
        }
    }
    public static void deleteDuplicateImage(Context context,ArrayList<ImageObject> images)
    {
        Bundle hashBundle = new Bundle();
        for (ImageObject image : images) {
            Bitmap b = BitmapFactory.decodeFile(image.getFilePath());
            String hash = image.hashImage(b);
            if(hashBundle.containsKey(hash))
            {
                image.deleteToTrash(context);
            }
            else
            {
                hashBundle.putString(hash, image.getFilePath());
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

    public void updateAlbumName(Context context, String oldAlbumName, String newAlbumName)
    {
        ArrayList<String> albumNames = SharedPreferencesManager.loadImageAlbumInfo(context, this.filePath);
        if(albumNames != null) {
            if(albumNames.contains(oldAlbumName)) {
                albumNames.remove(oldAlbumName);
                albumNames.add(newAlbumName);
                SharedPreferencesManager.saveImageAlbumInfo(context, this.filePath, albumNames);
            }
        }
    }

    public void setLatLong(float[] latLong) {
        this.latLong = latLong;
    }
    public float[] getLatLong() {
        return latLong;
    }
    private boolean locationLoaded = false;

    boolean isLocationLoaded() {
        return locationLoaded;
    }

    public void loadLatLong(Context context)
    {
        try {
            if(locationLoaded)
                return;
            ExifInterface exif = new ExifInterface(getFilePath());
            float[] latLong = new float[2];
            if (exif.getLatLong(latLong)) {
                setLatLong(latLong);
                //Log.d("ImageObject", "lat: " + latLong[0] + " long: " + latLong[1]);
            } else {
                //Log.d("ImageObject", "lat: null long: null");
                setLatLong(null);
            }
            locationLoaded = true;
        } catch (Exception e) {
            Log.e("Exif", e.toString());
        }
    }
    public String getAddress(Context context) {
        if (latLong == null)
            return "Unknown";

        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() {
                try {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(latLong[0], latLong[1], 1);
                    return addresses.get(0).getAddressLine(0);
                } catch (Exception e) {
                    Log.e("Address", e.getMessage());
                    return "Unknown";
                }
            }
        };

        FutureTask<String> future = new FutureTask<>(callable);
        new Thread(future).start();

        try {
            // Wait for 1 second and then retrieve the result
            return future.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.e("Address", "Geocoding operation timed out");
            return "Unknown";
        }
    }

    private File safeNewFile(File dir, String fileName) {
        File file = new File(dir, fileName);
        if(file.exists()) {
            int i = 1;
            String name = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            while (true) {
                file = new File(dir, name + "(" + i + ")" + extension);
                if(!file.exists())
                    break;
                i++;
            }
        }
        return file;
    }

    private File safeNewFile(String filePath)
    {
        File file = new File(filePath);
        if(file.exists()) {
            int i = 1;
            String name = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            while (true) {
                file = new File(name + "(" + i + ")" + extension);
                if(!file.exists())
                    break;
                i++;
            }
        }
        return file;
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
                else
                {
                    albumNames.remove(albumName);
                }
            }
            SharedPreferencesManager.deleteImageAlbumInfo(context, this);
        }

        File trash = new File(context.getExternalFilesDir(null), "Trash");
        if (!trash.exists())
            trash.mkdir();

        File newFile = safeNewFile(trash, this.fileName);

        SharedPreferencesManager.saveTrashFile(context, newFile.getAbsolutePath(), this);
        SharedPreferencesManager.saveImageAlbumInfo(context, newFile.getAbsolutePath(), albumNames);

        ArrayList<String> tags = SharedPreferencesManager.loadTagsForImage(context, this.filePath);
        if(tags != null) {
            SharedPreferencesManager.saveTagsForImage(context, newFile.getAbsolutePath(), tags);
            SharedPreferencesManager.deleteTagsForImage(context, this.filePath);
        }

        file.renameTo(newFile);
    }

    //delete file from trash
    public void deleteFile(Context context) {
        File file = new File(this.filePath);
        if(file.exists()) {
            file.delete();
            ImageObject oldObject = SharedPreferencesManager.loadTrashFile(context, this.filePath);
            if(oldObject != null) {
                SharedPreferencesManager.deleteLovedImages(context, oldObject.filePath);
                SharedPreferencesManager.deleteTrashFile(context, this.filePath);
                SharedPreferencesManager.deleteImageAlbumInfo(context, this);
                SharedPreferencesManager.deleteTagsForImage(context, this.filePath);
            }

            AlbumData album = SharedPreferencesManager.loadAlbumData(context, "Trash");
            album.removeImage(this);
            SharedPreferencesManager.saveAlbumData(context, album);
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

            File newFile = safeNewFile(oldFilePath);
            file.renameTo(newFile);

            ArrayList<String> albumNames = SharedPreferencesManager.loadImageAlbumInfo(context, this.filePath);

            if(albumNames != null && albumNames.size() > 0) {
                for (String albumName : albumNames) {
                    AlbumData album = SharedPreferencesManager.loadAlbumData(context, albumName);
                    if(album != null && oldObject != null) {
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

            ArrayList<String> tags = SharedPreferencesManager.loadTagsForImage(context, this.filePath);
            if(tags != null) {
                SharedPreferencesManager.saveTagsForImage(context, newFile.getAbsolutePath(), tags);
                SharedPreferencesManager.deleteTagsForImage(context, this.filePath);
            }

            AlbumData album = SharedPreferencesManager.loadAlbumData(context, "Trash");
            album.removeImage(this);
            SharedPreferencesManager.saveAlbumData(context, album);
        }

    }

    public String getQRCodeContent(Context context)
    {
        Bitmap b = BitmapFactory.decodeFile(this.filePath);
        //scan qr code in this image
        String contents = null;
        int[] intArray = new int[b.getWidth()*b.getHeight()];
        b.getPixels(intArray, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        LuminanceSource source = new RGBLuminanceSource(b.getWidth(), b.getHeight(), intArray);
        com.google.zxing.BinaryBitmap bitmap = new com.google.zxing.BinaryBitmap(new com.google.zxing.common.HybridBinarizer(source));
        try {
            contents = new com.google.zxing.qrcode.QRCodeReader().decode(bitmap).getText();
            Toast.makeText(context, contents, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contents;
    }

    private boolean tagLoaded = false;

    boolean isTagLoaded() {
        return tagLoaded;
    }

    public ArrayList<String> getTags(Context context, TaskCompletionSource<Void> taskCompletionSource) {
        ArrayList<String> tags = SharedPreferencesManager.loadTagsForImage(context, this.filePath);
        if(tags == null) {
            autoSetTag(context, taskCompletionSource);
            tagLoaded = true;
            return new ArrayList<>();
        }
        return tags;
    }
    public void addTag(Context context, String tag) {
        ArrayList<String> tags = SharedPreferencesManager.loadTagsForImage(context, this.filePath);
        if(tags == null) {
            tags = new ArrayList<>();
        }
        if(!tags.contains(tag)) {
            tags.add(tag);
            SharedPreferencesManager.saveTagsForImage(context, this.filePath, tags);
        }
    }
    public void removeTag(Context context, String tag) {
        ArrayList<String> tags = SharedPreferencesManager.loadTagsForImage(context, this.filePath);
        if(tags != null) {
            if(tags.contains(tag)) {
                tags.remove(tag);
                SharedPreferencesManager.saveTagsForImage(context, this.filePath, tags);
            }
        }
    }
    


    public void deleteTags(Context context) {
        SharedPreferencesManager.deleteTagsForImage(context, this.filePath);
    }
    private static final ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

    private void autoSetTag(Context context, TaskCompletionSource<Void> taskCompletionSource) {
        ArrayList<String> tags = new ArrayList<>();
        InputImage image = InputImage.fromBitmap(BitmapFactory.decodeFile(getFilePath()), 0);


        labeler.process(image)
                .addOnSuccessListener(labels -> {
                    for (com.google.mlkit.vision.label.ImageLabel label : labels) {
                        String eachLabel = label.getText();
                        float confidence = label.getConfidence();
                        if(confidence > 0.8) {
                            tags.add(eachLabel);
                        }
                    }
                    SharedPreferencesManager.saveTagsForImage(context, this.filePath, tags);
                    taskCompletionSource.setResult(null);
                })
                .addOnFailureListener(e -> {
                    // Task failed with an exception
                    // ...

                    Log.d("Taggg", "Failed "+ e.getMessage());
                });
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
        dest.writeFloatArray(latLong);
    }
    protected ImageObject(android.os.Parcel in) {
        filePath = in.readString();
        lastModifiedDate = in.readLong();
        fileName = in.readString();
        latLong = in.createFloatArray();
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
