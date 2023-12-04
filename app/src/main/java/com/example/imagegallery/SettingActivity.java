package com.example.imagegallery;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.fragment.app.FragmentTransaction;

import java.util.Locale;

public class SettingActivity extends AppCompatActivity implements SettingPropertyChangedCallBacks {


    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        context = this;

        // initialize the fragment
        inflateMainSettingFragment();

    }

    public static Context getContext() {
        return context;
    }

    /**
     * Inflate the main setting fragment
     */
    private void inflateMainSettingFragment() {

        SettingItemsFragment settingItemsFragment = new SettingItemsFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.option_fragment_container, new SettingItemsFragment());
        transaction.commit();

    }



    @Override
    public void onThemeChanged(String theme) {
        // change theme of the app

        switch (theme) {
            case "Dark":
                SharedPreferencesManager.saveThemeState(getApplicationContext(), 0);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "Light":
                SharedPreferencesManager.saveThemeState(getApplicationContext(), 1);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "SameSystem":
                SharedPreferencesManager.saveThemeState(getApplicationContext(), 2);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }

        recreate();

        // while changing theme, create a progress dialog
        // and dismiss it after theme is changed

    }

    @Override
    public void onLanguageChanged(String lang) {
        // change language of the app
        int languageState = SharedPreferencesManager.loadLanguageState(getApplicationContext());
        String language = "";
        switch (lang) {
            case "English":
                SharedPreferencesManager.saveLanguageState(getApplicationContext(), 0);
                language = "en-US";
                break;
            case "Vietnamese":
                SharedPreferencesManager.saveLanguageState(getApplicationContext(), 1);
                language = "vi";
                break;
        }

        LocaleListCompat localeListCompat = LocaleListCompat.create(new Locale(language));
        AppCompatDelegate.setApplicationLocales(localeListCompat);

        // khởi động lại app
        Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if(context instanceof Activity){
            ((Activity) context).finishAffinity();
        }
    }

    @Override
    public void recreate() {
        finish();
        // fade out animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        startActivity(getIntent());
    }
}

