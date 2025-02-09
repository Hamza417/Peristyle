package app.simple.peri.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import androidx.core.content.IntentCompat
import app.simple.peri.abstraction.AutoWallpaperUtils.getBitmapFromFile
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.SharedPreferences
import app.simple.peri.utils.BitmapUtils.applyEffects
import app.simple.peri.utils.ConditionUtils.invert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LiveAutoWallpaperService : WallpaperService() {

    private var engine: LiveAutoWallpaperEngine? = null
    private var handler: Handler? = null

    private var broadcastReceiver: BroadcastReceiver? = null

    private var transitionProgress = 0f // Progress of the fade (0.0 to 1.0)
    private var fadeStartTime: Long = -1L
    private var oldBitmap: Bitmap? = null
    private var isFading = false

    override fun onCreate() {
        super.onCreate()
        SharedPreferences.init(applicationContext)

        handler = Handler(Looper.getMainLooper()) { msg ->
            when (msg.what) {
                MSG_SET_WALLPAPER -> {
                    val filePath = msg.obj as String
                    engine!!.setWallpaper(filePath)
                }
            }
            true
        }

        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    when (intent.action) {
                        Intent.ACTION_SCREEN_ON -> {
                            if (MainComposePreferences.getChangeWhenOn()) {
                                askNextWallpaper()
                            }
                        }

                        Intent.ACTION_SCREEN_OFF -> {
                            if (MainComposePreferences.getChangeWhenOff()) {
                                askNextWallpaper()
                            }
                        }
                    }
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        }

        registerReceiver(broadcastReceiver, filter)
    }

    override fun onCreateEngine(): Engine {
        engine = LiveAutoWallpaperEngine()
        return engine!!
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            NEXT_WALLPAPER, PREVIEW_WALLPAPER -> {
                try {
                    val wallpaper = IntentCompat.getParcelableExtra(intent, EXTRA_WALLPAPER, Wallpaper::class.java)!!
                    Log.i(TAG, "Setting wallpaper: ${wallpaper.filePath}")
                    val msg = handler?.obtainMessage(MSG_SET_WALLPAPER, wallpaper.filePath)!!
                    handler?.sendMessage(msg)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
        handler?.removeCallbacksAndMessages(null)
    }

    // --------------------------------------------------------------------------------------------- //

    private inner class LiveAutoWallpaperEngine : Engine() {
        private var bitmap: Bitmap? = null

        fun setWallpaper(filePath: String) {
            CoroutineScope(Dispatchers.Main).launch {
                if (surfaceHolder.surface.isValid) {
                    val canvas = surfaceHolder.lockCanvas()
                    var localBitmap: Bitmap? = null

                    withContext(Dispatchers.Default) {
                        try {
                            getBitmapFromFile(filePath, canvas.width, canvas.height, recycle = false) { bmp ->
                                localBitmap = bmp
                            }
                        } catch (e: NullPointerException) {
                            if (localBitmap == null) {
                                localBitmap = bitmap
                            }
                        }
                    }

                    if (localBitmap != null) {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas)
                            setBitmapWithCrossfade(localBitmap!!)
                        }
                    }
                } else {
                    Log.i(TAG, "Surface is not valid, skipping wallpaper change")
                }
            }
        }

        fun setBitmapWithCrossfade(newBitmap: Bitmap) {
            if (bitmap != null) {
                oldBitmap = bitmap // Save the current bitmap for fading
                isFading = true
                fadeStartTime = System.currentTimeMillis()
                transitionProgress = 0f
            }
            bitmap = newBitmap.applyEffects(MainComposePreferences.getWallpaperEffects())
            onSurfaceRedrawNeeded(surfaceHolder) // Trigger the fade animation
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            if (isPreview) {
                askPreviewWallpaper()
            } else {
                askNextWallpaper()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder?) {
            super.onSurfaceRedrawNeeded(holder)
            holder?.let {
                val canvas: Canvas? = it.lockCanvas()
                canvas?.let { canvas1 ->
                    if (isFading && oldBitmap != null && bitmap != null) {
                        // Calculate the fade progress
                        val elapsedTime = System.currentTimeMillis() - fadeStartTime
                        transitionProgress = elapsedTime.toFloat() / CROSSFADE_DURATION

                        if (transitionProgress >= 1.0f) {
                            // End the fade and draw the new bitmap at full opacity
                            isFading = false
                            transitionProgress = 1.0f
                        }

                        // Draw the old bitmap
                        oldBitmap?.let { bmp ->
                            canvas1.drawBitmap(bmp, 0f, 0f, null)
                        }

                        // Overlay the new bitmap with alpha
                        bitmap?.let { bmp ->
                            val paint = Paint()
                            paint.alpha = (transitionProgress * 255).toInt()
                            canvas1.drawBitmap(bmp, 0f, 0f, paint)
                        }

                        // Trigger another redraw until the fade is complete
                        if (isFading) {
                            it.unlockCanvasAndPost(canvas1)
                            onSurfaceRedrawNeeded(holder)
                            return
                        } else {
                            // End the fade and recycle the old bitmap
                            oldBitmap?.recycle()
                            oldBitmap = null
                        }
                    } else {
                        // Draw the current bitmap normally if no fade is active
                        bitmap?.let { bmp ->
                            canvas1.drawBitmap(bmp, 0f, 0f, null)
                        }
                    }
                }

                if (canvas != null) {
                    it.unlockCanvasAndPost(canvas)
                }
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            if (isPreview.invert()) {
                super.onSurfaceDestroyed(holder)
                Log.i(TAG, "wallpaper surface destroyed")
                bitmap?.recycle()
                bitmap = null
                oldBitmap?.recycle()
                oldBitmap = null
            } else {
                Log.i(TAG, "Preview mode, skipping destruction")
            }
        }
    }

    private fun askPreviewWallpaper() {
        val intent = Intent(applicationContext, AutoWallpaperService::class.java)
        intent.action = AutoWallpaperService.RANDOM_PREVIEW_WALLPAPER
        applicationContext.startService(intent)
    }

    private fun askNextWallpaper() {
        val intent = Intent(applicationContext, AutoWallpaperService::class.java)
        intent.action = AutoWallpaperService.ACTION_NEXT_WALLPAPER
        applicationContext.startService(intent)
    }

    companion object {
        private const val TAG = "LiveAutoWallpaperService"
        const val NEXT_WALLPAPER = "action.SAME_WALLPAPER"
        const val PREVIEW_WALLPAPER = "action.PREVIEW_WALLPAPER"
        const val EXTRA_WALLPAPER = "extra.WALLPAPER"
        const val MSG_SET_WALLPAPER = 1
        private const val CROSSFADE_DURATION = 500L

        fun getIntent(context: Context, action: String): Intent {
            return Intent(context, LiveAutoWallpaperService::class.java).apply {
                this.action = action
            }
        }

        fun getIntent(context: Context): Intent {
            return Intent(context, LiveAutoWallpaperService::class.java)
        }
    }
}
