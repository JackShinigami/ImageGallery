package com.example.imagegallery;

import android.os.Bundle;
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
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            Toast.makeText(getApplicationContext(),"Dark chosen", Toast.LENGTH_SHORT).show();
        } else if (theme.equals("Light")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            Toast.makeText(getApplicationContext(),"Light chosen", Toast.LENGTH_SHORT).show();
        } else if (theme.equals("SameSystem")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            Toast.makeText(getApplicationContext(),"Follow system chosen", Toast.LENGTH_SHORT).show();
        }
    }
}

