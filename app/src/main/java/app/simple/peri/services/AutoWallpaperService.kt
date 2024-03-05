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
import app.simple.peri.utils.BitmapUtils.cropBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class AutoWallpaperService : Service() {

    /**
     * Flag to prevent multiple next wallpaper actions from running at the same time
     * This is necessary because the widget can be clicked multiple times in a short period of time
     */
    private var isNextWallpaperActionRunning = false

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
        Log.d(TAG, "Service started")

        if (intent?.action == ACTION_NEXT_WALLPAPER) {
            Log.d(TAG, "Next wallpaper action received")
            if (!isNextWallpaperActionRunning) {
                isNextWallpaperActionRunning = true
                init()
                isNextWallpaperActionRunning = false
            }
        } else {
            init()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
    }

    private fun init() {
        SharedPreferences.init(this)
        setWallpaper()
        Log.d(TAG, "Wallpaper set")
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
                            val byteArray = stream.readBytes()
                            val bitmapOptions = BitmapFactory.Options().apply {
                                inJustDecodeBounds = true
                            }

                            BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, bitmapOptions)

                            var bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, BitmapFactory.Options().apply {
                                inPreferredConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Bitmap.Config.RGBA_1010102
                                } else {
                                    Bitmap.Config.ARGB_8888
                                }

                                Log.d(TAG, "Expected bitmap size: $displayWidth x $displayHeight")
                                inSampleSize = BitmapUtils.calculateInSampleSize(bitmapOptions, displayWidth, displayHeight)
                                inJustDecodeBounds = false
                                Log.d(TAG, "Bitmap decoded with sample size: ${this.inSampleSize}")
                            })!!

                            Log.d(TAG, "Bitmap size: ${bitmap.width}x${bitmap.height}")

                            val left = bitmap.width.div(2) - displayWidth.div(2)
                            val top = 0
                            val right = left + displayWidth
                            val bottom = bitmap.height
                            val visibleCropHint = Rect(left, top, right, bottom)

                            if (MainPreferences.getCropWallpaper()) {
                                bitmap = bitmap.cropBitmap(visibleCropHint)
                            }

                            if (MainPreferences.isDifferentWallpaperForLockScreen()) {
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                                setLockScreenWallpaper()
                            } else {
                                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                            }

                            bitmap.recycle()

                            withContext(Dispatchers.Main) {
                                stopSelf()
                            }
                        }
                    }
                }
            }.getOrElse {
                Log.e(TAG, "Error setting wallpaper: $it")
            }
        }
    }

    private fun setLockScreenWallpaper() {
        runCatching {
            val dir = DocumentFile.fromTreeUri(applicationContext, Uri.parse(MainPreferences.getStorageUri()))
            val files = dir?.listFiles()
            files?.random()?.let {
                val wallpaperManager = WallpaperManager.getInstance(applicationContext)
                it.uri.let { uri ->
                    contentResolver.openInputStream(uri)?.use { stream ->
                        val byteArray = stream.readBytes()
                        val bitmapOptions = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }

                        BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, bitmapOptions)

                        var bitmap = BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, BitmapFactory.Options().apply {
                            inPreferredConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Bitmap.Config.RGBA_1010102
                            } else {
                                Bitmap.Config.ARGB_8888
                            }

                            Log.d(TAG, "Expected bitmap size: $displayWidth x $displayHeight")
                            inSampleSize = BitmapUtils.calculateInSampleSize(bitmapOptions, displayWidth, displayHeight)
                            inJustDecodeBounds = false
                            Log.d(TAG, "Bitmap decoded with sample size: ${this.inSampleSize}")
                        })!!

                        Log.d(TAG, "Bitmap size: ${bitmap.width}x${bitmap.height}")

                        val left = bitmap.width.div(2) - displayWidth.div(2)
                        val top = 0
                        val right = left + displayWidth
                        val bottom = bitmap.height
                        val visibleCropHint = Rect(left, top, right, bottom)

                        if (MainPreferences.getCropWallpaper()) {
                            bitmap = bitmap.cropBitmap(visibleCropHint)
                        }

                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)

                        bitmap.recycle()
                    }
                }
            }
        }.getOrElse {
            Log.e(TAG, "Error setting wallpaper: $it")
            Log.d(TAG, "Service stopped, wait for next alarm to start again")
        }
    }

    companion object {
        const val ACTION_NEXT_WALLPAPER: String = "app.simple.peri.services.action.NEXT_WALLPAPER"
        private const val TAG = "AutoWallpaperService"
    }
}
