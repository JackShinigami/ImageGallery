package com.example.imagegallery;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AlbumHelper {

    private static Set<String> defaultAlbums;
    private static AlbumHelper albumHelper;
    
    private static boolean isSecurityQuestionSet = false;

    private static ArrayList<String> questionList;

    private AlbumHelper(){
        defaultAlbums = new HashSet<>();
        questionList = new ArrayList<>();
        questionList.add("What is your favorite color?");
        questionList.add("What is your favorite food?");
        questionList.add("What is your favorite movie?");
        questionList.add("What is your favorite animal?");
        questionList.add("What is your favorite sport?");
        questionList.add("What is your favorite book?");
        questionList.add("What is your favorite song?");
        questionList.add("What is your favorite game?");
        questionList.add("What is your favorite TV show?");
        questionList.add("What is your favorite subject?");

    }

    public static AlbumHelper getInstance(){
        if(albumHelper == null){
            albumHelper = new AlbumHelper();
        }
        return albumHelper;
    }

    public void addDefaultAlbum(String albumName){
        defaultAlbums.add(albumName);
    }

    public boolean isDefaultAlbum(String albumName){
        return defaultAlbums.contains(albumName);
    }

    public void addImageToAlbum(Context context, ImageObject imageObject){
        ArrayList<String> albumNameList = SharedPreferencesManager.loadAlbumNameList(context);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, albumNameList);
        if(albumNameList == null){
            albumNameList = new ArrayList<>();
        }

        View view = LayoutInflater.from(context).inflate(R.layout.add_image_to_album, null);
        AutoCompleteTextView editName = view.findViewById(R.id.edit_album_name);
        editName.setAdapter(arrayAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        ArrayList<String> finalAlbumNameList = albumNameList;
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String albumName = editName.getText().toString();
                if(albumName.length() != 0){
                    if(finalAlbumNameList.contains(albumName)){
                        AlbumData albumData = SharedPreferencesManager.loadAlbumData(context, albumName);
                       if(albumData.addImage(imageObject)){
                           SharedPreferencesManager.saveAlbumData(context, albumData);
                           imageObject.addAlbumName(context,albumName);
                           Toast.makeText(context, "Image has been added to " + albumName, Toast.LENGTH_SHORT).show();
                       }
                       else{
                           Toast.makeText(context, "Image already exists in this album", Toast.LENGTH_SHORT).show();
                       }
                    }
                    else{
                        AlbumData albumData = new AlbumData(albumName);
                        albumData.addImage(imageObject);
                        SharedPreferencesManager.saveAlbumData(context, albumData);
                        imageObject.addAlbumName(context,albumName);
                        finalAlbumNameList.add(albumName);
                        SharedPreferencesManager.saveAlbumNameList(context, finalAlbumNameList);
                        Toast.makeText(context, "Image has been added to " + albumName, Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(context, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create();
        builder.show();
    }

    public void addImagesToAlbum(Context context, ArrayList<ImageObject> images){
        ArrayList<String> albumNameList = SharedPreferencesManager.loadAlbumNameList(context);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, albumNameList);
        if(albumNameList == null){
            albumNameList = new ArrayList<>();
        }

        View view = LayoutInflater.from(context).inflate(R.layout.add_image_to_album, null);
        AutoCompleteTextView editName = view.findViewById(R.id.edit_album_name);
        editName.setAdapter(arrayAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        ArrayList<String> finalAlbumNameList = albumNameList;
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String albumName = editName.getText().toString();
                if(albumName.length() != 0){
                    if(finalAlbumNameList.contains(albumName)){
                        for(ImageObject imageObject : images){
                            AlbumData albumData = SharedPreferencesManager.loadAlbumData(context, albumName);
                            if(albumData.addImage(imageObject)){
                                SharedPreferencesManager.saveAlbumData(context, albumData);
                                imageObject.addAlbumName(context,albumName);
                            }

                        }
                        Toast.makeText(context, "Images have been add to album " + albumName, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(context, "Album does not exist", Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    Toast.makeText(context, "Album name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create();
        builder.show();
    }


    public void removeImageFromAlbum(Context context, ImageObject imageObject){
        MainActivity mainActivity = (MainActivity) context;
        String albumName = mainActivity.getCurrentFragementName();
        AlbumData albumData = SharedPreferencesManager.loadAlbumData(context, albumName);

        if(albumData.deleteImage(imageObject)){
            SharedPreferencesManager.saveAlbumData(context, albumData);
            Toast.makeText(context, "Image has been deleted from " + albumName, Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(context, "Image does not exist in this album", Toast.LENGTH_SHORT).show();
        }
    }

    public ArrayList<AlbumData> createDefaultAlbum(Context context){
        ArrayList<AlbumData> albums = new ArrayList<>();

        File externalStorage = Environment.getExternalStorageDirectory();
        File trashDirectory = new File(externalStorage, "Trash");
        ArrayList<ImageObject> trashImages = new ArrayList<>();
        if(!trashDirectory.exists()) {
            trashDirectory.mkdir();
        }
        else{
            trashImages = new ArrayList<>();
            ImageObject.getImage(context, trashDirectory, trashImages);
        }

        AlbumData trash = new AlbumData("Trash", trashImages, R.drawable.ic_trash);
        addDefaultAlbum(trash.getAlbumName());
        albums.add(trash);

        AlbumData favorite = SharedPreferencesManager.loadAlbumData(context, "Favorites");


        if(favorite == null){
            favorite = new AlbumData("Favorites", R.drawable.ic_favorite);
            albumHelper.addDefaultAlbum(favorite.getAlbumName());
            SharedPreferencesManager.saveAlbumData(context, favorite);
        }

        addDefaultAlbum(favorite.getAlbumName());
        albums.add(favorite);

        SharedPreferencesManager.saveAlbumData(context, new AlbumData("Trash", trashImages));
        return albums;
    }

    public void setAlbumPassword(Context context, String albumName){
        isSecurityQuestionSet = SharedPreferencesManager.isSecurityQuestionSet(context);
        if(SharedPreferencesManager.hasSetPassword(context, albumName)){
            checkAlbumPassword(context, albumName, new PasswordCheckCallBack() {
                @Override
                public void onPasswordChecked(boolean isPasswordCorrect) {
                    if(isPasswordCorrect){
                        setPassword(context, albumName);
                    }
                }
            });
        }
        else {
            if(!isSecurityQuestionSet){
                setSecurityQuestion(context, new PasswordCheckCallBack() {
                    @Override
                    public void onPasswordChecked(boolean isPasswordCorrect) {
                        if(isPasswordCorrect){
                            setPassword(context, albumName);
                        }
                    }
                });


            }
            else{
                setPassword(context, albumName);
            }

        }

    }

    private void setPassword(Context context, String albumName) {
        View view = LayoutInflater.from(context).inflate(R.layout.set_password, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        EditText editPassword = view.findViewById(R.id.edit_password);
        EditText editConfirmPassword = view.findViewById(R.id.retype_password);
        TextView forgetPassword = view.findViewById(R.id.forget_password);
        forgetPassword.setVisibility(View.GONE);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(editPassword.getText().toString().equals(editConfirmPassword.getText().toString())){
                    SharedPreferencesManager.saveAlbumPassword(context, albumName, editPassword.getText().toString());
                    Toast.makeText(context, "Password has been set", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context, "Password does not match", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create();
        builder.show();
    }

    public void checkAlbumPassword(Context context, String albumName, PasswordCheckCallBack passwordCheckCallBack){

        if(SharedPreferencesManager.hasSetPassword(context, albumName)){
            View view = LayoutInflater.from(context).inflate(R.layout.set_password, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setView(view);
            EditText editPassword = view.findViewById(R.id.edit_password);
            TextView title = view.findViewById(R.id.txtTitle);
            TextView forgetPassword = view.findViewById(R.id.forget_password);

            forgetPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resetAlbumPassword(context, albumName);
                }
            });

            LinearLayout retypePassword = view.findViewById(R.id.retype_password_layout);
            title.setText("Enter Current Password");
            retypePassword.setVisibility(View.GONE);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(SharedPreferencesManager.checkAlbumPassword(context, albumName, editPassword.getText().toString())){
                        Toast.makeText(context, "Password is correct", Toast.LENGTH_SHORT).show();
                        passwordCheckCallBack.onPasswordChecked(true);
                    }
                    else{
                        Toast.makeText(context, "Password is incorrect", Toast.LENGTH_SHORT).show();
                        passwordCheckCallBack.onPasswordChecked(false);

                    }
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    passwordCheckCallBack.onPasswordChecked(false);
                }
            });
            builder.create();
            builder.show();


        }
        else{
            passwordCheckCallBack.onPasswordChecked(true);
        }


    }

    public void setSecurityQuestion(Context context, PasswordCheckCallBack passwordCheckCallBack){
        View view = LayoutInflater.from(context).inflate(R.layout.set_reset_password_form, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        Spinner questions = view.findViewById(R.id.security_question_spinner);
        EditText answer = view.findViewById(R.id.security_question_answer);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, questionList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        questions.setAdapter(arrayAdapter);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(answer.getText().toString().length() != 0){
                    SharedPreferencesManager.saveSecurityQuestion(context, questions.getSelectedItem().toString(), answer.getText().toString());
                    Toast.makeText(context, "Security question has been set", Toast.LENGTH_SHORT).show();
                    passwordCheckCallBack.onPasswordChecked(true);
                }
                else{
                    Toast.makeText(context, "Answer cannot be empty", Toast.LENGTH_SHORT).show();
                    passwordCheckCallBack.onPasswordChecked(false);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                    passwordCheckCallBack.onPasswordChecked(false);
            }
        });
        builder.create();
        builder.show(); }

    public void resetAlbumPassword(Context context, String albumName){

        if(SharedPreferencesManager.getEnterWrongAnswerTimes(context) == 5){
            long time = SharedPreferencesManager.getTimeEnterWrongAnswer(context);
            if(time != 0){
                long currentTime = System.currentTimeMillis();
                if(currentTime - time > 300000){
                    SharedPreferencesManager.saveEnterWrongAnswerTimes(context, 0);
                }
            }
        }

        View view = LayoutInflater.from(context).inflate(R.layout.set_reset_password_form, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        TextView txtTitle = view.findViewById(R.id.txtTitle);
        txtTitle.setText("Reset Password");
        Spinner questions = view.findViewById(R.id.security_question_spinner);
        EditText answer = view.findViewById(R.id.security_question_answer);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, questionList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        questions.setAdapter(arrayAdapter);

        int enterWrongAnswerTimes = SharedPreferencesManager.getEnterWrongAnswerTimes(context);

        if(enterWrongAnswerTimes == 5){
            Toast.makeText(context, "You have entered wrong answer 5 times. Please try again later", Toast.LENGTH_SHORT).show();
            return;
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(answer.getText().toString().length() != 0){
                    if(SharedPreferencesManager.checkSecurityQuestion(context, questions.getSelectedItem().toString(), answer.getText().toString())){
                        setPassword(context, albumName);
                        SharedPreferencesManager.saveEnterWrongAnswerTimes(context, 0);
                    }
                    else{
                        Toast.makeText(context, "Security question answer is incorrect", Toast.LENGTH_SHORT).show();
                        SharedPreferencesManager.saveEnterWrongAnswerTimes(context, enterWrongAnswerTimes + 1);
                        if(enterWrongAnswerTimes == 4){
                            SharedPreferencesManager.saveTimeEnterWrongAnswer(context);
                        }
                    }
                }
                else{
                    Toast.makeText(context, "Answer cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create();
        builder.show();
    }


}
