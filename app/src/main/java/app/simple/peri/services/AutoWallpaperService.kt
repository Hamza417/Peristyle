package app.simple.peri.services

import android.app.Service
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.preferences.SharedPreferences
import app.simple.peri.utils.BitmapUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                val dir = DocumentFile.fromTreeUri(applicationContext, Uri.parse(MainPreferences.getStorageUri()))
                val files = dir?.listFiles()
                files?.random()?.let {
                    val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                    it.uri.let { uri ->
                        contentResolver.openInputStream(uri)?.use { stream ->
                            val bitmap = BitmapFactory.decodeStream(stream, null, BitmapFactory.Options().apply {
                                inPreferredConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Bitmap.Config.RGBA_1010102
                                } else {
                                    Bitmap.Config.ARGB_8888
                                }

                                inSampleSize = BitmapUtils.calculateInSampleSize(this, displayWidth, displayHeight)
                                inJustDecodeBounds = false
                            })

                            val bitmapWidth = bitmap?.width
                            val bitmapHeight = bitmap?.height

                            val left = (bitmapWidth!! - displayWidth) / 2
                            val top = 0
                            val right = left + displayWidth
                            val bottom = bitmapHeight!!

                            val visibleCropHint = Rect(left, top, right, bottom)

                            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
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
    }
}
