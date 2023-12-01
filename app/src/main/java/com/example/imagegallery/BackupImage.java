package com.example.imagegallery;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class BackupImage {

    public static void uploadImage(Context context, ImageObject imageObject){
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                String token = task.getResult();
                Log.d("TOKEN", token);

                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                StorageReference riversRef = mStorageRef.child(token + "/" + imageObject.getFileName());
                Uri file = Uri.fromFile(new File(imageObject.getFilePath()));
                UploadTask uploadTask = riversRef.putFile(file);
                uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        // Update progress bar
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.d("UPLOAD", "Upload is " + progress + "% done");
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload successful
                        Log.d("UPLOAD", "Upload successful");
                        Toast.makeText(context, R.string.backup_success, Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.e("UPLOAD", "Upload failed", exception);
                        Toast.makeText(context, R.string.backup_fail, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

public static void downloadImage(Context context, TaskCompletionSource<Void> taskCompletionSource){

        File externalFilesDir = Environment.getExternalStorageDirectory();
        File pictures = new File(externalFilesDir, "Pictures");
        File downloadDir = new File(pictures, "Backup");

        Log.d("DOWNLOAD", "Download directory: " + downloadDir.getAbsolutePath());

        if(!downloadDir.exists()){
            downloadDir.mkdir();
        }


        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                String token = task.getResult();
                Log.d("TOKEN", token);

                StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
                StorageReference riversRef = mStorageRef.child(token);

                riversRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        int count = 0;

                        for(StorageReference imageRef : listResult.getItems()){
                            String name = imageRef.getName().toLowerCase();
                            if(name.endsWith(".jpg") || name.endsWith(".png") ||
                               name.endsWith(".jpeg") || name.endsWith(".gif") ||
                               name.endsWith(".webp") || name.endsWith(".heic")){
                                count++;
                            }
                        }

                        final int numFiles = count;
                        count = 0;

                        for(StorageReference imageRef : listResult.getItems()){

                            String name = imageRef.getName().toLowerCase();
                            if(name.endsWith(".jpg") || name.endsWith(".png") ||
                                    name.endsWith(".jpeg") || name.endsWith(".gif") ||
                                    name.endsWith(".webp") || name.endsWith(".heic")){
                                count++;
                                final int finalCount = count;
                                File file = new File(downloadDir, imageRef.getName());
                                imageRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        Log.d("DOWNLOAD", "Download successful");

                                        if(finalCount == numFiles){
                                            taskCompletionSource.setResult(null);
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Log.e("DOWNLOAD", "Download failed", exception);

                                        if(finalCount == numFiles){
                                            taskCompletionSource.setResult(null);
                                        }// if
                                    }// onFailure
                                });// addOnFailureListener
                            } // if
                        }// for
                    }// onSuccess
                });// listAll
            } // onComplete
        }); // getToken
    }// downloadImage
}// BackupImage
