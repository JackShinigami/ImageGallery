package com.example.imagegallery;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentTransaction;

public class SettingActivity extends AppCompatActivity implements SettingPropertyChangedCallBacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // initialize the fragment
        inflateMainSettingFragment();

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


        if (theme.equals("Dark")) {
            SharedPreferencesManager.saveThemeState(getApplicationContext(), 0);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else if (theme.equals("Light")) {
            SharedPreferencesManager.saveThemeState(getApplicationContext(), 1);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (theme.equals("SameSystem")) {
            SharedPreferencesManager.saveThemeState(getApplicationContext(), 2);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
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
        if (lang.equals("English")) {
            SharedPreferencesManager.saveLanguageState(getApplicationContext(), 0);
            language = "en-US";
        } else if (lang.equals("Vietnamese")) {
            SharedPreferencesManager.saveLanguageState(getApplicationContext(), 1);
            language = "vi";
        }

        LocaleListCompat localeListCompat = LocaleListCompat.create(new Locale(language));
        AppCompatDelegate.setApplicationLocales(localeListCompat);

        recreate();
    }

    @Override
    public void recreate() {
        finish();
        // fade out animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        startActivity(getIntent());
    }
}

