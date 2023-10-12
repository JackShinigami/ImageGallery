package com.example.imagegallery;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.BitmapFactory;

import java.io.IOException;

public class Wallpapersetter {
    public static void setWallpaper(Context context, String Path) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        try {
            wallpaperManager.setBitmap(BitmapFactory.decodeFile(Path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setLockScreen(Context context, String Path)
    {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        try {
            wallpaperManager.setBitmap(BitmapFactory.decodeFile(Path), null, true, WallpaperManager.FLAG_LOCK);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
