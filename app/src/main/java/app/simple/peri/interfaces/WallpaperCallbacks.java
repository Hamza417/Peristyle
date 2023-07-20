package app.simple.peri.interfaces;

import android.view.View;

import com.google.android.material.checkbox.MaterialCheckBox;

import androidx.constraintlayout.widget.ConstraintLayout;
import app.simple.peri.models.Wallpaper;

public interface WallpaperCallbacks {
    void onWallpaperClicked(Wallpaper wallpaper, int position, ConstraintLayout constraintLayout);
    void onWallpaperLongClicked(Wallpaper wallpaper, int position, View view, MaterialCheckBox checkBox);
}
