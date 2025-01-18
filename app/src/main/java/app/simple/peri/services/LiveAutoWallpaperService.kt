package app.simple.peri.services

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import app.simple.peri.abstraction.AbstractComposeAutoWallpaperService
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.BitmapUtils
import app.simple.peri.utils.BitmapUtils.cropBitmap
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.utils.ParcelUtils.parcelable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream

class LiveAutoWallpaperService : WallpaperService() {

    private var engine: LiveAutoWallpaperEngine? = null
    private var handler: Handler? = null

    override fun onCreate() {
        super.onCreate()
        val intent = Intent(applicationContext, AutoWallpaperService::class.java)
        intent.action = AutoWallpaperService.ACTION_NEXT_WALLPAPER
        applicationContext.startService(intent)

        handler = Handler(Looper.getMainLooper()) { msg ->
            when (msg.what) {
                MSG_SET_WALLPAPER -> {
                    val filePath = msg.obj as String
                    engine!!.setWallpaper(filePath)
                }
            }
            true
        }
    }

    override fun onCreateEngine(): Engine {
        engine = LiveAutoWallpaperEngine()
        return engine!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            SAME_WALLPAPER -> {
                val wallpaper: Wallpaper = intent.parcelable<Wallpaper>(EXTRA_WALLPAPER)!!
                Log.i(TAG, "Setting wallpaper: ${wallpaper.filePath}")
                val msg = handler?.obtainMessage(MSG_SET_WALLPAPER, wallpaper.filePath)!!
                handler?.sendMessage(msg)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private inner class LiveAutoWallpaperEngine : Engine() {
        private var bitmap: Bitmap? = null

        fun setWallpaper(filePath: String) {
            CoroutineScope(Dispatchers.Main).launch {
                val canvas = surfaceHolder.lockCanvas()

                withContext(Dispatchers.Default) {
                    getBitmapFromFile(filePath, canvas.width, canvas.height) { bmp ->
                        bitmap = bmp
                    }
                }

                surfaceHolder.unlockCanvasAndPost(canvas)
                onSurfaceRedrawNeeded(surfaceHolder)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                // Start the animation
            } else {
                // Stop the animation
            }
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)

        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)

        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder?) {
            super.onSurfaceRedrawNeeded(holder)
            holder?.let {
                Log.i(TAG, "Redrawing wallpaper")
                val canvas: Canvas? = it.lockCanvas()
                bitmap?.let { bmp ->
                    // Draw the bitmap on the canvas
                    canvas!!.drawBitmap(bmp, 0f, 0f, null)
                }
                it.unlockCanvasAndPost(canvas)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            bitmap?.recycle()
            bitmap = null
        }
    }

    private fun getBitmapFromFile(path: String, width: Int, height: Int, onBitmap: (Bitmap) -> Unit) {
        Log.i(TAG, "Getting bitmap from file: $path with width: $width and height: $height")
        path.toFile().inputStream().use { stream ->
            val byteArray = stream.readBytes()
            var bitmap = decodeBitmap(byteArray, width, height)

            // Correct orientation of the bitmap if faulty due to EXIF data
            bitmap = BitmapUtils.correctOrientation(bitmap, ByteArrayInputStream(byteArray))

            val visibleCropHint = calculateVisibleCropHint(bitmap, width, height)
            bitmap = bitmap.cropBitmap(visibleCropHint)
            onBitmap(bitmap)
        }
    }

    protected fun calculateVisibleCropHint(bitmap: Bitmap, displayWidth: Int, displayHeight: Int): Rect {
        // Calculate the aspect ratio of the display
        val aspectRatio = displayWidth.toFloat() / displayHeight.toFloat()
        // Calculate the aspect ratio of the bitmap
        val bitmapAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        // Determine the crop width and height based on the aspect ratios
        val (cropWidth, cropHeight) = if (bitmapAspectRatio > aspectRatio) {
            // If the bitmap is wider than the desired aspect ratio
            val width = (bitmap.height * aspectRatio).toInt()
            width to bitmap.height
        } else {
            // If the bitmap is taller than the desired aspect ratio
            val height = (bitmap.width / aspectRatio).toInt()
            bitmap.width to height
        }

        // Calculate the left, top, right, and bottom coordinates for the crop rectangle
        val left = (bitmap.width - cropWidth) / 2
        val top = (bitmap.height - cropHeight) / 2
        val right = left + cropWidth
        val bottom = top + cropHeight

        // Return the calculated crop rectangle
        return Rect(left, top, right, bottom)
    }

    protected fun decodeBitmap(byteArray: ByteArray, displayWidth: Int, displayHeight: Int): Bitmap {
        val bitmapOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, bitmapOptions)

        return BitmapFactory.decodeStream(
                ByteArrayInputStream(byteArray), null, BitmapFactory.Options().apply {
            inPreferredConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Bitmap.Config.RGBA_1010102
            } else {
                Bitmap.Config.ARGB_8888
            }

            inMutable = true
            inSampleSize =
                BitmapUtils.calculateInSampleSize(bitmapOptions, displayWidth, displayHeight)
            inJustDecodeBounds = false
            Log.d(AbstractComposeAutoWallpaperService.TAG, "Bitmap decoded with sample size: ${this.inSampleSize}")
        })!!
    }

    companion object {
        private const val TAG = "LiveAutoWallpaperService"
        const val SAME_WALLPAPER = "action.SAME_WALLPAPER"
        const val HOME_SCREEN_WALLPAPER = "action.HOME_SCREEN_WALLPAPER"
        const val LOCK_SCREEN_WALLPAPER = "action.LOCK_SCREEN_WALLPAPER"
        const val EXTRA_WALLPAPER = "extra.WALLPAPER"
        const val MSG_SET_WALLPAPER = 1

        fun getIntent(context: Context, action: String): Intent {
            return Intent(context, LiveAutoWallpaperService::class.java).apply {
                this.action = action
            }
        }
    }
}