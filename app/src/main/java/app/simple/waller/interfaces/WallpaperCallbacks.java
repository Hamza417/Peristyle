package app.simple.waller.interfaces;

import android.widget.ImageView;

import app.simple.waller.models.Wallpaper;

public interface WallpaperCallbacks {
    void onWallpaperClicked(Wallpaper wallpaper, int position, ImageView imageView);
    void onWallpaperLongClicked(Wallpaper wallpaper, int position);
}
