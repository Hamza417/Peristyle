package app.simple.peri.receivers

import android.app.NotificationManager
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import app.simple.peri.R
import app.simple.peri.services.AutoWallpaperService
import app.simple.peri.utils.FileUtils.toUri

class WallpaperActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AutoWallpaperService.ACTION_DELETE_WALLPAPER -> {
                val isHomeScreen = intent.getBooleanExtra("isHomeScreen", true)
                val wallpaperManager = WallpaperManager.getInstance(context)

                if (isHomeScreen) {
                    wallpaperManager.clear(WallpaperManager.FLAG_SYSTEM)
                } else {
                    wallpaperManager.clear(WallpaperManager.FLAG_LOCK)
                }

                val uri = intent.getStringExtra(AutoWallpaperService.EXTRA_WALLPAPER_URI)!!.toUri()
                val documentFile = DocumentFile.fromSingleUri(context, uri)
                documentFile?.delete()
                Log.d(TAG, "Wallpaper deleted")

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(intent.getIntExtra(AutoWallpaperService.EXTRA_NOTIFICATION_ID, 0))
            }
            AutoWallpaperService.ACTION_SEND_WALLPAPER -> {
                val uri = intent.getStringExtra(AutoWallpaperService.EXTRA_WALLPAPER_URI)!!.toUri()
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                }

                context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.send)))

                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(intent.getIntExtra(AutoWallpaperService.EXTRA_NOTIFICATION_ID, 0))
            }
        }
    }

    companion object {
        private const val TAG = "WallpaperActionReceiver"
    }
}
