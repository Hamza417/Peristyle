package app.simple.peri.abstraction

import android.app.ActivityManager
import android.content.Context
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

    }

    override fun setHomeScreenWallpaper(wallpaper: Wallpaper) {

    }

    override fun setLockScreenWallpaper(wallpaper: Wallpaper) {

    }

    @Suppress("DEPRECATION")
    protected fun isWallpaperServiceRunning(): Boolean {
        val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
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
        val services = activityManager.getRunningServices(Int.MAX_VALUE)
        for (service in services) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }

        return false
    }
}
