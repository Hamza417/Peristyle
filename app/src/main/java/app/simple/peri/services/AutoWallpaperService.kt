package app.simple.peri.services

import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import app.simple.peri.R
import app.simple.peri.abstraction.AbstractAutoLiveWallpaperService
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.SharedPreferences
import java.io.File

class AutoWallpaperService : AbstractAutoLiveWallpaperService() {

    /**
     * Flag to prevent multiple next wallpaper actions from running at the same time
     * This is necessary because the widget can be clicked multiple times in a short period of time
     */
    private var isNextWallpaperActionRunning = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        when (intent?.action) {
            ACTION_NEXT_WALLPAPER -> {
                Log.d(TAG, "Next wallpaper action received")
                if (!isNextWallpaperActionRunning) {
                    isNextWallpaperActionRunning = true
                    runCatching {
                        Toast.makeText(applicationContext, R.string.changing_wallpaper, Toast.LENGTH_SHORT)
                            .show()
                    }
                    init()
                } else {
                    Log.d(TAG, "Next wallpaper action already running, ignoring")
                    Toast.makeText(applicationContext, R.string.next_wallpaper_already_running, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            ACTION_DELETE_WALLPAPER -> {
                val wallpaperPath = intent.getStringExtra(EXTRA_WALLPAPER_PATH)
                if (wallpaperPath != null) {
                    val file = File(wallpaperPath)
                    val allowedPaths = MainComposePreferences.getAllowedPaths()

                    if (file.exists() && allowedPaths.any { file.canonicalPath.startsWith(it) }) {
                        Log.i(TAG, "Deleting wallpaper: ${file.absolutePath}")
                        file.delete()
                        Log.i(TAG, "File deleted")
                    } else {
                        Log.e(TAG, "File does not exist or is outside the allowed paths, skipping")
                    }
                } else {
                    Log.e(TAG, "No wallpaper path provided, skipping")
                }
            }
            RANDOM_PREVIEW_WALLPAPER -> {
                Log.d(TAG, "Random preview wallpaper action received")
                setPreviewWallpaper()
            }
            else -> {
                init()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "auto wallpaper service destroyed")
    }

    private fun init() {
        SharedPreferences.init(applicationContext)

        if (isWallpaperServiceRunning()) {
            Log.i(TAG, "Wallpaper service is running, setting next wallpaper through live wallpaper service")
            postLiveWallpaper {
                isNextWallpaperActionRunning = false
            }
        } else {
            Log.d(TAG, "Wallpaper service is not running, setting next wallpaper through compose service")
            setComposeWallpaper {
                isNextWallpaperActionRunning = false
            }
        }
    }

    companion object {
        const val ACTION_NEXT_WALLPAPER: String = "app.simple.peri.services.action.NEXT_WALLPAPER"
        const val RANDOM_PREVIEW_WALLPAPER = "app.simple.peri.services.action.RANDOM_PREVIEW_WALLPAPER"
    }
}
