package app.simple.peri.abstraction

import android.app.ActivityManager
import android.content.Context
import android.os.Parcelable
import android.util.Log
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.services.LiveAutoWallpaperService
import app.simple.peri.utils.WallpaperServiceNotification.showErrorNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class AbstractAutoLiveWallpaperService : AbstractComposeAutoWallpaperService() {

    protected fun postLiveWallpaper(onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            validateCollection()

            runCatching {
                when {
                    MainPreferences.isSettingForHomeScreen() -> {
                        Log.d(TAG, "Setting wallpaper for home screen")
                        getHomeScreenWallpaper()?.let { wallpaper ->
                            setHomeScreenWallpaper(wallpaper)
                        }
                    }
                    else -> {
                        Log.d(TAG, "Setting wallpaper for both home and lock screen")
                        setSameWallpaper(getRandomWallpaperFromDatabase()!!)
                    }
                }

                withContext(Dispatchers.Main) {
                    onComplete()
                    stopSelf()
                }
            }.getOrElse {
                it.printStackTrace()
                Log.e(TAG, "Error setting wallpaper: $it")

                withContext(Dispatchers.Main) {
                    showErrorNotification(it.stackTraceToString())
                    onComplete()
                    stopSelf()
                }
            }
        }
    }

    override fun setSameWallpaper(wallpaper: Wallpaper) {
        if (isWallpaperServiceRunning()) {
            val intent = LiveAutoWallpaperService.getIntent(applicationContext, LiveAutoWallpaperService.NEXT_WALLPAPER)
            intent.putExtra(LiveAutoWallpaperService.EXTRA_WALLPAPER, wallpaper as Parcelable)
            applicationContext.startService(intent)
            Log.i(TAG, "wallpaper data sent, live wallpaper should handle this from here")
        } else {
            super.setSameWallpaper(wallpaper)
        }
    }

    override fun setHomeScreenWallpaper(wallpaper: Wallpaper) {
        if (isWallpaperServiceRunning()) {
            val intent = LiveAutoWallpaperService.getIntent(applicationContext, LiveAutoWallpaperService.NEXT_WALLPAPER)
            intent.putExtra(LiveAutoWallpaperService.EXTRA_WALLPAPER, wallpaper as Parcelable)
            applicationContext.startService(intent)
            Log.i(TAG, "wallpaper data sent, live wallpaper should handle this from here")
        } else {
            super.setHomeScreenWallpaper(wallpaper)
        }
    }

    /**
     * As much as it needs to Live Wallpapers don't have the ability to work exclusively on the lock screen.
     * So, I am skipping this method.
     */
    override fun setLockScreenWallpaper(wallpaper: Wallpaper) {
        if (isWallpaperServiceRunning()) {
            //        val intent = LiveAutoWallpaperService.getIntent(applicationContext, LiveAutoWallpaperService.LOCK_SCREEN_WALLPAPER)
            //        intent.putExtra(LiveAutoWallpaperService.EXTRA_WALLPAPER, wallpaper as Parcelable)
            //        applicationContext.startService(intent)
        } else {
            super.setLockScreenWallpaper(wallpaper)
        }
    }

    protected fun setPreviewWallpaper() {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val wallpaper = getRandomWallpaperFromDatabase() ?: throw NoSuchElementException("No wallpapers found in database")
                val intent = LiveAutoWallpaperService.getIntent(applicationContext, LiveAutoWallpaperService.PREVIEW_WALLPAPER)
                intent.putExtra(LiveAutoWallpaperService.EXTRA_WALLPAPER, wallpaper as Parcelable)
                applicationContext.startService(intent)
            } catch (e: NoSuchElementException) {
                Log.e(TAG, e.stackTraceToString())
            }
        }
    }

    @Suppress("DEPRECATION")
    protected fun isWallpaperServiceRunning(): Boolean {
        val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        /**
         * "As of Build. VERSION_CODES. O, this method is no longer available
         * to third party applications. For backwards compatibility, it will still
         * return the caller's own services."
         *
         * If we are querying my app's services, this method will work just fine.
         */
        val services = activityManager.getRunningServices(Int.MAX_VALUE)
        for (service in services) {
            if (LiveAutoWallpaperService::class.java.name == service.service.className) {
                return true
            }
        }

        return false
    }
}
