package app.simple.peri.utils

import android.util.DisplayMetrics
import app.simple.peri.math.Ratio
import app.simple.peri.models.Wallpaper

object WallpaperUtils {

    /**
     * Check if the image can fit screen vertically and not leave
     * empty space on the sides
     */
    fun Wallpaper.canFitVerticallyProperly(displayMetrics: DisplayMetrics): Boolean {
        val wallpaperRatio = Ratio.calculateAspectRatio(width!!, height!!)
        val displayRatio = Ratio.calculateAspectRatio(displayMetrics.widthPixels, displayMetrics.heightPixels)
        return wallpaperRatio.height <= displayRatio.height
    }
}