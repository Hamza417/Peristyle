package app.simple.peri.receivers

import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.services.AutoWallpaperService
import app.simple.peri.utils.FileUtils.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WallpaperActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AutoWallpaperService.ACTION_DELETE_WALLPAPER_HOME -> {
                val wallpaperManager = WallpaperManager.getInstance(context)
                wallpaperManager.clear(WallpaperManager.FLAG_SYSTEM)

                val uri = intent.getStringExtra(AutoWallpaperService.EXTRA_WALLPAPER_URI)!!.toUri()
                val documentFile = DocumentFile.fromSingleUri(context, uri)
                if (documentFile?.exists() == true) {
                    documentFile.delete()
                    Log.d(TAG, "Wallpaper deleted")
                } else {
                    Log.e(TAG, "Wallpaper not found")
                }

                val notificationID = intent.getIntExtra(AutoWallpaperService.EXTRA_NOTIFICATION_ID, 0)
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                Log.i(TAG, "Notification ID: $notificationID")
                notificationManager.cancel(AutoWallpaperService.HOME_NOTIFICATION_ID)
                updateDatabase(uri, context)
            }
            AutoWallpaperService.ACTION_DELETE_WALLPAPER_LOCK -> {
                val wallpaperManager = WallpaperManager.getInstance(context)
                wallpaperManager.clear(WallpaperManager.FLAG_LOCK)

                val uri = intent.getStringExtra(AutoWallpaperService.EXTRA_WALLPAPER_URI)!!.toUri()
                val documentFile = DocumentFile.fromSingleUri(context, uri)
                if (documentFile?.exists() == true) {
                    documentFile.delete()
                    Log.d(TAG, "Wallpaper deleted")
                } else {
                    Log.e(TAG, "Wallpaper not found")
                }

                val notificationID = intent.getIntExtra(AutoWallpaperService.EXTRA_NOTIFICATION_ID, 0)
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                Log.i(TAG, "Notification ID: $notificationID")
                notificationManager.cancel(AutoWallpaperService.LOCK_NOTIFICATION_ID)
                updateDatabase(uri, context)
            }
        }
    }

    private fun updateDatabase(uri: Uri, context: Context) {
        val applicationContext = context.applicationContext // Avoid memory leak
        CoroutineScope(Dispatchers.IO).launch {
            val wallpaperDatabase = WallpaperDatabase.getInstance(applicationContext)
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()!!
            wallpaperDao.deleteByUri(uri.toString())
            wallpaperDatabase.close()
        }
    }

    companion object {
        private const val TAG = "WallpaperActionReceiver"
    }
}
