package com.example.imagegallery;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;

import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;


public class DetailActivity extends AppCompatActivity  {

    private ImageView imageView, iv_love, iv_addtag;
    private ImageView imgRotate, imgFlip, imgFilter, imgCrop;
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
    private Bitmap originalBitmap, flippedBitmap, rotatedBitmap;

    private ArrayList<String> tags = new ArrayList<>();

    ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
        if (result.isSuccessful()) {
            Bitmap cropped = BitmapFactory.decodeFile(result.getUriFilePath(getApplicationContext(), true));
            saveCroppedImage(cropped);
        }
    });
    private TaskCompletionSource<Void> tagsLoadingTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imageView = findViewById(R.id.imageView);
        ImageObject obj = (ImageObject) getIntent().getParcelableExtra("imageObject");
        obj.loadImage(this, imageView);

        tags.clear();
        tagsLoadingTask = new TaskCompletionSource<>();
        tags = obj.getTags(this, tagsLoadingTask);
        obj.loadLatLong(this);

        tagsLoadingTask.getTask().addOnCompleteListener(task ->{
            Toast.makeText(this, "Tags loaded", Toast.LENGTH_SHORT).show();
            tags = obj.getTags(this, tagsLoadingTask);
            Log.d("TAG", "onCreate: " + tags.toString());
        });

        iv_addtag = findViewById(R.id.iv_tag);
        iv_addtag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagsLoadingTask = new TaskCompletionSource<>();
                tags = obj.getTags(DetailActivity.this, tagsLoadingTask);
                Log.d("TAG", "onCreate: " + tags.toString());
                PopupMenu popupMenu = new PopupMenu(DetailActivity.this, iv_addtag);
                popupMenu.getMenuInflater().inflate(R.menu.detail_tag_popup, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if(R.id.add == itemId){
                        Dialog dialog = new Dialog(DetailActivity.this);
                        dialog.setContentView(R.layout.dialog_addtag);

                        TextView tv_tag = dialog.findViewById(R.id.et_tagname);
                        Button btn_submit = dialog.findViewById(R.id.btn_submit);

                        btn_submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String tag = tv_tag.getText().toString();
                                if(tag.isEmpty()){
                                    Toast.makeText(DetailActivity.this, "Tag name cannot be empty", Toast.LENGTH_SHORT).show();
                                }
                                else if (tags.contains(tag)){
                                    Toast.makeText(DetailActivity.this, "Tag name already exist", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    tags.add(tag);
                                    obj.addTag(DetailActivity.this, tag);
                                    dialog.dismiss();
                                }
                            }
                        });

                        dialog.show();
                    }
                    else if(R.id.remove == itemId){
                        Dialog dialog = new Dialog(DetailActivity.this);
                        dialog.setContentView(R.layout.dialog_addtag);

                        TextView tv_tag = dialog.findViewById(R.id.et_tagname);
                        Button btn_submit = dialog.findViewById(R.id.btn_submit);

                        btn_submit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String tag = tv_tag.getText().toString();
                                if(tag.isEmpty()){
                                    Toast.makeText(DetailActivity.this, "Tag name cannot be empty", Toast.LENGTH_SHORT).show();
                                }
                                else if (tags.contains(tag)){
                                    tags.remove(tag);
                                    obj.removeTag(DetailActivity.this, tag);
                                    dialog.dismiss();                                }
                                else {
                                    Toast.makeText(DetailActivity.this, "Tag name not exist", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                        dialog.show();
                    }
                    return true;
                });


                popupMenu.show();


            }
        });


/*        Log.d("IIMAGE", obj.getFilePath());
        Log.d("IIMAGE", obj.getFileName());
        Log.d("IIMAGE", obj.getAlbumNames(this).toString());
        Log.d("IIMAGE", obj.getTags(this, tagsLoadingTask).toString());
        Log.d("IIMAGE", obj.getAddress(this));*/

            iv_love = findViewById(R.id.iv_love);

            if(SharedPreferencesManager.loadCurrentName(this).equals("Trash"))
                iv_love.setVisibility(View.GONE);



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

        //cropping
        imgCrop = findViewById(R.id.imgCrop);
        imgCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getImageFile(obj.getFilePath());
                Log.d("filepath", obj.getFilePath());
            }
        });
        //filter
        imgFilter = findViewById(R.id.imgFilter);
        seekBarFilter = findViewById(R.id.seekBarFilter);
        resetSaturation();

        imgFilter.setOnClickListener(new View.OnClickListener() {
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
        imgRotate = findViewById(R.id.imgRotate);
        imgRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotate(90f);
            }
        });

        //flipping image
        originalBitmap = BitmapFactory.decodeFile(obj.getFilePath());
        imageView.setImageBitmap(originalBitmap);

        imgFlip = findViewById(R.id.imgFlip);

        imgFlip.setOnClickListener(new View.OnClickListener() {
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
                popupMenu.getMenu().findItem(R.id.add_to_album).setVisible(false);
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
//                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), b, "Title", null);
//                    Toast.makeText(this, path, Toast.LENGTH_SHORT).show();
//                    Uri imageUri =  Uri.parse(path);
//                    share.putExtra(Intent.EXTRA_STREAM, imageUri);
//                    startActivity(Intent.createChooser(share, "Select"));
                }
                else if(R.id.add_to_album == itemId){
                    AlbumHelper albumHelper = AlbumHelper.getInstance();
                    albumHelper.addImageToAlbum(this, obj);
                }
                else if(R.id.delete_image == itemId) {
                    obj.deleteToTrash(this);
                    if(SearchActivity.isSearchActivityRunning())
                    {
                        SearchActivity.addDeleteImage(obj);
                    }
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
                } else if((R.id.qrscan)==itemId)  {
                    String url=obj.getQRCodeContent(this);
                    if(IsValidUrl(url)){
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);}


                } else if (R.id.labeling == itemId) {

                    InputImage image = InputImage.fromBitmap(BitmapFactory.decodeFile(obj.getFilePath()), 0);

                    ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);

                    labeler.process(image)
                            .addOnSuccessListener(labels -> {
                                // Task completed successfully
                                // ...
                                String text = "";
                                for (com.google.mlkit.vision.label.ImageLabel label : labels) {
                                    String eachLabel = label.getText();
                                    float confidence = label.getConfidence();
                                    text += eachLabel + " " + confidence + "\n";
                                }
                                Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                // Task failed with an exception
                                // ...
                                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                            });

                } else if (R.id.info_image == itemId) {
                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.dialog_info_image);

                    TextView img_name = dialog.findViewById(R.id.txtName);
                    TextView img_path = dialog.findViewById(R.id.txtPath);
                    TextView img_Location = dialog.findViewById(R.id.txtLocation);
                    TextView img_tags = dialog.findViewById(R.id.txtTag);
                    TextView img_date = dialog.findViewById(R.id.txtDate);


                    img_name.setText(obj.getFileName());
                    img_path.setText(obj.getFilePath());
                    img_Location.setText(obj.getAddress(this));

                    img_tags.setText(obj.getTags(this, tagsLoadingTask).toString());

                    Date date = new Date(obj.getLastModifiedDate());
                    img_date.setText(date.toString());



                    dialog.show();
                }


                return true;
            });

            popupMenu.show();
        });
    }

    public static boolean IsValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            return URLUtil.isValidUrl(urlString) && Patterns.WEB_URL.matcher(urlString).matches();
        } catch (MalformedURLException ignored) {
        }
        return false;
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
            if(scaleFactor >= 1f)
            {
                posX -= distanceX;
                posY -= distanceY;

                // block image from going out of screen
                if(posX > imageView.getWidth()*scaleFactor - imageView.getWidth())
                    posX = imageView.getWidth()*scaleFactor - imageView.getWidth();
                if(posY > imageView.getHeight()*scaleFactor - imageView.getHeight())
                    posY = imageView.getHeight()*scaleFactor - imageView.getHeight();
                if(posX < imageView.getWidth() - imageView.getWidth()*scaleFactor)
                    posX = imageView.getWidth() - imageView.getWidth()*scaleFactor;
                if(posY < imageView.getHeight() - imageView.getHeight()*scaleFactor)
                    posY = imageView.getHeight() - imageView.getHeight()*scaleFactor;


                imageView.setTranslationX(posX);
                imageView.setTranslationY(posY);
                return true;
            } else {
                imageView.setTranslationX(initialposX);
                imageView.setTranslationY(initialposY);
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
        flippedBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

        isFlippedHorizontally = !isFlippedHorizontally;
        if (isFlippedHorizontally) {
            matrix.reset();
            matrix.setScale(-1, 1);
            // Create a new flipped bitmap based on the original bitmap
            flippedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(flippedBitmap);
        } else {
            imageView.setImageBitmap(originalBitmap);
        }
    }
    private void rotate(float val)
    {
        currentRotation =  (currentRotation + val);
        //imageView.animate().rotation(currentRotation).setDuration(500).start();
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        Matrix matrix = new Matrix();
        matrix.postRotate(val);

        rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        imageView.setImageBitmap(rotatedBitmap);

    }

    private void reset()
    {
        matrix.reset();

        imageView.setImageMatrix(matrix);
        rotate(-currentRotation);
        currentRotation = 0f;

        posX = initialposX;
        posY = initialposY;
        scaleFactor = initialScaleFactor;

        imageView.setTranslationX(posX);
        imageView.setTranslationY(posY);
        imageView.setScaleX(scaleFactor);
        imageView.setScaleY(scaleFactor);

        if(isFlippedHorizontally) {
            flipImage();
            isFlippedHorizontally = false;
        }

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

    private void startCrop(Uri uri) {
        CropImageOptions cropImageOptions = new CropImageOptions();
        cropImageOptions.imageSourceIncludeGallery = false;
        cropImageOptions.imageSourceIncludeCamera = true;
        CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(uri, cropImageOptions);
        cropImage.launch(cropImageContractOptions);
    }
    private void getImageFile(String filepath) {
        File file = new File(filepath);
        Uri uri = Uri.fromFile(file);

        startCrop(uri);
    }

    private void saveCroppedImage(Bitmap bitmap) {
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + "/Cropped Images");


        if (!myDir.exists()) {
            myDir.mkdirs();
        }

        // Generate a unique file name
        String imageName = "Image_" + new Date().getTime() + ".jpg";

        File file = new File(myDir, imageName);
        if (file.exists()) file.delete();

        try {
            // Save the Bitmap to the file
            OutputStream outputStream;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                outputStream = Files.newOutputStream(file.toPath());
            } else {
                outputStream = new FileOutputStream(file);
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Add the image to the MediaStore
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);



        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }
}


