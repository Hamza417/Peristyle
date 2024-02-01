package app.simple.peri.services

import android.app.Service
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.preferences.SharedPreferences

class AutoWallpaperService : Service() {

    private val displayWidth: Int by lazy {
        resources.displayMetrics.widthPixels
    }

    private val displayHeight: Int by lazy {
        resources.displayMetrics.heightPixels
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        SharedPreferences.init(this)
        setWallpaper()
        Log.d("AutoWallpaperService", "Wallpaper set")
    }

    private fun setWallpaper() {
        val dir = DocumentFile.fromTreeUri(this, Uri.parse(MainPreferences.getStorageUri()))
        val files = dir?.listFiles()
        files?.random()?.let {
            val wallpaperManager = WallpaperManager.getInstance(this)
            it.uri.let { uri ->
                contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    val bitmapCenterX = bitmap.width / 2
                    val halfDisplayWidth = displayWidth / 2

                    val visibleCropHint = Rect(bitmapCenterX - halfDisplayWidth,
                                               0,
                                               bitmapCenterX + halfDisplayWidth,
                                               bitmap.height)

                    wallpaperManager.setBitmap(bitmap, visibleCropHint, true, WallpaperManager.FLAG_SYSTEM)
                    wallpaperManager.setBitmap(bitmap, visibleCropHint, true, WallpaperManager.FLAG_LOCK)
                    stopSelf()
                }
            }
        }
    }
}