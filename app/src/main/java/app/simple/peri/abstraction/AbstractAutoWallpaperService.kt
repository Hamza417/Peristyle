package app.simple.peri.abstraction

import android.app.Service
import android.app.WallpaperManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.WindowManager
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.WallpaperServiceNotification.createNotificationChannels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

abstract class AbstractAutoWallpaperService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    protected val wallpaperManager: WallpaperManager by lazy {
        WallpaperManager.getInstance(applicationContext)
    }

    protected val displayWidth: Int by lazy {
        getScreenSizeFromService().width
    }

    protected val displayHeight: Int by lazy {
        getScreenSizeFromService().height
    }

    @Throws(NoSuchElementException::class)
    protected suspend fun getRandomPreviewWallpaper(): Wallpaper? {
        return withContext(Dispatchers.IO) {
            try {
                Log.i(TAG, "Fetching random wallpaper from database")
                val dao = WallpaperDatabase.getInstance(applicationContext)?.wallpaperDao()!!
                dao.getRandomWallpaper()
            } catch (_: NoSuchElementException) {
                Log.e(TAG, "No wallpapers found in database")
                null
            }
        }
    }

    protected suspend fun validateCollection() {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val dao = WallpaperDatabase.getInstance(applicationContext)?.wallpaperDao()
                dao?.purgeNonExistingWallpapers(WallpaperDatabase.getInstance(applicationContext)!!)
            }
        }
    }

    protected suspend fun validateUsage() {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                Log.i(TAG, "Validating wallpaper usage counts")

                if (MainComposePreferences.getMaxSetCount() > 0) {
                    Log.i(TAG, "Max set count is enabled, checking wallpaper usage")
                    val dao = WallpaperDatabase.getInstance(applicationContext)?.wallpaperDao()
                    Log.i(TAG, "Checking wallpaper usage in database ${dao?.getAllWallpaperUsage()?.size ?: 0} entries")
                    dao!!.getAllWallpaperUsage().forEach {
                        Log.i(TAG, "Wallpaper ID: ${it.wallpaperId}, Usage Count: ${it.usageCount}")
                        if (it.usageCount >= MainComposePreferences.getMaxSetCount()) {
                            val wallpaper = dao.getWallpaperByID(it.wallpaperId)
                            if (wallpaper != null) {
                                Log.i(TAG, "Deleting wallpaper with ID: ${wallpaper.id} due to usage count limit")
                                if (File(wallpaper.filePath).delete()) {
                                    Log.i(TAG, "Wallpaper file deleted: ${wallpaper.filePath}")
                                    dao.delete(it)
                                }
                            } else {
                                Log.w(TAG, "Wallpaper with ID: ${it.wallpaperId} not found for deletion")
                            }
                        }
                    }
                }
            }.onFailure {
                Log.e(TAG, "Error validating wallpaper usage: ${it.message}", it)
            }
        }
    }

    fun getScreenSizeFromService(): Size {
        val windowManager = applicationContext.getSystemService(WINDOW_SERVICE) as WindowManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            val bounds = metrics.bounds
            Size(bounds.width(), bounds.height())
        } else {
            @Suppress("DEPRECATION")
            val display = windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            display.getMetrics(metrics)
            Size(metrics.widthPixels, metrics.heightPixels)
        }
    }

    companion object {
        private const val TAG = "AbstractAutoWallpaperService"
    }
}
