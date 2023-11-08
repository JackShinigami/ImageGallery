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

import android.icu.text.SimpleDateFormat;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.os.Handler;
import android.os.Message;
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
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    String currentPhotoPath = "default";
    private String PATHPREFNAME = "pathPref";
    private ImageButton btnAlbum, btnGallery, btnCamera, btnSearch;

    private RecyclerView recyclerView;
    private ImageAdapter adapter;
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private ArrayList<ImageObject> currentImages;

    public enum FragmentType {
        IMAGE_FRAGMENT,
        ALBUM_FRAGMENT,
        ALBUM_IMAGE_FRAGMENT
    }

    private FragmentType currentFragment;
    private String currentFragmentName;

    private ImageFragment imageFragment;
    private AlbumFragment albumFragment;

    private boolean loading = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Dòng này để khi tắt app bằng nút đỏ debug, mở cmt và cmt dòng ở dưới lại, sau khi chạy xong tắt bằng đt và để lại như cũ
        //currentFragment = FragmentType.IMAGE_FRAGMENT;
        currentFragment = FragmentType.values()[SharedPreferencesManager.loadStateFragment(this)];
        currentPhotoPath="default";

        if (currentFragment == FragmentType.ALBUM_IMAGE_FRAGMENT) {
            currentImages = SharedPreferencesManager.loadCurrentImages(this);
            currentFragmentName = SharedPreferencesManager.loadCurrentName(this);
        } else {
            currentImages = new ArrayList<>();
            currentFragmentName = "Gallery";
        }

        // Lấy WindowManager
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowSize.getScreenSize(windowManager);


        btnAlbum = findViewById(R.id.btnAlbum);
        btnGallery = findViewById(R.id.btnGallery);

        btnCamera = findViewById(R.id.btnCamera);
        btnSearch = findViewById(R.id.btnSearch);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        File externalStorage = Environment.getExternalStorageDirectory();

// Lấy thư mục Pictures
        File picturesDirectory = new File(externalStorage, "Pictures");
        File downloadDirectory = new File(externalStorage, "Download");
        File dcimDirectory = new File(externalStorage, "DCIM");

        ArrayList<ImageObject> images = new ArrayList<>();

        ImageObject.getImage(this, picturesDirectory, images);
        ImageObject.getImage(this, downloadDirectory, images);
        ImageObject.getImage(this, dcimDirectory, images);


        AlbumHelper albumHelper = AlbumHelper.getInstance();
        ArrayList<AlbumData> defaultAlbums = albumHelper.createDefaultAlbum(this);

        if (FragmentType.IMAGE_FRAGMENT == currentFragment) {
            //Load ImageFragment with images on fragment_container
            imageFragment = ImageFragment.newInstance(images, "Gallery");
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, imageFragment, "Gallery");
            fragmentTransaction.addToBackStack("MainStack");
            fragmentTransaction.commit();

            btnGallery.setImageResource(R.drawable.ic_gallery_launcher_selected);
            btnAlbum.setImageResource(R.drawable.ic_album_launcher);
        } else if (FragmentType.ALBUM_FRAGMENT == currentFragment) {
            albumFragment = AlbumFragment.newInstance(defaultAlbums);

            FragmentTransaction AlbumFragmentTransaction = fragmentManager.beginTransaction();
            AlbumFragmentTransaction.replace(R.id.fragment_container, albumFragment, "Album");
            AlbumFragmentTransaction.addToBackStack("MainStack");
            AlbumFragmentTransaction.commit();
            btnAlbum.setImageResource(R.drawable.ic_album_launcher_selected);
            btnGallery.setImageResource(R.drawable.ic_gallery_launcher);
        } else if (FragmentType.ALBUM_IMAGE_FRAGMENT == currentFragment) {
            ArrayList<ImageObject> currentImages = SharedPreferencesManager.loadAlbumData(this, currentFragmentName).getImages();
            imageFragment = ImageFragment.newInstance(currentImages, currentFragmentName);
            FragmentTransaction AlbumImageFragmentTransaction = fragmentManager.beginTransaction();
            AlbumImageFragmentTransaction.replace(R.id.fragment_container, imageFragment, currentFragmentName);
            AlbumImageFragmentTransaction.addToBackStack("MainStack");
            AlbumImageFragmentTransaction.commit();
            btnAlbum.setImageResource(R.drawable.ic_album_launcher_selected);
            btnGallery.setImageResource(R.drawable.ic_gallery_launcher);
        }


    }

    private ArrayList<ImageObject> images;
    private ArrayList<AlbumData> defaultAlbums;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0)
            {
                if(FragmentType.IMAGE_FRAGMENT == currentFragment)
                {
                    imageFragment.setFragmentAdapter(images);
                    btnGallery.setImageResource(R.drawable.ic_gallery_launcher_selected);
                    btnAlbum.setImageResource(R.drawable.ic_album_launcher);

                    btnGallery.setEnabled(false);
                    btnAlbum.setEnabled(true);
                    btnCamera.setEnabled(true);
                }
                else if(FragmentType.ALBUM_IMAGE_FRAGMENT == currentFragment)
                {
                    ArrayList<ImageObject> currentImages = SharedPreferencesManager.loadAlbumData(MainActivity.this, currentFragmentName).getImages();
                    imageFragment.setFragmentAdapter(currentImages);
                    btnAlbum.setImageResource(R.drawable.ic_album_launcher_selected);
                    btnGallery.setImageResource(R.drawable.ic_gallery_launcher);

                    btnGallery.setEnabled(true);
                    btnAlbum.setEnabled(true);
                    btnCamera.setEnabled(true);
                }
                else if(FragmentType.ALBUM_FRAGMENT == currentFragment)
                {
                    albumFragment.setDefaultAlbums(defaultAlbums);
                    btnAlbum.setImageResource(R.drawable.ic_album_launcher_selected);
                    btnGallery.setImageResource(R.drawable.ic_gallery_launcher);

                    btnGallery.setEnabled(true);
                    btnAlbum.setEnabled(false);
                    btnCamera.setEnabled(false);
                }

                btnAlbum.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    public void onClick(View v) {

                        albumFragment = AlbumFragment.newInstance(defaultAlbums);
                        FragmentManager albumFragmentManager = getSupportFragmentManager();
                        FragmentTransaction AlbumFragmentTransaction = fragmentManager.beginTransaction();
                        AlbumFragmentTransaction.replace(R.id.fragment_container, albumFragment, "Album");
                        AlbumFragmentTransaction.addToBackStack("MainStack");
                        AlbumFragmentTransaction.commit();
                        btnAlbum.setImageResource(R.drawable.ic_album_launcher_selected);
                        btnGallery.setImageResource(R.drawable.ic_gallery_launcher);

                        btnGallery.setEnabled(true);
                        btnAlbum.setEnabled(false);
                        btnCamera.setEnabled(false);
                        setCurrentFragment(FragmentType.ALBUM_FRAGMENT);
                    }
                });


                btnGallery.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    public void onClick(View view) {
                        imageFragment = ImageFragment.newInstance(images, "Gallery");
                        FragmentManager fragmentManager = getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container, imageFragment, "Gallery");
                        fragmentTransaction.addToBackStack("MainStack");
                        fragmentTransaction.commit();
                        btnAlbum.setImageResource(R.drawable.ic_album_launcher);
                        btnGallery.setImageResource(R.drawable.ic_gallery_launcher_selected);

                        btnGallery.setEnabled(false);
                        btnAlbum.setEnabled(true);
                        btnCamera.setEnabled(true);

                        setCurrentFragment(FragmentType.IMAGE_FRAGMENT);
                        setCurrentImages(new ArrayList<>());
                        setCurrentFragmentName("Gallery");
                        SharedPreferencesManager.saveCurrentName(MainActivity.this, "Gallery");
                        SharedPreferencesManager.saveStateFragment(MainActivity.this, 0);
                    }
                });


                btnSearch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                        intent.putParcelableArrayListExtra("images", images);
                        startActivity(intent);

                    }
                });
            }
            else if(msg.what == 1)
            {
                onResume();
            }
        }
    };
    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onResume() {
        super.onResume();
        //load currentPhotoPath
        SharedPreferences sharedPref = getSharedPreferences(PATHPREFNAME, Context.MODE_PRIVATE);
        if (sharedPref.contains("path") && sharedPref != null) {
            currentPhotoPath = sharedPref.getString("path", "");
        }
        //Toast.makeText(this, currentPhotoPath, Toast.LENGTH_SHORT).show();
        //get location from shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("location", Context.MODE_PRIVATE);
        String strlatitude = sharedPreferences.getString("latitude", "0");
        String strlongitude = sharedPreferences.getString("longitude", "0");
        double latitude = Double.parseDouble(strlatitude);
        double longitude = Double.parseDouble(strlongitude);
        Toast.makeText(this, "Latitude: " + strlongitude + " Longitude: " + strlatitude, Toast.LENGTH_SHORT).show();

        setExif(latitude, longitude);



        checkcurrentPhotoPath();

        //test, comment the line below if it doesn't work
        checkPhotoInAlbum();


        Thread updateImagesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    File externalStorage = Environment.getExternalStorageDirectory();

                    // Lấy thư mục Pictures
                    File picturesDirectory = new File(externalStorage, "Pictures");
                    File downloadDirectory = new File(externalStorage, "Download");
                    File dcimDirectory = new File(externalStorage, "DCIM");

                    images = new ArrayList<>();

                    ImageObject.getImage(MainActivity.this, picturesDirectory, images);
                    ImageObject.getImage(MainActivity.this, downloadDirectory, images);
                    ImageObject.getImage(MainActivity.this, dcimDirectory, images);
                    AlbumHelper albumHelper = AlbumHelper.getInstance();
                    defaultAlbums = albumHelper.createDefaultAlbum(MainActivity.this);

                    handler.sendEmptyMessage(0);
                    loading = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        if(!loading)
        {
            loading = true;
            updateImagesThread.start();
        }
    }
    void setExif(double latitude, double longitude)
    {
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(currentPhotoPath);
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if(exif != null){
            try {
                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, dec2DMS(latitude));
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, dec2DMS(longitude));

                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitude > 0 ? "N" : "S");
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitude > 0 ? "E" : "W");

                exif.saveAttributes();
            } catch (IOException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, "Exif is null", Toast.LENGTH_SHORT).show();
        }
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
        //super.onBackPressed();
        int count = fragmentManager.getBackStackEntryCount();
        if (count == 1 || currentFragment == FragmentType.IMAGE_FRAGMENT) {
            super.onBackPressed();
            fragmentManager.popBackStackImmediate(null,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
            finishAffinity();
        } else {
            fragmentManager.popBackStack();
            handler.sendEmptyMessage(0);
        }
    }


    public void setCurrentImages(ArrayList<ImageObject> currentImages) {
        this.currentImages = currentImages;
    }

    public void setCurrentFragment(FragmentType currentFragment) {
        this.currentFragment = currentFragment;
        SharedPreferencesManager.saveStateFragment(this, currentFragment.ordinal());
    }

    public void setCurrentFragmentName(String currentFragmentName) {
        this.currentFragmentName = currentFragmentName;
        SharedPreferencesManager.saveCurrentName(this, currentFragmentName);
    }

    public String getCurrentFragementName() {
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

    private void dispatchTakePictureIntent()  {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent

        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            Log.d(TAG, "dispatchTakePictureIntent: " + ex.getMessage());
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





        galleryAddPic();


        //TRYING TO MAKE TAKE PHOTO BUTTON ADD IMAGE TO ALBUM
        /*currentFragment = FragmentType.IMAGE_FRAGMENT;
        currentImages = new ArrayList<>();
        currentFragmentName = "Gallery";*/  //dont have to, we want to back where capture image button is clicked

        ArrayList<String> albumNameList = SharedPreferencesManager.loadAlbumNameList(this);

        if (albumNameList == null) {
            albumNameList = new ArrayList<>();
        }

        ArrayList<String> finalAlbumNameList = albumNameList;
        String albumName = getCurrentFragementName();

        //create image object
        //File file = new File(currentPhotoPath);
        String fileName = photoFile.getName().toLowerCase();
        long date = photoFile.lastModified();
        ImageObject imageObject = new ImageObject(currentPhotoPath, date, fileName);

        if (finalAlbumNameList.contains(albumName)) {
            AlbumData albumData = SharedPreferencesManager.loadAlbumData(this, albumName);
            if (albumData.addImage(imageObject)) {
                SharedPreferencesManager.saveAlbumData(this, albumData);
                imageObject.addAlbumName(this, albumName);

                //Toast.makeText(this, "Image has been added to " + albumName, Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(this, "Image already exists in this album", Toast.LENGTH_SHORT).show();
            }
        }

        //get location
        /*LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        double latitude = 0;
        double longitude = 0;
        if(location != null){
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        Toast.makeText(this, "Latitude: " + latitude + " Longitude: " + longitude, Toast.LENGTH_SHORT).show();*/

        GPSTracker gpsTracker = new GPSTracker(this);
        double latitude = gpsTracker.getLatitude();
        double longitude = gpsTracker.getLongitude();
        //Toast.makeText(this, "Latitude: " + latitude + " Longitude: " + longitude, Toast.LENGTH_SHORT).show();



        //Toast address
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(addresses != null){
            Address address = addresses.get(0);
            String addressLine = address.getAddressLine(0);
            //Toast.makeText(this, addressLine, Toast.LENGTH_SHORT).show();
        }
        //save location to shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("location", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("latitude", String.valueOf(latitude));
        editor.putString("longitude", String.valueOf(longitude));
        editor.commit();

        //Toast.makeText(this, currentPhotoPath, Toast.LENGTH_SHORT).show();
        //add exif



    }

    String dec2DMS(double coord) {
        coord = coord > 0 ? coord : -coord;
        String sOut = Integer.toString((int)coord) + "/1,";
        coord = (coord % 1) * 60;
        sOut = sOut + Integer.toString((int)coord) + "/1,";
        coord = (coord % 1) * 60000;
        sOut = sOut + Integer.toString((int)coord) + "/1000";
        return sOut;
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

    public void checkPhotoInAlbum(){
        ArrayList<String> albumNameList = SharedPreferencesManager.loadAlbumNameList(this);
        if(albumNameList == null){
            albumNameList = new ArrayList<>();
        }

        ArrayList<String> finalAlbumNameList = albumNameList;
        String albumName = getCurrentFragementName();

        //create image object
        File file = new File(currentPhotoPath);
        String fileName = file.getName().toLowerCase();
        long date = file.lastModified();
        ImageObject imageObject = new ImageObject(currentPhotoPath, date, fileName);

        if(finalAlbumNameList.contains(albumName) ){

            AlbumData albumData = SharedPreferencesManager.loadAlbumData(this, albumName);
            Bitmap bitmap = null;
            bitmap= BitmapFactory.decodeFile(currentPhotoPath);
            if(bitmap==null)
            {
                albumData.removeImage(imageObject);
                SharedPreferencesManager.saveAlbumData(this, albumData);

            }

        }
    }

    public void setImageFragment(ImageFragment imageFragment) {
        this.imageFragment = imageFragment;
    }

    public void updateButtonInAlbum()
    {
        btnGallery.setEnabled(true);
        btnAlbum.setEnabled(true);
        btnCamera.setEnabled(true);
    }
}