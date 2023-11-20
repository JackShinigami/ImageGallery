package com.example.imagegallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Adapter;

import com.example.imagegallery.R;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.logging.LoggingPermission;

public class SlideShowActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    int currentposition = 0;
    private Runnable runnable;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_show);

        viewPager2 = findViewById(R.id.viewPager2);
        ArrayList<ImageObject> imageObjects = getIntent().getParcelableArrayListExtra("images");
       // viewPager2.setAdapter(new SlideShowAdapter(this, imageObjects));
        viewPager2.setAdapter(new SlideShowAdapter(this, imageObjects));

        runnable = new Runnable() {
            @Override
            public void run() {
                if(currentposition >= imageObjects.size()){
                    currentposition = 0;
                }
                viewPager2.setCurrentItem(currentposition++, true);
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(handler!=null && runnable!=null){
            handler.removeCallbacks(runnable);
        }
    }
}