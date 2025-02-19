package app.simple.peri.utils

import android.app.ActivityManager
import android.content.Context
import app.simple.peri.services.LiveAutoWallpaperService

object ServiceUtils {
    fun isWallpaperServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION") val services = manager.getRunningServices(Integer.MAX_VALUE)
        return services.any { it.service.className == LiveAutoWallpaperService::class.java.name }
    }
}