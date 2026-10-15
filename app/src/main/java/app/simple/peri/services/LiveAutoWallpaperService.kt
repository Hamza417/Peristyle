package app.simple.peri.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path.Direction
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.Choreographer
import android.view.Display
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.WindowManager
import androidx.core.content.IntentCompat
import app.simple.peri.abstraction.AutoWallpaperUtils.getBitmapFromFile
import app.simple.peri.extensions.WallpaperGestureProcessor
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
import kotlin.math.hypot
import kotlin.time.Duration.Companion.milliseconds

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
                    val req = msg.obj as? SetWallpaperRequest
                    val filePath = req?.filePath
                    if (filePath.isNullOrBlank()) {
                        Log.w(TAG, "MSG_SET_WALLPAPER received with null/blank path")
                        return@Handler true
                    }
                    engine?.setWallpaper(filePath, isPreviewRequest = req.isPreviewRequest)
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
                    val isPreviewRequest = intent.action == PREVIEW_WALLPAPER
                    Log.i(TAG, "Setting wallpaper: ${wallpaper.filePath} (preview=$isPreviewRequest)")
                    val msg = handler?.obtainMessage(
                            MSG_SET_WALLPAPER,
                            SetWallpaperRequest(wallpaper.filePath, isPreviewRequest)
                    )
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

        private lateinit var gestureProcessor: WallpaperGestureProcessor

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

        fun setWallpaper(filePath: String, isPreviewRequest: Boolean) {
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
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
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

                // Preview wallpaper updates should never post notifications.
                if (MainComposePreferences.getAutoWallpaperNotification() && !isPreview && !isPreviewRequest) {
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

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            if (isPreview) {
                askPreviewWallpaper()
            } else {
                requestNextWallpaperDebounced(REASON_SURFACE_CREATED)
            }

            gestureProcessor = WallpaperGestureProcessor(
                    context = getUiSafeContext(),
                    onDoubleTap = {
                        Log.d(TAG, "Double-tap detected")
                        if (MainComposePreferences.isDoubleTapToChange()) {
                            requestNextWallpaperDebounced(REASON_GESTURE_TRIGGERED)
                        } else {
                            Log.d(TAG, "Double-tap gesture is disabled, ignoring")
                        }
                    },
                    onThreeFingerSwipe = { direction ->
                        Log.d(TAG, "Three-finger swipe detected: $direction")

                        if (MainComposePreferences.isThreeFingerSwipeUpToChange()) {
                            when (direction) {
                                WallpaperGestureProcessor.Direction.UP -> {
                                    Log.d(TAG, "Three-finger swipe UP detected")
                                    requestNextWallpaperDebounced(REASON_GESTURE_TRIGGERED)
                                }
                                WallpaperGestureProcessor.Direction.DOWN -> {
                                    Log.d(TAG, "Three-finger swipe DOWN detected")
                                    requestNextWallpaperDebounced(REASON_GESTURE_TRIGGERED)
                                }
                                WallpaperGestureProcessor.Direction.LEFT -> {
                                    Log.d(TAG, "Three-finger swipe LEFT detected")
                                    // Handle three-finger swipe left action here
                                }
                                WallpaperGestureProcessor.Direction.RIGHT -> {
                                    Log.d(TAG, "Three-finger swipe RIGHT detected")
                                    // Handle three-finger swipe right action here
                                }
                            }
                        } else {
                            Log.d(TAG, "Three-finger swipe gesture is disabled, ignoring")
                        }
                    },
                    onTwoFingerSwipe = { direction ->
                        Log.d(TAG, "Two-finger swipe detected: $direction")

                        if (MainComposePreferences.isTwoFingerSwipeUpToChange()) {
                            when (direction) {
                                WallpaperGestureProcessor.Direction.UP -> {
                                    Log.d(TAG, "Two-finger swipe UP detected")
                                    requestNextWallpaperDebounced(REASON_GESTURE_TRIGGERED)
                                }
                                WallpaperGestureProcessor.Direction.DOWN -> {
                                    Log.d(TAG, "Two-finger swipe DOWN detected")
                                    requestNextWallpaperDebounced(REASON_GESTURE_TRIGGERED)
                                }
                                WallpaperGestureProcessor.Direction.LEFT -> {
                                    Log.d(TAG, "Two-finger swipe LEFT detected")
                                    // Handle two-finger swipe left action here
                                }
                                WallpaperGestureProcessor.Direction.RIGHT -> {
                                    Log.d(TAG, "Two-finger swipe RIGHT detected")
                                    // Handle two-finger swipe right action here
                                }
                            }
                        } else {
                            Log.d(TAG, "Two-finger swipe gesture is disabled, ignoring")
                        }
                    }
            )
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
                        val effect = MainComposePreferences.getLiveWallpaperTransitionEffect()

                        val duration = when (effect) {
                            TRANSITION_BLOCKS, TRANSITION_CIRCULAR_REVEAL -> {
                                1000L // Slower duration for some transitions if required
                            }
                            else -> {
                                CROSSFADE_DURATION
                            }
                        }

                        val elapsedTime = System.currentTimeMillis() - fadeStartTime
                        val linearProgress = (elapsedTime.toFloat() / duration).coerceIn(0f, 1f)

                        // Apply decelerate effect (ease-out quadratic)
                        transitionProgress = 1f - (1f - linearProgress) * (1f - linearProgress)

                        if (linearProgress >= 1.0f) {
                            isFading = false
                            transitionProgress = 1.0f
                        }

                        when (effect) {
                            TRANSITION_CROSSFADE -> {
                                oldBitmap?.let { bmp ->
                                    canvas.drawBitmap(bmp, 0f, 0f, null)
                                }

                                bitmap?.let { bmp ->
                                    fadePaint.alpha = (transitionProgress * 255).toInt().coerceIn(0, 255)
                                    canvas.drawBitmap(bmp, 0f, 0f, fadePaint)
                                }
                            }
                            TRANSITION_ZOOM -> {
                                oldBitmap?.let { bmp ->
                                    canvas.save()
                                    // Old bitmap zooms in slightly (1.0 -> 1.2) as it is covered
                                    val scale = 1.0f + (0.2f * transitionProgress)
                                    val px = bmp.width / 2f
                                    val py = bmp.height / 2f
                                    canvas.scale(scale, scale, px, py)
                                    canvas.drawBitmap(bmp, 0f, 0f, null)
                                    canvas.restore()
                                }

                                bitmap?.let { bmp ->
                                    canvas.save()
                                    val scale = 1.5f - (0.5f * transitionProgress)
                                    val px = bmp.width / 2f
                                    val py = bmp.height / 2f
                                    canvas.scale(scale, scale, px, py)
                                    fadePaint.alpha = (transitionProgress * 255).toInt().coerceIn(0, 255)
                                    canvas.drawBitmap(bmp, 0f, 0f, fadePaint)
                                    canvas.restore()
                                }
                            }
                            TRANSITION_CIRCULAR_REVEAL -> {
                                oldBitmap?.let { bmp ->
                                    canvas.drawBitmap(bmp, 0f, 0f, null)
                                }

                                bitmap?.let { bmp ->
                                    val centerX = bmp.width / 2f
                                    val centerY = bmp.height / 2f
                                    val maxRadius = hypot(centerX.toDouble(), centerY.toDouble()).toFloat()
                                    val currentRadius = maxRadius * transitionProgress

                                    val path = android.graphics.Path()
                                    path.addCircle(centerX, centerY, currentRadius, Direction.CW)

                                    canvas.save()
                                    canvas.clipPath(path)
                                    canvas.drawBitmap(bmp, 0f, 0f, null)
                                    canvas.restore()
                                }
                            }
                            TRANSITION_BLOCKS -> {
                                oldBitmap?.let { bmp ->
                                    canvas.drawBitmap(bmp, 0f, 0f, null)
                                }

                                bitmap?.let { bmp ->
                                    val cols = 8
                                    val rows = (cols * (bmp.height.toFloat() / bmp.width.toFloat()))
                                        .toInt()
                                        .coerceAtLeast(1)
                                    val blockW = bmp.width.toFloat() / cols
                                    val blockH = bmp.height.toFloat() / rows

                                    val totalDiagonals = cols + rows - 1
                                    val step = 1f / (totalDiagonals + 1)

                                    for (row in 0 until rows) {
                                        for (column in 0 until cols) {
                                            val diagonal = column + row
                                            val diagonalStart = diagonal * step
                                            val diagonalEnd = diagonalStart + (step * 2) // overlap slightly

                                            if (transitionProgress > diagonalStart) {
                                                val blockProgress =
                                                    ((transitionProgress - diagonalStart) / (diagonalEnd - diagonalStart))
                                                        .coerceIn(0f, 1f)

                                                val left = column * blockW
                                                val top = row * blockH
                                                val right = if (column == cols - 1) bmp.width.toFloat() else left + blockW
                                                val bottom = if (row == rows - 1) bmp.height.toFloat() else top + blockH

                                                val srcRect = Rect(
                                                        left.toInt(),
                                                        top.toInt(),
                                                        right.toInt(),
                                                        bottom.toInt())

                                                val dstRect = RectF(left, top, right, bottom)

                                                if (transitionProgress >= 1f || blockProgress >= 1f) {
                                                    canvas.drawBitmap(bmp, srcRect, dstRect, null)
                                                } else {
                                                    fadePaint.alpha = (blockProgress * 255)
                                                        .toInt()
                                                        .coerceIn(0, 255)

                                                    canvas.drawBitmap(bmp, srcRect, dstRect, fadePaint)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                oldBitmap?.let { bmp ->
                                    canvas.drawBitmap(bmp, 0f, 0f, null)
                                }

                                bitmap?.let { bmp ->
                                    fadePaint.alpha = (transitionProgress * 255).toInt().coerceIn(0, 255)
                                    canvas.drawBitmap(bmp, 0f, 0f, fadePaint)
                                }
                            }
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
                gestureProcessor.processTouchEvent(event)
            } else {
                Log.w(TAG, "Received null MotionEvent in onTouchEvent")
            }
        }
    }

    private fun requestNextWallpaperDebounced(reason: String) {
        // Debounce window - coalesce storms of triggers into one request.
        requestNextJob?.cancel()
        requestNextJob = requestScope.launch {
            delay(NEXT_WALLPAPER_DEBOUNCE_MS.milliseconds)
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

    private fun getUiSafeContext(): Context {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+: Create a proper WindowContext for the wallpaper
            val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
            createWindowContext(display, WindowManager.LayoutParams.TYPE_WALLPAPER, null)
        } else {
            // Android 11 and below: The Service context is perfectly fine
            this@LiveAutoWallpaperService
        }
    }

    companion object {
        private const val TAG = "LiveAutoWallpaperService"
        const val NEXT_WALLPAPER = "action.SAME_WALLPAPER"
        const val PREVIEW_WALLPAPER = "action.PREVIEW_WALLPAPER"
        const val EXTRA_WALLPAPER = "extra.WALLPAPER"
        const val MSG_SET_WALLPAPER = 1
        private const val CROSSFADE_DURATION = 500L
        private const val NEXT_WALLPAPER_DEBOUNCE_MS = 400L

        const val TRANSITION_CROSSFADE = 0
        const val TRANSITION_ZOOM = 1
        const val TRANSITION_BLOCKS = 2
        const val TRANSITION_CIRCULAR_REVEAL = 3

        private const val REASON_SCREEN_ON = "screen_on"
        private const val REASON_SCREEN_OFF = "screen_off"
        private const val REASON_GESTURE_TRIGGERED = "gesture_triggered"
        private const val REASON_SURFACE_CREATED = "surface_created"

        fun getIntent(context: Context, action: String): Intent {
            return Intent(context, LiveAutoWallpaperService::class.java).apply {
                this.action = action
            }
        }

        fun getIntent(context: Context): Intent {
            return Intent(context, LiveAutoWallpaperService::class.java)
        }

        private data class SetWallpaperRequest(
                val filePath: String,
                val isPreviewRequest: Boolean,
        )
    }
}
