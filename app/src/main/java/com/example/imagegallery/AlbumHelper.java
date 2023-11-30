package com.example.imagegallery;


import static android.provider.Settings.System.getString;

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
    private static ArrayList<String> savedQuestions;
    Context context = MainActivity.getContext();

    private AlbumHelper(){
        defaultAlbums = new HashSet<>();
        questionList = new ArrayList<>();

        // add string from strings.xml resources to questionList (R.string.what_is_your_favorite_color)
        questionList.add(context.getString(R.string.what_is_your_favorite_color));
        questionList.add(context.getString(R.string.what_is_your_favorite_food));
        questionList.add(context.getString(R.string.what_is_your_favorite_movie));
        questionList.add(context.getString(R.string.what_is_your_favorite_animal));
        questionList.add(context.getString(R.string.what_is_your_favorite_sport));
        questionList.add(context.getString(R.string.what_is_your_favorite_book));
        questionList.add(context.getString(R.string.what_is_your_favorite_song));
        questionList.add(context.getString(R.string.what_is_your_favorite_game));
        questionList.add(context.getString(R.string.what_is_your_favorite_tv_show));
        questionList.add(context.getString(R.string.what_is_your_favorite_subject));

        savedQuestions = new ArrayList<>();
        savedQuestions.add("what is your favorite color");
        savedQuestions.add("what is your favorite food");
        savedQuestions.add("what is your favorite movie");
        savedQuestions.add("what is your favorite animal");
        savedQuestions.add("what is your favorite sport");
        savedQuestions.add("what is your favorite book");
        savedQuestions.add("what is your favorite song");
        savedQuestions.add("what is your favorite game");
        savedQuestions.add("what is your favorite tv show");
        savedQuestions.add("what is your favorite subject");


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
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String albumName = editName.getText().toString();

                if(albumName.length() != 0){

                    if(finalAlbumNameList.contains(albumName)){
                        AlbumData albumData = SharedPreferencesManager.loadAlbumData(context, albumName);

                        if(albumData.addImage(imageObject)){
                           albumData.setLastModifiedDate();
                           SharedPreferencesManager.saveAlbumData(context, albumData);
                           imageObject.addAlbumName(context,albumName);
                           Toast.makeText(context, context.getString(R.string.image_has_been_added_to) + albumName, Toast.LENGTH_SHORT).show();
                       }
                       else{
                           Toast.makeText(context, context.getString(R.string.image_already_exists_in_this_album), Toast.LENGTH_SHORT).show();
                       }
                    }
                    else{
                        Toast.makeText(context, context.getString(R.string.album_does_not_exist), Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(context,context.getString( R.string.album_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(context.getString(R.string.cancel), null);
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
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String albumName = editName.getText().toString();

                if(albumName.length() != 0){

                    if(finalAlbumNameList.contains(albumName)){

                        for(ImageObject imageObject : images){
                            AlbumData albumData = SharedPreferencesManager.loadAlbumData(context, albumName);

                            if(albumData.addImage(imageObject)){
                                albumData.setLastModifiedDate();
                                SharedPreferencesManager.saveAlbumData(context, albumData);
                                imageObject.addAlbumName(context,albumName);
                            }

                        }
                        Toast.makeText(context, context.getString(R.string.image_has_been_added_to) + albumName, Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(context, context.getString(R.string.album_does_not_exist), Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    Toast.makeText(context, context.getString(R.string.album_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.create();
        builder.show();
    }


    public void removeImageFromAlbum(Context context, ImageObject imageObject){
        MainActivity mainActivity = (MainActivity) context;
        String albumName = mainActivity.getCurrentFragementName();
        AlbumData albumData = SharedPreferencesManager.loadAlbumData(context, albumName);
        albumData.setLastModifiedDate();

        if(albumData.deleteImage(imageObject)){
            SharedPreferencesManager.saveAlbumData(context, albumData);
        }

    }

    public ArrayList<AlbumData> createDefaultAlbum(Context context){
        ArrayList<AlbumData> albums = new ArrayList<>();

        File externalStorage = Environment.getExternalStorageDirectory();
        File trashDirectory = new File(context.getExternalFilesDir(null), "Trash");
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
        SharedPreferencesManager.saveAlbumData(context, trash);


        AlbumData favorite = SharedPreferencesManager.loadAlbumData(context, "Favorites");


        if(favorite == null){
            favorite = new AlbumData("Favorites", R.drawable.ic_loved);
            albumHelper.addDefaultAlbum(favorite.getAlbumName());
            SharedPreferencesManager.saveAlbumData(context, favorite);
        }

        addDefaultAlbum(favorite.getAlbumName());
        albums.add(favorite);

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

        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(editPassword.getText().toString().equals(editConfirmPassword.getText().toString())){
                    SharedPreferencesManager.saveAlbumPassword(context, albumName, editPassword.getText().toString());
                    Toast.makeText(context, R.string.password_has_been_set, Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context, R.string.password_does_not_match, Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(context.getString(R.string.cancel), null);
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

            builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    if(SharedPreferencesManager.checkAlbumPassword(context, albumName, editPassword.getText().toString())){
                        Toast.makeText(context, R.string.password_is_correct, Toast.LENGTH_SHORT).show();
                        passwordCheckCallBack.onPasswordChecked(true);
                    }
                    else{
                        Toast.makeText(context, R.string.password_is_incorrect, Toast.LENGTH_SHORT).show();
                        passwordCheckCallBack.onPasswordChecked(false);

                    }
                }
            });

            builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
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

        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(answer.getText().toString().length() != 0){
                    String question = savedQuestions.get(questions.getSelectedItemPosition());
                    SharedPreferencesManager.saveSecurityQuestion(context, question, answer.getText().toString());
                    Toast.makeText(context, R.string.security_question_has_been_set, Toast.LENGTH_SHORT).show();
                    passwordCheckCallBack.onPasswordChecked(true);
                }
                else{
                    Toast.makeText(context, R.string.answer_cannot_be_empty, Toast.LENGTH_SHORT).show();
                    passwordCheckCallBack.onPasswordChecked(false);
                }
            }
        });

        builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
            Toast.makeText(context, R.string.entered_wrong_answer_5_times, Toast.LENGTH_SHORT).show();
            return;
        }

        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(answer.getText().toString().length() != 0){
                    String question = savedQuestions.get(questions.getSelectedItemPosition());

                    if(SharedPreferencesManager.checkSecurityQuestion(context, question, answer.getText().toString())){
                        setPassword(context, albumName);
                        SharedPreferencesManager.saveEnterWrongAnswerTimes(context, 0);
                    }
                    else{
                        Toast.makeText(context, R.string.security_question_answer_is_incorrect, Toast.LENGTH_SHORT).show();
                        SharedPreferencesManager.saveEnterWrongAnswerTimes(context, enterWrongAnswerTimes + 1);

                        if(enterWrongAnswerTimes == 4){
                            SharedPreferencesManager.saveTimeEnterWrongAnswer(context);
                        }
                    }
                }
                else{
                    Toast.makeText(context, R.string.answer_cannot_be_empty, Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(context.getString(R.string.cancel), null);
        builder.create();
        builder.show();
    }


}
