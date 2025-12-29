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
import android.view.Choreographer
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.core.content.IntentCompat
import app.simple.peri.abstraction.AutoWallpaperUtils.getBitmapFromFile
import app.simple.peri.extensions.DoubleTapListener
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.SharedPreferences
import app.simple.peri.utils.BitmapUtils.applyEffects
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.utils.WallpaperServiceNotification.showWallpaperChangedNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.FileNotFoundException

class LiveAutoWallpaperService : WallpaperService() {

    private var engine: LiveAutoWallpaperEngine? = null
    private var handler: Handler? = null

    private var broadcastReceiver: BroadcastReceiver? = null

    private var transitionProgress = 0f // Progress of the fade (0.0 to 1.0)
    private var fadeStartTime: Long = -1L
    private var oldBitmap: Bitmap? = null
    private var isFading = false

    // Coalesce rapid triggers (screen on/off + double-tap storms)
    private val requestScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var requestNextJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        SharedPreferences.init(applicationContext)

        handler = Handler(Looper.getMainLooper()) { msg ->
            when (msg.what) {
                MSG_SET_WALLPAPER -> {
                    val filePath = msg.obj as? String
                    if (filePath.isNullOrBlank()) {
                        Log.w(TAG, "MSG_SET_WALLPAPER received with null/blank path")
                        return@Handler true
                    }
                    engine?.setWallpaper(filePath)
                }
            }
            true
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        if (MainComposePreferences.getChangeWhenOn()) {
                            requestNextWallpaperDebounced(REASON_SCREEN_ON)
                        }
                    }

                    Intent.ACTION_SCREEN_OFF -> {
                        if (MainComposePreferences.getChangeWhenOff()) {
                            requestNextWallpaperDebounced(REASON_SCREEN_OFF)
                        }
                    }
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
                val wallpaper = IntentCompat.getParcelableExtra(intent, EXTRA_WALLPAPER, Wallpaper::class.java)
                if (wallpaper == null) {
                    Log.w(TAG, "No wallpaper extra found for action=${intent.action}")
                } else {
                    Log.i(TAG, "Setting wallpaper: ${wallpaper.filePath}")
                    val msg = handler?.obtainMessage(MSG_SET_WALLPAPER, wallpaper.filePath)
                    if (msg != null) {
                        handler?.sendMessage(msg)
                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (_: Exception) {
            // Receiver may already be unregistered in some edge cases.
        }
        handler?.removeCallbacksAndMessages(null)
        requestScope.cancel()
    }

    // --------------------------------------------------------------------------------------------- //

    private inner class LiveAutoWallpaperEngine : Engine() {

        // Simplified gesture detector for double-tap functionality
        private val gestureDetector = GestureDetector(
                applicationContext,
                DoubleTapListener {
                    if (MainComposePreferences.getDoubleTapToChange()) {
                        requestNextWallpaperDebounced(REASON_DOUBLE_TAP)
                        true
                    } else {
                        Log.w(TAG, "Double tap to change wallpaper is disabled in preferences")
                        false
                    }
                }
        )

        private var bitmap: Bitmap? = null

        // Keep work tied to engine lifecycle
        private val engineJob = SupervisorJob()
        private val engineScope = CoroutineScope(engineJob + Dispatchers.Main.immediate)
        private var setWallpaperJob: Job? = null

        private var surfaceWidth: Int = 0
        private var surfaceHeight: Int = 0

        private val fadePaint = Paint()

        // Crossfade scheduler (avoids recursive redraw)
        private var frameScheduled = false
        private val frameCallback = Choreographer.FrameCallback {
            frameScheduled = false
            if (surfaceHolder.surface.isValid) {
                drawFrameOnce(surfaceHolder)
                if (isFading) {
                    scheduleNextFrame()
                }
            }
        }

        private fun scheduleNextFrame() {
            if (frameScheduled) return
            frameScheduled = true
            Choreographer.getInstance().postFrameCallback(frameCallback)
        }

        private fun cancelFrames() {
            if (!frameScheduled) return
            frameScheduled = false
            Choreographer.getInstance().removeFrameCallback(frameCallback)
        }

        fun setWallpaper(filePath: String) {
            // Keep only the latest request, cancel in-flight decode/effects.
            setWallpaperJob?.cancel()
            setWallpaperJob = engineScope.launch {
                if (!surfaceHolder.surface.isValid) {
                    Log.i(TAG, "Surface is not valid, skipping wallpaper change")
                    return@launch
                }

                val targetW = surfaceWidth.takeIf { it > 0 } ?: surfaceHolder.surfaceFrame.width()
                val targetH = surfaceHeight.takeIf { it > 0 } ?: surfaceHolder.surfaceFrame.height()

                if (targetW <= 0 || targetH <= 0) {
                    Log.w(TAG, "Surface size unavailable (w=$targetW h=$targetH), skipping wallpaper change")
                    return@launch
                }

                val decoded: Bitmap? = withContext(Dispatchers.Default) {
                    var localBitmap: Bitmap? = null
                    try {
                        getBitmapFromFile(filePath, targetW, targetH, recycle = false) { bmp ->
                            localBitmap = bmp
                        }
                    } catch (_: NullPointerException) {
                        // Keep behavior consistent with previous code.
                    } catch (e: FileNotFoundException) {
                        Log.e(TAG, "File not found: $filePath", e)
                    }
                    localBitmap
                }

                val finalBitmap = decoded ?: bitmap
                if (finalBitmap == null) {
                    Log.w(TAG, "Failed to decode wallpaper and no previous bitmap available")
                    return@launch
                }

                setBitmapWithCrossfade(finalBitmap)
                MainComposePreferences.setLastLiveWallpaperPath(filePath)
                if (MainComposePreferences.getAutoWallpaperNotification()) {
                    showWallpaperChangedNotification(true, filePath.toFile(), finalBitmap)
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

            // Trigger the fade animation using scheduled frames.
            scheduleNextFrame()
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            if (isPreview) {
                askPreviewWallpaper()
            } else {
                requestNextWallpaperDebounced(REASON_SURFACE_CREATED)
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceWidth = width
            surfaceHeight = height
        }

        private fun drawFrameOnce(holder: SurfaceHolder) {
            val canvas: Canvas? = try {
                holder.lockCanvas()
            } catch (e: Exception) {
                Log.w(TAG, "lockCanvas failed", e)
                null
            }

            if (canvas == null) return

            try {
                when {
                    MainComposePreferences.getDisableAnimations() -> {
                        bitmap?.let { bmp ->
                            canvas.drawBitmap(bmp, 0f, 0f, null)
                        }
                        // Ensure we don't keep scheduling frames.
                        isFading = false
                        oldBitmap?.recycle()
                        oldBitmap = null
                    }

                    isFading && oldBitmap != null && bitmap != null -> {
                        val elapsedTime = System.currentTimeMillis() - fadeStartTime
                        transitionProgress = (elapsedTime.toFloat() / CROSSFADE_DURATION).coerceIn(0f, 1f)

                        if (transitionProgress >= 1.0f) {
                            isFading = false
                        }

                        oldBitmap?.let { bmp ->
                            canvas.drawBitmap(bmp, 0f, 0f, null)
                        }

                        bitmap?.let { bmp ->
                            fadePaint.alpha = (transitionProgress * 255).toInt().coerceIn(0, 255)
                            canvas.drawBitmap(bmp, 0f, 0f, fadePaint)
                        }

                        if (!isFading) {
                            oldBitmap?.recycle()
                            oldBitmap = null
                        }
                    }

                    else -> {
                        bitmap?.let { bmp ->
                            canvas.drawBitmap(bmp, 0f, 0f, null)
                        }
                    }
                }
            } finally {
                try {
                    holder.unlockCanvasAndPost(canvas)
                } catch (e: Exception) {
                    Log.w(TAG, "unlockCanvasAndPost failed", e)
                }
            }
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder?) {
            super.onSurfaceRedrawNeeded(holder)
            if (holder == null) return

            // Draw once, then schedule more frames if we’re mid-fade.
            if (!holder.surface.isValid) return
            drawFrameOnce(holder)
            if (isFading) {
                scheduleNextFrame()
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            cancelFrames()
            setWallpaperJob?.cancel()
            engineJob.cancel()

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

        override fun onTouchEvent(event: MotionEvent?) {
            if (event != null) {
                gestureDetector.onTouchEvent(event)
            } else {
                Log.w(TAG, "Received null MotionEvent in onTouchEvent")
            }
        }
    }

    private fun requestNextWallpaperDebounced(reason: String) {
        // Debounce window: coalesce storms of triggers into one request.
        requestNextJob?.cancel()
        requestNextJob = requestScope.launch {
            delay(NEXT_WALLPAPER_DEBOUNCE_MS)
            Log.d(TAG, "Requesting next wallpaper (debounced), reason=$reason")
            askNextWallpaper()
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
        private const val NEXT_WALLPAPER_DEBOUNCE_MS = 400L

        private const val REASON_SCREEN_ON = "screen_on"
        private const val REASON_SCREEN_OFF = "screen_off"
        private const val REASON_DOUBLE_TAP = "double_tap"
        private const val REASON_SURFACE_CREATED = "surface_created"

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
