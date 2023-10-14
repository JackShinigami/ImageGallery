package com.example.imagegallery;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    String currentPhotoPath="default";
    private String PATHPREFNAME = "pathPref";
    private ImageButton btnAlbum, btnGallery, btnCamera;

    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private ArrayList<ImageObject> currentImages;
    public enum FragmentType {
        IMAGE_FRAGMENT,
        ALBUM_FRAGMENT,
        ALBUM_IMAGE_FRAGMENT
    }

    private FragmentType currentFragment;
    private String currentFragmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Dòng này để khi tắt app bằng nút đỏ debug, mở cmt và cmt dòng ở dưới lại, sau khi chạy xong tắt bằng đt và để lại như cũ
        //currentFragment = FragmentType.IMAGE_FRAGMENT;
        currentFragment = FragmentType.values()[SharedPreferencesManager.loadStateFragment(this)];


        if(currentFragment == FragmentType.ALBUM_IMAGE_FRAGMENT) {
            currentImages = SharedPreferencesManager.loadCurrentImages(this);
            currentFragmentName = SharedPreferencesManager.loadCurrentName(this);
        }
        else {
            currentImages = new ArrayList<>();
            currentFragmentName = "Gallery";
        }

        // Lấy WindowManager
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowSize.getScreenSize(windowManager);


        btnAlbum = findViewById(R.id.btnAlbum);
        btnGallery = findViewById(R.id.btnGallery);

        btnCamera = findViewById(R.id.btnCamera);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onResume() {
        super.onResume();

        //load currentPhotoPath
        SharedPreferences sharedPref = getSharedPreferences(PATHPREFNAME, Context.MODE_PRIVATE);
        if (sharedPref.contains("path") && sharedPref!=null) {
            currentPhotoPath = sharedPref.getString("path", "");
        }


        checkcurrentPhotoPath();
        File externalStorage = Environment.getExternalStorageDirectory();

// Lấy thư mục Pictures
        File picturesDirectory = new File(externalStorage, "Pictures");
        File downloadDirectory = new File(externalStorage, "Download");
        File dcimDirectory = new File(externalStorage, "DCIM");

        ArrayList<ImageObject> images = new ArrayList<>();

        ImageObject.getImage(picturesDirectory, images);
        ImageObject.getImage(downloadDirectory, images);
        ImageObject.getImage(dcimDirectory, images);
// In ra danh sách các file ảnh
        for (ImageObject imageFile : images) {
            Log.d("IMAGE", imageFile.getFilePath());

        }

        if(FragmentType.IMAGE_FRAGMENT == currentFragment){
            //Load ImageFragment with images on fragment_container
            ImageFragment imageFragment = ImageFragment.newInstance(images, currentFragmentName);
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, imageFragment);
            fragmentTransaction.commit();

            btnGallery.setImageResource(R.drawable.ic_gallery_launcher_selected);
            btnAlbum.setImageResource(R.drawable.ic_album_launcher);
        }
        else if(FragmentType.ALBUM_FRAGMENT == currentFragment) {
            ArrayList<AlbumData> albumData = new ArrayList<>();
            AlbumData album = new AlbumData("All Images", images);
            albumData.add(album);
            AlbumFragment albumFragment = AlbumFragment.newInstance(albumData);

            FragmentTransaction AlbumFragmentTransaction = fragmentManager.beginTransaction();
            AlbumFragmentTransaction.replace(R.id.fragment_container, albumFragment);
            AlbumFragmentTransaction.commit();
            btnAlbum.setImageResource(R.drawable.ic_album_launcher_selected);
            btnGallery.setImageResource(R.drawable.ic_gallery_launcher);
        }
        else if(FragmentType.ALBUM_IMAGE_FRAGMENT == currentFragment){

            ImageFragment albumImageFragment = ImageFragment.newInstance(currentImages, currentFragmentName);
            FragmentTransaction AlbumImageFragmentTransaction = fragmentManager.beginTransaction();
            AlbumImageFragmentTransaction.replace(R.id.fragment_container, albumImageFragment);
            AlbumImageFragmentTransaction.commit();
            btnAlbum.setImageResource(R.drawable.ic_album_launcher_selected);
            btnGallery.setImageResource(R.drawable.ic_gallery_launcher);
        }


        btnAlbum.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View v) {

                ArrayList<AlbumData> albumData = new ArrayList<>();
                AlbumData album = new AlbumData("All Images", images);
                albumData.add(album);

                AlbumFragment albumFragment = AlbumFragment.newInstance( albumData);
                FragmentManager albumFragmentManager = getSupportFragmentManager();
                FragmentTransaction AlbumFragmentTransaction = fragmentManager.beginTransaction();
                AlbumFragmentTransaction.replace(R.id.fragment_container, albumFragment);
                AlbumFragmentTransaction.commit();
                btnAlbum.setImageResource(R.drawable.ic_album_launcher_selected);
                btnGallery.setImageResource(R.drawable.ic_gallery_launcher);

                setCurrentFragment(FragmentType.ALBUM_FRAGMENT);
            }
        });


        btnGallery.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onClick(View view) {
                ImageFragment imageFragment = ImageFragment.newInstance(images, "Gallery");
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, imageFragment);
                fragmentTransaction.commit();
                btnAlbum.setImageResource(R.drawable.ic_album_launcher);
                btnGallery.setImageResource(R.drawable.ic_gallery_launcher_selected);

                setCurrentFragment(FragmentType.IMAGE_FRAGMENT);
                setCurrentImages(new ArrayList<>());
                setCurrentFragmentName("Gallery");
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        //save currentPhotoPath
        SharedPreferences sharedPref = getSharedPreferences(PATHPREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("path", currentPhotoPath);
        editor.commit();

        SharedPreferencesManager.saveCurrentImages(this, currentImages);
        SharedPreferencesManager.saveStateFragment(this, currentFragment.ordinal());
        SharedPreferencesManager.saveCurrentName(this, currentFragmentName);
        Log.println(Log.DEBUG, "onSaveInstanceState", currentFragment.toString());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesManager.saveCurrentImages(this, new ArrayList<>());
        SharedPreferencesManager.saveStateFragment(this, 0);
        SharedPreferencesManager.saveCurrentName(this, "Gallery");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }


    public void setCurrentImages(ArrayList<ImageObject> currentImages) {
        this.currentImages = currentImages;
    }

    public void setCurrentFragment(FragmentType currentFragment) {
        this.currentFragment = currentFragment;
    }

    public void setCurrentFragmentName(String currentFragmentName) {
        this.currentFragmentName = currentFragmentName;
    }

    public String getCurrentFragementName()
    {
        return currentFragmentName;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)));
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent

        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.d(TAG, "dispatchTakePictureIntent: "+ex.getMessage());
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            //Toast.makeText(this, "File created", Toast.LENGTH_SHORT).show();
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider1",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivity(takePictureIntent);
        }


        //check if bitmap is null


        galleryAddPic();
        currentFragment = FragmentType.IMAGE_FRAGMENT;
        currentImages = new ArrayList<>();
        currentFragmentName = "Gallery";


    }

    private void galleryAddPic() {
        //copy to gallery
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);

    }

    public boolean deleteFile(String path)
    {
        File file = new File(path);
        boolean isDeleted = file.delete();
        /*if(isDeleted)
        {
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Not deleted", Toast.LENGTH_SHORT).show();
        }*/
        return isDeleted;
    }

    public void setWallpaper(String Path) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        try {
            wallpaperManager.setBitmap(BitmapFactory.decodeFile(Path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLockScreen(String Path)
    {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        try {
            wallpaperManager.setBitmap(BitmapFactory.decodeFile(Path), null, true, WallpaperManager.FLAG_LOCK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void checkcurrentPhotoPath()
    {
        if (currentPhotoPath.equals("default"))
        {
            return;
        }
        Bitmap bitmap = null;
        bitmap= BitmapFactory.decodeFile(currentPhotoPath);
        if(bitmap==null)
        {
            Toast.makeText(this, "Bitmap is null", Toast.LENGTH_SHORT).show();
            deleteFile(currentPhotoPath);
        }
    }
}