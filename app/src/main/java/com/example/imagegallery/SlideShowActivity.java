package com.example.imagegallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
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
    private boolean userScrollChange = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_show);

        viewPager2 = findViewById(R.id.viewPager2);
        ArrayList<ImageObject> imageObjects = getIntent().getParcelableArrayListExtra("images");
       // viewPager2.setAdapter(new SlideShowAdapter(this, imageObjects));
        viewPager2.setAdapter(new SlideShowAdapter(this, imageObjects));
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                userScrollChange = state != ViewPager2.SCROLL_STATE_IDLE;
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                if(!userScrollChange) {
                    if (viewPager2.getCurrentItem() == imageObjects.size() - 1) {
                        viewPager2.setCurrentItem(0);
                    } else {
                        viewPager2.setCurrentItem(viewPager2.getCurrentItem() + 1);
                    }
                }
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