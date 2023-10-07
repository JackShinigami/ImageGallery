package com.example.imagegallery;

import android.util.DisplayMetrics;
import android.view.WindowManager;

public class WindowSize {
    private static int width;
    private static int height;

    public static int getWidth() {
        return width;
    }

    public static int getHeight() {
        return height;
    }

    public static void setSize(int width, int height) {
        WindowSize.width = width;
        WindowSize.height = height;
    }

    public static void getScreenSize(WindowManager windowManager) {
// Lấy DisplayMetrics
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

// Lấy kích thước của cửa sổ
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
    }
}
