package app.simple.peri.receivers

import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.services.AutoWallpaperService
import app.simple.peri.utils.FileUtils.toFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class WallpaperActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AutoWallpaperService.ACTION_DELETE_WALLPAPER_HOME -> {
                val wallpaperManager = WallpaperManager.getInstance(context)
                wallpaperManager.clear(WallpaperManager.FLAG_SYSTEM)

                val file = intent.getStringExtra(AutoWallpaperService.EXTRA_WALLPAPER_PATH)!!.toFile()
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "Wallpaper deleted")
                } else {
                    Log.e(TAG, "Wallpaper not found")
                }

                val notificationID = intent.getIntExtra(AutoWallpaperService.EXTRA_NOTIFICATION_ID, 0)
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                Log.i(TAG, "Notification ID: $notificationID")
                notificationManager.cancel(AutoWallpaperService.HOME_NOTIFICATION_ID)
            }
            AutoWallpaperService.ACTION_DELETE_WALLPAPER_LOCK -> {
                val wallpaperManager = WallpaperManager.getInstance(context)
                wallpaperManager.clear(WallpaperManager.FLAG_LOCK)

                val file = intent.getStringExtra(AutoWallpaperService.EXTRA_WALLPAPER_PATH)!!.toFile()
                if (file.exists()) {
                    file.delete()
                    Log.d(TAG, "Wallpaper deleted")
                } else {
                    Log.e(TAG, "Wallpaper not found")
                }

                val notificationID = intent.getIntExtra(AutoWallpaperService.EXTRA_NOTIFICATION_ID, 0)
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                Log.i(TAG, "Notification ID: $notificationID")
                notificationManager.cancel(AutoWallpaperService.LOCK_NOTIFICATION_ID)
            }
        }
    }

    private fun updateDatabase(file: File, context: Context) {
        val applicationContext = context.applicationContext // Avoid memory leak
        CoroutineScope(Dispatchers.IO).launch {
            val wallpaperDatabase = WallpaperDatabase.getInstance(applicationContext)
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()!!
            wallpaperDao.deleteByFile(file.absolutePath.toString())
            wallpaperDatabase.close()
        }
    }

    companion object {
        private const val TAG = "WallpaperActionReceiver"
    }
}
