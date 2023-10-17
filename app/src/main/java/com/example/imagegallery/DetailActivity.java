package com.example.imagegallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;



public class DetailActivity extends AppCompatActivity  {

    private ImageView imageView, iv_love;
    private Button btnRotate, btnFlipHorizontal, btnFlipVertical, btnFilter;
    private SeekBar seekBarFilter;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float currentRotation = 0f;
    private Matrix matrix = new Matrix();
    private float scaleFactor = 1f;
    private float initialScaleFactor = 1f;
    private float posX = 0f;
    private float posY = 0f;
    private float initialposX = 0f;
    private float initialposY = 0f;
    private boolean isFlippedHorizontally = false;
    private boolean isFlippedVertically = false;
    private Bitmap originalBitmap, flippedBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imageView = findViewById(R.id.imageView);
        ImageObject obj = (ImageObject) getIntent().getParcelableExtra("imageObject");
        obj.loadImage(this, imageView);

        iv_love = findViewById(R.id.iv_love);

        if(obj.isLoved(this))
            iv_love.setImageResource(R.drawable.ic_loved);
        else
            iv_love.setImageResource(R.drawable.ic_not_loved);

        iv_love.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(obj.isLoved(view.getContext())){
                    obj.setLoved(view.getContext(), false);
                    iv_love.setImageResource(R.drawable.ic_not_loved);

                    AlbumData favorite = SharedPreferencesManager.loadAlbumData(view.getContext(),"Favorites");
                    favorite.removeImage(obj);
                    SharedPreferencesManager.saveAlbumData(view.getContext(),favorite);
                }
                else{
                    obj.setLoved(view.getContext(),true);
                    iv_love.setImageResource(R.drawable.ic_loved);

                    AlbumData favorite = SharedPreferencesManager.loadAlbumData(view.getContext(),"Favorites");
                    favorite.addImage(obj);
                    SharedPreferencesManager.saveAlbumData(view.getContext(),favorite);
                }
            }
        });


        //filter
        btnFilter = findViewById(R.id.btnFilter);
        seekBarFilter = findViewById(R.id.seekBarFilter);
        resetSaturation();

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int visibility = seekBarFilter.getVisibility();
                seekBarFilter.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);
            }
        });

        seekBarFilter.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                float saturationVal = (float) i/50 ;
                applySaturationFilter(saturationVal);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        //zooming and panning
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        gestureDetector = new GestureDetector(this, new GestureListener());

        //rotating image
        btnRotate = findViewById(R.id.btnRotate);
        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotate();
            }
        });

        //flipping image
        originalBitmap = BitmapFactory.decodeFile(obj.getFilePath());
        flippedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(flippedBitmap);

        btnFlipHorizontal = findViewById(R.id.btnFlipHorizontal);
        btnFlipVertical = findViewById(R.id.btnFlipVertical);

        btnFlipVertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipImage();
            }
        });
        btnFlipHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flipImage();
            }
        });


        ImageView iv_more = findViewById(R.id.iv_more);
        iv_more.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, iv_more);
            popupMenu.getMenuInflater().inflate(R.menu.detail_image_popup, popupMenu.getMenu());

            if(SharedPreferencesManager.loadCurrentName(this).equals("Trash")){
                popupMenu.getMenu().findItem(R.id.delete_image).setVisible(false);
                popupMenu.getMenu().findItem(R.id.delete_trash).setVisible(true);
                popupMenu.getMenu().findItem(R.id.restore_image).setVisible(true);
            }
            else{
                popupMenu.getMenu().findItem(R.id.delete_image).setVisible(true);
                popupMenu.getMenu().findItem(R.id.delete_trash).setVisible(false);
                popupMenu.getMenu().findItem(R.id.restore_image).setVisible(false);
            }

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
                else if(R.id.delete_image == itemId) {
                    obj.deleteToTrash(this);
                    finish();
                }
                else if(R.id.delete_trash == itemId)
                {
                    obj.deleteFile(this);
                    finish();
                }
                else if(R.id.restore_image == itemId)
                {
                    obj.restoreFile(this);
                    finish();
                }


                return true;
            });

            popupMenu.show();
        });
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    private class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if(scaleFactor != 1f)
            {
                posX -= distanceX;
                posY -= distanceY;
                imageView.setTranslationX(posX);
                imageView.setTranslationY(posY);
                return true;
            }
            return false;
        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Handle any fling gestures (optional)
            return true;
        }
        @Override
        public void onLongPress(MotionEvent e) {
            // Handle long press (optional)
        }
        @Override
        public void onShowPress(MotionEvent e) {
            // Handle show press (optional)
        }
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // Handle single tap up (optional)
            return true;
        }
        @Override
        public boolean onDoubleTap(@NonNull MotionEvent e) {
            reset();
            Toast.makeText(DetailActivity.this, "Double tap", Toast.LENGTH_SHORT).show();
            return true;
        }
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(@NonNull ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));
            imageView.setScaleX(scaleFactor);
            imageView.setScaleY(scaleFactor);
            return true;
        }
    }
    private void flipImage()
    {
        isFlippedHorizontally = !isFlippedHorizontally;
        isFlippedVertically = !isFlippedVertically;

        if (isFlippedHorizontally || isFlippedVertically) {
            matrix.reset();
            matrix.setScale(isFlippedHorizontally ? -1 : 1, isFlippedVertically ? -1 : 1);


            // Create a new flipped bitmap based on the original bitmap
            flippedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

            imageView.setImageBitmap(flippedBitmap);
        } else {
            imageView.setImageBitmap(originalBitmap);
        }
    }
    private void rotate()
    {
        currentRotation =  (currentRotation + 90f);
        imageView.animate().rotation(currentRotation).setDuration(500).start();
    }

    private void reset()
    {
        matrix.reset();

        currentRotation = 0;
        flippedBitmap = originalBitmap;
        imageView.setImageBitmap(originalBitmap);

        posX = initialposX;
        posY = initialposY;
        scaleFactor = initialScaleFactor;

        imageView.setTranslationX(posX);
        imageView.setTranslationY(posY);
        imageView.setScaleX(scaleFactor);
        imageView.setScaleY(scaleFactor);

        resetSaturation();
    }
    private void applySaturationFilter(float val)
    {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(val);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        imageView.setColorFilter(filter);
    }

    private void resetSaturation()
    {
        seekBarFilter.setProgress(50);
        applySaturationFilter(1);
    }
}


