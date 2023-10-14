package com.example.imagegallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.number.Scale;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
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
    private Button btnRotate, btnFlipHorizontal, btnFlipVertical;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float currentRotation = 0f;
    private Matrix matrix = new Matrix();
    private float scaleFactor = 1f;
    private float previousScaleFactor = 1f;
    private float translateX = 0f;
    private float translateY = 0f;
    private float previousTranslateX = 0f;
    private float previousTranslateY = 0f;

    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Time threshold for double-click in milliseconds
    private long lastClickTime = 0;

    private boolean isFlippedHorizontally = false;
    private boolean isFlippedVertically = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imageView = findViewById(R.id.imageView);
        ImageObject obj = (ImageObject) getIntent().getParcelableExtra("imageObject");
        obj.loadImage(this, imageView);

        imageView.setScaleType(ImageView.ScaleType.MATRIX);
        imageView.setImageMatrix(matrix);
        btnRotate = findViewById(R.id.btnRotate);
        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float centerX = imageView.getWidth() ;
                float centerY = imageView.getHeight() ;
                matrix.postRotate(currentRotation, centerX, centerY);
                rotate();
            }
        });

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        gestureDetector = new GestureDetector(this, new MoveListener());

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //double click to return to normal state
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    imageView.setScaleType(ImageView.ScaleType.MATRIX);

                    long clickTime = System.currentTimeMillis();
                    if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
                        scaleFactor = 1f;
                        translateX = 0f;
                        translateY = 0f;
                        matrix.reset();
                        imageView.setImageMatrix(matrix);
                    }
                    lastClickTime = clickTime;
                }
                //handling scale and move gesture
                scaleGestureDetector.onTouchEvent(motionEvent);
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        btnFlipHorizontal = findViewById(R.id.btnFlipHorizontal);
        btnFlipVertical = findViewById(R.id.btnFlipVertical);

        btnFlipVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFlippedVertically) {
                    isFlippedVertically = true;
                } else {
                    isFlippedVertically = false;
                }
                flip();
                imageView.setImageMatrix(matrix);
            }
        });
        btnFlipHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFlippedHorizontally) {
                    isFlippedHorizontally = true;
                } else {
                    isFlippedHorizontally = false;
                }
                flip();
                imageView.setImageMatrix(matrix);
            }
        });


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
                else if(R.id.add_to_album == itemId){
                    AlbumHelper.addImgaeToAlbum(this, obj);
                }


                return true;
            });
            //show popup menu
            popupMenu.show();
        });

    }
    private void rotate()
    {
        currentRotation =  (currentRotation + 90f);

        //rotate bitmap
/*        Drawable drawable = imageView.getDrawable();
        Bitmap bitmap =  rotateBitmap(((BitmapDrawable) drawable).getBitmap(), currentRotation);
        Log.d("after bitmap", bitmap.toString());
        imageView.setImageBitmap(bitmap);*/

        float centerX = imageView.getWidth() / 2f;
        float centerY = imageView.getHeight() / 2f;

        matrix.reset();
        matrix.postRotate(currentRotation, centerX, centerY);

        imageView.setImageMatrix(matrix);

        // Rotate the image animation
        //imageView.animate().rotation(currentRotation).setDuration(500).start();
    }
    private Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));


            if (scaleFactor > 1.0f) {
                matrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            } else {
                matrix.reset();
            }

            imageView.setImageMatrix(matrix);

            return true;
        }
    }

    private class MoveListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            translateX -= distanceX;
            translateY -= distanceY;

            float maxTranslateX = (imageView.getWidth() - (imageView.getWidth() / scaleFactor)) / 2;
            float maxTranslateY = (imageView.getHeight() - (imageView.getHeight() / scaleFactor)) / 2;

            translateX = Math.max(-maxTranslateX, Math.min(translateX, maxTranslateX));
            translateY = Math.max(-maxTranslateY, Math.min(translateY, maxTranslateY));

            matrix.postTranslate(-distanceX, -distanceY);
            imageView.setImageMatrix(matrix);

            Log.d("SCALE FACTOR", String.valueOf(scaleFactor));
            Log.d("TRANSLATE X", String.valueOf(translateX));
            Log.d("TRANSLATE Y", String.valueOf(translateY));
            Log.d("PREVIOUS SCALE FACTOR", String.valueOf(previousScaleFactor));
            Log.d("PREVIOUS TRANSLATE X", String.valueOf(previousTranslateX));
            Log.d("PREVIOUS TRANSLATE Y", String.valueOf(previousTranslateY));

            return true;
        }
    }

    private void flip()
    {
        float centerX = imageView.getWidth() / 2f;
        float centerY = imageView.getHeight() / 2f;
        float scaleX = isFlippedHorizontally ? -1 : 1;
        float scaleY = isFlippedVertically ? -1 : 1;

        matrix.setScale(scaleX, scaleY, centerX, centerY);
        matrix.postTranslate(0, imageView.getHeight());
        matrix.postScale(1, -1, centerX, centerY);

        isFlippedHorizontally = !isFlippedHorizontally;
        isFlippedVertically = !isFlippedVertically;

        imageView.setImageMatrix(matrix);
    }

}


