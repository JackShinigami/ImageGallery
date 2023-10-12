package com.example.imagegallery;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
    private ImageButton btnChangeGrid, btnSort, btnAlbum, btnGallery, btnCamera;

    private RecyclerView recyclerView;
    private MyAdapter adapter;

    private ArrayList<ImageObject> images = new ArrayList<>();
    private int[] colNumbers = {2, 3, 4};
    private int colNumberIndex = 0;

    private boolean asencding = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Lấy WindowManager
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowSize.getScreenSize(windowManager);


        btnAlbum = findViewById(R.id.btnAlbum);

        btnCamera = findViewById(R.id.btnCamera);
        btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
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

        ImageObject.sortByDate(images, asencding);


        //Load ImageFragment with images on fragment_container
        ImageFragment imageFragment = ImageFragment.newInstance(images);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, imageFragment);
        fragmentTransaction.commit();


        btnAlbum.setOnClickListener(new View.OnClickListener() {
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
            }
        });

        btnGallery = findViewById(R.id.btnGallery);
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageFragment imageFragment = ImageFragment.newInstance(images);
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, imageFragment);
                fragmentTransaction.commit();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
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