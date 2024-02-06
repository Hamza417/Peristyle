package app.simple.peri.services

import android.app.Service
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AutoWallpaperService", "Service started")
        init()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("AutoWallpaperService", "Service destroyed")
    }

    private fun init() {
        SharedPreferences.init(this)
        setWallpaper()
        Log.d("AutoWallpaperService", "Wallpaper set")
    }

    private fun setWallpaper() {
        runCatching {
            val dir = DocumentFile.fromTreeUri(this, Uri.parse(MainPreferences.getStorageUri()))
            val files = dir?.listFiles()
            files?.random()?.let {
                val wallpaperManager = WallpaperManager.getInstance(this)
                it.uri.let { uri ->
                    contentResolver.openInputStream(uri)?.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream).cropBitmapFromCenter()
                        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, displayWidth, displayHeight, true)
                        wallpaperManager.setBitmap(scaledBitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                        bitmap.recycle()
                    }
                }
            }
        }.getOrElse {
            Log.e("AutoWallpaperService", "Error setting wallpaper: $it")
            Log.e("AutoWallpaperService", "Trying again...")
            setWallpaper()
        }
    }

    private fun Bitmap.cropBitmapFromCenter(): Bitmap {
        val aspectRatio: Float = displayWidth.toFloat() / displayHeight.toFloat()
        var newHeight: Int = (width / aspectRatio).toInt()

        // Ensure newHeight does not exceed the original height
        if (newHeight > height) {
            newHeight = height
        }

        val yOffset: Int = (height - newHeight) / 2

        return Bitmap.createBitmap(this, 0, yOffset, width, newHeight)
    }
}
