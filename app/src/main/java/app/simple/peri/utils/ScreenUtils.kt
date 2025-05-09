package app.simple.peri.utils

import android.app.KeyguardManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.PowerManager
import android.util.DisplayMetrics
import android.util.Size
import android.view.WindowManager
import android.view.WindowMetrics
import app.simple.peri.R
import app.simple.peri.models.Wallpaper

object ScreenUtils {

    /**
     * Returns screen size in pixels.
     */
    @Suppress("DEPRECATION")
    fun getScreenSize(context: Context): Size {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics: WindowMetrics = context.getSystemService(WindowManager::class.java).currentWindowMetrics
            Size(metrics.bounds.width(), metrics.bounds.height())
        } else {
            val display = context.getSystemService(WindowManager::class.java).defaultDisplay
            val metrics = if (display != null) {
                DisplayMetrics().also { display.getRealMetrics(it) }
            } else {
                Resources.getSystem().displayMetrics
            }

            Size(metrics.widthPixels, metrics.heightPixels)
        }
    }

    fun Wallpaper.isWallpaperFittingScreen(context: Context): Boolean {
        val screenSize = getScreenSize(context)
        return width == screenSize.width && height == screenSize.height
    }

    fun getScreenDensity(context: Context): String {
        return when (context.resources.displayMetrics.densityDpi) {
            DisplayMetrics.DENSITY_LOW -> {
                "ldpi"
            }

            DisplayMetrics.DENSITY_140 -> {
                "ldpi - mdpi"
            }

            DisplayMetrics.DENSITY_MEDIUM -> {
                "mdpi"
            }

            DisplayMetrics.DENSITY_180,
            DisplayMetrics.DENSITY_200,
            DisplayMetrics.DENSITY_220 -> {
                "mdpi - hdpi"
            }

            DisplayMetrics.DENSITY_HIGH -> {
                "hdpi"
            }

            DisplayMetrics.DENSITY_260,
            DisplayMetrics.DENSITY_280,
            DisplayMetrics.DENSITY_300 -> {
                "hdpi - xhdpi"
            }

            DisplayMetrics.DENSITY_XHIGH -> {
                "xhdpi"
            }

            DisplayMetrics.DENSITY_340,
            DisplayMetrics.DENSITY_360,
            DisplayMetrics.DENSITY_400,
            DisplayMetrics.DENSITY_420,
            DisplayMetrics.DENSITY_440 -> {
                "xhdpi - xxhdpi"
            }

            DisplayMetrics.DENSITY_XXHIGH -> {
                "xxhdpi"
            }

            DisplayMetrics.DENSITY_560,
            DisplayMetrics.DENSITY_600 -> {
                "xxhdpi - xxxhdpi"
            }

            DisplayMetrics.DENSITY_XXXHIGH -> {
                "xxxhdpi"
            }

            DisplayMetrics.DENSITY_TV -> {
                "tvdpi"
            }

            else -> context.getString(R.string.unknown)
        }
    }

    fun getRefreshRate(context: Context): Float {
        @Suppress("deprecation")
        return context.getSystemService(WindowManager::class.java).defaultDisplay.refreshRate
    }

    fun getOrientation(context: Context): String {
        return when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> context.resources.getString(R.string.landscape)
            Configuration.ORIENTATION_PORTRAIT -> context.resources.getString(R.string.portrait)
            else -> context.resources.getString(R.string.unknown)
        }
    }

    fun Context.isLandscape(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    fun Context.isPortrait(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    }

    fun isDeviceLocked(context: Context): Boolean {
        return (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked
    }

    fun isDeviceSleeping(context: Context): Boolean {
        return (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
    }
}
