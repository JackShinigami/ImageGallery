package com.example.imagegallery;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;

import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
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
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;


public class DetailActivity extends AppCompatActivity  {

    private ImageView imageView;
    private ImageObject obj;
    private ImageView iv_love, iv_addtag;
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
    private Bitmap originalBitmap, flippedBitmap, rotatedBitmap, croppedBitmap, displayedBitmap;

    private float saturationVal = 1f;
    ColorMatrix color = new ColorMatrix();

    private ArrayList<String> tags = new ArrayList<>();
    private ArrayList<ImageObject> imgobjs = new ArrayList<>();

    ActivityResultLauncher<CropImageContractOptions> cropImage = registerForActivityResult(new CropImageContract(), result -> {
        if (result.isSuccessful()) {
            croppedBitmap = BitmapFactory.decodeFile(result.getUriFilePath(getApplicationContext(), true));
            imageView.setImageBitmap(croppedBitmap);
            displayedBitmap = croppedBitmap;
            //saveCroppedImage(croppedBitmap);
        }
    });
    private TaskCompletionSource<Void> tagsLoadingTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_detail);

            imageView = findViewById(R.id.imageView);
            obj = (ImageObject) getIntent().getParcelableExtra("imageObject");
            obj.loadImage(this, imageView);

            tags.clear();
            tagsLoadingTask = new TaskCompletionSource<>();
            tags = obj.getTags(this, tagsLoadingTask);
            Thread loadLatLong = new Thread(new Runnable() {
                @Override
                public void run() {
                    obj.loadLatLong(DetailActivity.this);
                }
            });
            loadLatLong.start();

            tagsLoadingTask.getTask().addOnCompleteListener(task -> {
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
                        if (R.id.add_tag == itemId) {
                            Dialog dialog = new Dialog(DetailActivity.this);
                            dialog.setContentView(R.layout.dialog_addtag);

                            TextView tv_tag = dialog.findViewById(R.id.et_tagname);
                            Button btn_submit = dialog.findViewById(R.id.btn_submit);

                            btn_submit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String tag = tv_tag.getText().toString();
                                    if (tag.isEmpty()) {
                                        Toast.makeText(DetailActivity.this, "Tag name cannot be empty", Toast.LENGTH_SHORT).show();
                                    } else if (tags.contains(tag)) {
                                        Toast.makeText(DetailActivity.this, "Tag name already exist", Toast.LENGTH_SHORT).show();
                                    } else {
                                        tags.add(tag);
                                        obj.addTag(DetailActivity.this, tag);
                                        dialog.dismiss();
                                    }
                                }
                            });

                            dialog.show();
                        } else if (R.id.remove_tag == itemId) {
                            Dialog dialog = new Dialog(DetailActivity.this);
                            dialog.setContentView(R.layout.dialog_addtag);

                            TextView tv_tag = dialog.findViewById(R.id.et_tagname);
                            Button btn_submit = dialog.findViewById(R.id.btn_submit);

                            btn_submit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String tag = tv_tag.getText().toString();
                                    if (tag.isEmpty()) {
                                        Toast.makeText(DetailActivity.this, "Tag name cannot be empty", Toast.LENGTH_SHORT).show();
                                    } else if (tags.contains(tag)) {
                                        tags.remove(tag);
                                        obj.removeTag(DetailActivity.this, tag);
                                        dialog.dismiss();
                                    } else {
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


            originalBitmap = BitmapFactory.decodeFile(obj.getFilePath());
            imageView.setImageBitmap(originalBitmap);

            displayedBitmap = originalBitmap;

            //cropping
            imgCrop = findViewById(R.id.imgCrop);
            imgCrop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getImageFile(obj.getFilePath());
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
                    Log.d("TAG", "onProgressChanged: " + i);
                    saturationVal = (float) i / 25;
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

                if (SharedPreferencesManager.loadCurrentName(this).equals("Trash")) {
                    popupMenu.getMenu().findItem(R.id.delete_image).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete_trash).setVisible(true);
                    popupMenu.getMenu().findItem(R.id.restore_image).setVisible(true);
                    popupMenu.getMenu().findItem(R.id.add_to_album).setVisible(false);
                } else {
                    popupMenu.getMenu().findItem(R.id.delete_image).setVisible(true);
                    popupMenu.getMenu().findItem(R.id.delete_trash).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.restore_image).setVisible(false);
                }


                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (R.id.set_wallpaper == itemId) {
                        Toast.makeText(this, "Set wallpaper", Toast.LENGTH_SHORT).show();
                        Wallpapersetter.setWallpaper(this, obj.getFilePath());
                    } else if (R.id.set_lockscreen == itemId) {
                        Toast.makeText(this, "Set lockscreen", Toast.LENGTH_SHORT).show();
                        Wallpapersetter.setLockScreen(this, obj.getFilePath());
                    } else if (R.id.share == itemId) {

                   /* Bitmap b = BitmapFactory.decodeFile(obj.getFilePath());
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("image/*");
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);*/
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("image/*");
                        MediaScannerConnection.scanFile(this,
                                new String[]{obj.getFilePath()}, null,
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
                    } else if (R.id.add_to_album == itemId) {
                        AlbumHelper albumHelper = AlbumHelper.getInstance(this);
                        albumHelper.addImageToAlbum(this, obj);
                    } else if (R.id.delete_image == itemId) {
                        Dialog dialog = new Dialog(this);
                        dialog.setContentView(R.layout.dialog_save_edited_image);
                        TextView txtTitle = dialog.findViewById(R.id.tv_message_dialog);
                        txtTitle.setText(R.string.delete_images_confirm);
                        Button btnYes = dialog.findViewById(R.id.btn_save);
                        Button btnNo = dialog.findViewById(R.id.btn_cancel);
                        btnYes.setText(R.string.delete);
                        btnNo.setText(R.string.cancel);
                        btnYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                obj.deleteToTrash(getApplicationContext());
                                if (SearchActivity.isSearchActivityRunning()) {
                                    SearchActivity.addDeleteImage(obj);
                                }
                                finish();
                                dialog.dismiss();
                            }
                        });
                        btnNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    } else if (R.id.delete_trash == itemId) {
                        Dialog dialog = new Dialog(this);
                        dialog.setContentView(R.layout.dialog_save_edited_image);
                        TextView txtTitle = dialog.findViewById(R.id.tv_message_dialog);
                        txtTitle.setText(R.string.delete_trashes_confirm);
                        Button btnYes = dialog.findViewById(R.id.btn_save);
                        Button btnNo = dialog.findViewById(R.id.btn_cancel);
                        btnYes.setText(R.string.delete);
                        btnNo.setText(R.string.cancel);
                        btnYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                obj.deleteFile(getApplicationContext());
                                finish();
                                dialog.dismiss();
                            }
                        });
                        btnNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.show();
                    } else if (R.id.restore_image == itemId) {
                        obj.restoreFile(this);
                        finish();
                    } else if ((R.id.qrscan) == itemId) {
                        String url = obj.getQRCodeContent(this);
                        if (IsValidUrl(url)) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            startActivity(intent);
                        }


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
                    } else if (R.id.extract_text==itemId) {
                        TextRecognizer recognizer =
                                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

                        InputImage image = InputImage.fromBitmap(BitmapFactory.decodeFile(obj.getFilePath()), 0);
                        recognizer.process(image)
                                .addOnSuccessListener(visionText -> {
                                    // Task completed successfully
                                    // ...
                                    String text = visionText.getText();
                                    Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
                                    //copy to clipboard
                                    String label = "text";
                                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText(label, text);
                                    clipboard.setPrimaryClip(clip);

                                })
                                .addOnFailureListener(e -> {
                                    // Task failed with an exception
                                    // ...
                                    Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                                });
                    }


                    return true;
                });

                popupMenu.show();
            });
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            Log.e("ErrorDetail", "onCreate: ", e);
            finish();
        }
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
        imageView.setScaleX(-imageView.getScaleX());
    }
    private void rotate(float val)
    {
        currentRotation =  (currentRotation + val)%360;
        imageView.setRotation(currentRotation);
    }

    private void saveFlippedBitmap()
    {
        Matrix matrix = new Matrix();
        matrix.setScale(-1, 1);
        // Create a new flipped bitmap based on the original bitmap
        flippedBitmap = Bitmap.createBitmap(displayedBitmap, 0, 0, displayedBitmap.getWidth(), displayedBitmap.getHeight(), matrix, true);
        displayedBitmap = flippedBitmap;
    }
    private void saveRotatedBitmap(float val)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(val);

        rotatedBitmap = Bitmap.createBitmap(displayedBitmap, 0, 0, displayedBitmap.getWidth(), displayedBitmap.getHeight(), matrix, true);
        displayedBitmap = rotatedBitmap;
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
    //interface for applying saturation filter
    private void applySaturationFilter(float val)
    {
        color.setSaturation(val);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(color);
        imageView.setColorFilter(filter);
    }
    private void saveColorFilter(float val)
    {
        Bitmap bitmap = displayedBitmap;
        Bitmap saturatedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());

        Canvas canvas = new Canvas(saturatedBitmap);
        Paint paint = new Paint();

        color.setSaturation(val);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(color);
        paint.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, paint);

        displayedBitmap = saturatedBitmap;
    }


    private void resetSaturation()
    {
        seekBarFilter.setProgress(25);
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

    private void saveImage(Bitmap bitmap) {
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

    private void overrideImage(Bitmap bitmap)
    {
        File newfile = new File(obj.getFilePath());
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(newfile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);


            // Compress and write the bitmap to the output stream
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close(); // Don't forget to close the output stream
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onBackPressed() {

        if(saturationVal != 1.0f) {
            saveColorFilter(saturationVal);
        }

        if (isFlippedHorizontally) {
            saveFlippedBitmap();
        }

        if (currentRotation != 0f) {
            saveRotatedBitmap(currentRotation);
        }

        if (!originalBitmap.sameAs(displayedBitmap))
        {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_save_edited_image);

            Button btn_save = dialog.findViewById(R.id.btn_save);
            Button btn_cancel = dialog.findViewById(R.id.btn_cancel);

            btn_save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveImage(displayedBitmap);
                    dialog.dismiss();
                    finish();
                }
            });

            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    finish();
                }
            });

            dialog.show();
        }
        else
            super.onBackPressed();
    }
}


