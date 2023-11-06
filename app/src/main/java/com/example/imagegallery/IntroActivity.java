package com.example.imagegallery;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class IntroActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Nếu quyền chưa được cấp, hiển thị pop-up xin cấp quyền
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_MEDIA_IMAGES,
                            android.Manifest.permission.CAMERA, android.Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_NETWORK_STATE, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_MEDIA_LOCATION}, 1);
        }
        else {
            Intent myIntent = new Intent(IntroActivity.this, MainActivity.class);
            startActivity(myIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1)
        {
            for(int i = 0; i < permissions.length; i++)
            {
                Log.d("PERMISSION", permissions[i] + " " + grantResults[i]);
            }
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED || grantResults[2] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Đã cấp quyền", Toast.LENGTH_SHORT).show();
                Intent myIntent = new Intent(IntroActivity.this, MainActivity.class);
                startActivity(myIntent);
            }
            else
            {
                Toast.makeText(this, "Chưa cấp quyền", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

}