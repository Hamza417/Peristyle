package app.simple.peri.abstraction

import android.app.ActivityManager
import android.content.Context
import android.os.Parcelable
import android.service.wallpaper.WallpaperService
import app.simple.peri.models.Wallpaper
import app.simple.peri.services.LiveAutoWallpaperService

abstract class AbstractAutoLiveWallpaperService : AbstractComposeAutoWallpaperService() {

    protected fun postLiveWallpaper(onComplete: () -> Unit) {
        setComposeWallpaper {
            onComplete()
        }
    }

    override fun setSameWallpaper(wallpaper: Wallpaper) {
        val intent = LiveAutoWallpaperService.getIntent(applicationContext, LiveAutoWallpaperService.SAME_WALLPAPER)
        intent.putExtra(LiveAutoWallpaperService.EXTRA_WALLPAPER, wallpaper as Parcelable)
        applicationContext.startService(intent)
    }

    override fun setHomeScreenWallpaper(wallpaper: Wallpaper) {
        val intent = LiveAutoWallpaperService.getIntent(applicationContext, LiveAutoWallpaperService.HOME_SCREEN_WALLPAPER)
        intent.putExtra(LiveAutoWallpaperService.EXTRA_WALLPAPER, wallpaper as Parcelable)
        applicationContext.startService(intent)
    }

    override fun setLockScreenWallpaper(wallpaper: Wallpaper) {
        val intent = LiveAutoWallpaperService.getIntent(applicationContext, LiveAutoWallpaperService.LOCK_SCREEN_WALLPAPER)
        intent.putExtra(LiveAutoWallpaperService.EXTRA_WALLPAPER, wallpaper as Parcelable)
        applicationContext.startService(intent)
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

    @Suppress("DEPRECATION")
    private fun isWallpaperServiceRunning(context: Context, serviceClass: Class<out WallpaperService>): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        /**
         * "As of Build. VERSION_CODES. O, this method is no longer available
         * to third party applications. For backwards compatibility, it will still
         * return the caller's own services."
         *
         * If we are querying my app's services, this method will work just fine.
         */
        val services = activityManager.getRunningServices(Int.MAX_VALUE)
        for (service in services) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }

        return false
    }
}
