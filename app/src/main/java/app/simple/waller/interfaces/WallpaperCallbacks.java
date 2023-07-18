package app.simple.waller.interfaces;

import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import app.simple.waller.models.Wallpaper;

public interface WallpaperCallbacks {
    void onWallpaperClicked(Wallpaper wallpaper, int position, ConstraintLayout constraintLayout);
    void onWallpaperLongClicked(Wallpaper wallpaper, int position);
}
