import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import java.io.IOException;

public class WallpaperSetter {
    static public  void setWallpaper(Context context, String path) {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        try {
            wallpaperManager.setBitmap(bitmap);
            Toast.makeText(context, "Wallpaper set", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
