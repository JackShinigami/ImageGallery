package com.example.imagegallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DetailActivity extends AppCompatActivity {

    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imageView = findViewById(R.id.imageView);
        ImageObject obj = (ImageObject) getIntent().getParcelableExtra("imageObject");
        obj.loadImage(this, imageView);


        ImageView iv_more = findViewById(R.id.iv_more);
        iv_more.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, iv_more);
            popupMenu.getMenuInflater().inflate(R.menu.detail_image_popup, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if(R.id.set_wallpaper == itemId){
                    Toast.makeText(this, "Set wallpaper", Toast.LENGTH_SHORT).show();
                    Wallpapersetter.setWallpaper(this, obj.getFilePath());
                }
                else if(R.id.set_lockscreen == itemId){
                    Toast.makeText(this, "Set lockscreen", Toast.LENGTH_SHORT).show();
                    Wallpapersetter.setLockScreen(this, obj.getFilePath());
                }
                else if(R.id.share == itemId){

                   /* Bitmap b = BitmapFactory.decodeFile(obj.getFilePath());
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);*/
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    MediaScannerConnection.scanFile(this,
                            new String[] {obj.getFilePath() }, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                public void onScanCompleted(String path, Uri uri) {
                                    share.putExtra(Intent.EXTRA_STREAM, uri);
                                    startActivity(Intent.createChooser(share, "Select"));
                                }
                            });
                    /*String path = MediaStore.Images.Media.insertImage(getContentResolver(), b, "Title", null);
                    Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
                    Uri imageUri =  Uri.parse(path);
                    share.putExtra(Intent.EXTRA_STREAM, imageUri);
                    startActivity(Intent.createChooser(share, "Select"));*/
                }

                return true;
            });
            //shơw popup menu
            popupMenu.show();
        });

    }
}