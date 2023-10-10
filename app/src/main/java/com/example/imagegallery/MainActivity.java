package com.example.imagegallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnChangeGrid, btnSort;
    private RecyclerView recyclerView;
    private MyAdapter adapter;

    private List<ImageObject> images = new ArrayList<>();
    private int[] colNumbers = {2, 3, 4};
    private int colNumberIndex = 0;

    private boolean asencding = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.rv_items);

        // Lấy WindowManager
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowSize.getScreenSize(windowManager);

        btnChangeGrid = findViewById(R.id.btnChangeGrid);
        btnSort = findViewById(R.id.btnSort);

        //Thêm dividers giữa các item
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
    }

    @Override
    protected void onResume() {
        super.onResume();
        File externalStorage = Environment.getExternalStorageDirectory();

// Lấy thư mục Pictures
        File picturesDirectory = new File(externalStorage, "Pictures");
        File downloadDirectory = new File(externalStorage, "Download");
        File dcimDirectory = new File(externalStorage, "DCIM");

        List<ImageObject> images = new ArrayList<>();

        ImageObject.getImage(picturesDirectory, images);
        ImageObject.getImage(downloadDirectory, images);
        ImageObject.getImage(dcimDirectory, images);
// In ra danh sách các file ảnh
        for (ImageObject imageFile : images) {
            Log.d("IMAGE", imageFile.getFilePath());
        }
        ImageObject.sortByDate(images, asencding);

        adapter = new MyAdapter(images);
        recyclerView.setAdapter(adapter);

        adapter.setColNumber(colNumbers[colNumberIndex]);

        recyclerView.setLayoutManager(new GridLayoutManager(this, adapter.getColNumber()));

        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(asencding) {
                    asencding = false;
                    ImageObject.sortByDate(images, false);
                }
                else {
                    asencding = true;
                    ImageObject.sortByDate(images, true);
                }

                adapter.notifyDataSetChanged();
            }
        });

        btnChangeGrid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colNumberIndex = (colNumberIndex + 1) % colNumbers.length;
                adapter.setColNumber(colNumbers[colNumberIndex]);
                recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, adapter.getColNumber()));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}