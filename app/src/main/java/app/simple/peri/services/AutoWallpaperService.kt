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
import android.widget.Toast
import androidx.core.net.toUri
import app.simple.peri.R
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.preferences.SharedPreferences
import app.simple.peri.utils.BitmapUtils
import app.simple.peri.utils.BitmapUtils.cropBitmap
import app.simple.peri.utils.ScreenUtils
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
        ScreenUtils.getScreenSize(applicationContext).width
    }

    private val displayHeight: Int by lazy {
        ScreenUtils.getScreenSize(applicationContext).height
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
                runCatching {
                    Toast.makeText(applicationContext, R.string.changing_wallpaper, Toast.LENGTH_SHORT).show()
                }
                init()
                isNextWallpaperActionRunning = false
            } else {
                Log.d(TAG, "Next wallpaper action already running, ignoring")
                Toast.makeText(applicationContext, R.string.next_wallpaper_already_running, Toast.LENGTH_SHORT).show()
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

        if (MainPreferences.isWallpaperWhenSleeping()) {
            setWallpaper()
            Log.d(TAG, "Wallpaper set when the user is sleeping")
        } else {
            if (ScreenUtils.isDeviceSleeping(applicationContext)) {
                Log.d(TAG, "Device is sleeping, waiting for next alarm to set wallpaper")
            } else {
                setWallpaper()
            }
        }
    }

    private fun setWallpaper() {
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                val files = getWallpapersFromDatabase()
                if (MainPreferences.isTweakOptionSelected(MainPreferences.LINEAR_AUTO_WALLPAPER)) {
                    if (MainPreferences.getLastWallpaperPosition() >= (files?.size?.minus(1) ?: 0)) {
                        files?.get(0)?.uri?.toUri()?.let { uri ->
                            setWallpaperFromUri(uri, files)
                            MainPreferences.setLastWallpaperPosition(0)
                        }
                    } else {
                        files?.get(MainPreferences.getLastWallpaperPosition().plus(1))?.uri?.toUri()?.let { uri ->
                            setWallpaperFromUri(uri, files)
                            MainPreferences.setLastWallpaperPosition(MainPreferences.getLastWallpaperPosition() + 1)
                        }
                    }
                } else {
                    files?.random()?.uri?.toUri()?.let { uri ->
                        setWallpaperFromUri(uri, files)
                        MainPreferences.setLastWallpaperPosition(files.indexOf(files.find { it.uri == uri.toString() }))
                    }
                }
            }.getOrElse {
                Log.e(TAG, "Error setting wallpaper: $it")
            }
        }
    }

    private suspend fun getWallpapersFromDatabase(): List<Wallpaper>? {
        return withContext(Dispatchers.IO) {
            val dao = WallpaperDatabase.getInstance(applicationContext)?.wallpaperDao()
            dao?.sanitizeEntries()
            dao?.getWallpapers()
        }
    }

    private suspend fun setWallpaperFromUri(uri: Uri, files: List<Wallpaper>) {
        withContext(Dispatchers.IO) {
            val wallpaperManager = WallpaperManager.getInstance(applicationContext)
            contentResolver.openInputStream(uri)?.use { stream ->
                val byteArray = stream.readBytes()
                var bitmap = decodeBitmap(byteArray)

                // Correct orientation of the bitmap if faulty due to EXIF data
                bitmap = BitmapUtils.correctOrientation(bitmap, ByteArrayInputStream(byteArray))

                val visibleCropHint = calculateVisibleCropHint(bitmap)

                if (MainPreferences.getCropWallpaper()) {
                    bitmap = bitmap.cropBitmap(visibleCropHint)
                }

                setWallpaperBasedOnPreference(bitmap, wallpaperManager, files)

                bitmap.recycle()

                withContext(Dispatchers.Main) {
                    stopSelf()
                }
            }
        }
    }

    private fun decodeBitmap(byteArray: ByteArray): Bitmap {
        val bitmapOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, bitmapOptions)

        return BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, BitmapFactory.Options().apply {
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
    }

    private fun calculateVisibleCropHint(bitmap: Bitmap): Rect {
        val left = bitmap.width.div(2) - displayWidth.div(2)
        val top = 0
        val right = left + displayWidth
        val bottom = bitmap.height
        return Rect(left, top, right, bottom)
    }

    private fun setWallpaperBasedOnPreference(bitmap: Bitmap, wallpaperManager: WallpaperManager, files: List<Wallpaper>) {
        when (MainPreferences.getWallpaperSetFor()) {
            MainPreferences.BOTH -> {
                if (MainPreferences.isDifferentWallpaperForLockScreen()) {
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    setLockScreenWallpaper(files)
                } else {
                    /**
                     * Setting them separately to avoid the wallpaper not setting
                     * in some devices for lock screen.
                     */
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                }
            }

            MainPreferences.HOME -> {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
            }

            MainPreferences.LOCK -> {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
            }
        }
    }

    private fun setLockScreenWallpaper(files: List<Wallpaper>?) {
        runCatching {
            files?.random()?.uri?.toUri()?.let { uri ->
                setLockScreenWallpaperFromUri(uri)
            }
        }.getOrElse {
            Log.e(TAG, "Error setting wallpaper: $it")
            Log.d(TAG, "Service stopped, wait for next alarm to start again")
        }
    }

    private fun setLockScreenWallpaperFromUri(uri: Uri) {
        val wallpaperManager = WallpaperManager.getInstance(applicationContext)
        contentResolver.openInputStream(uri)?.use { stream ->
            val byteArray = stream.readBytes()
            var bitmap = decodeBitmap(byteArray)

            // Correct orientation of the bitmap if faulty due to EXIF data
            bitmap = BitmapUtils.correctOrientation(bitmap, ByteArrayInputStream(byteArray))

            val visibleCropHint = calculateVisibleCropHint(bitmap)

            if (MainPreferences.getCropWallpaper()) {
                bitmap = bitmap.cropBitmap(visibleCropHint)
            }

            wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)

            bitmap.recycle()
        }
    }

    companion object {
        const val ACTION_NEXT_WALLPAPER: String = "app.simple.peri.services.action.NEXT_WALLPAPER"
        private const val TAG = "AutoWallpaperService"
    }
}
