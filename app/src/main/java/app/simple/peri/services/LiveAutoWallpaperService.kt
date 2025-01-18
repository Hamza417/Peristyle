package app.simple.peri.services

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import app.simple.peri.abstraction.AutoWallpaperUtils.getBitmapFromFile
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.ParcelUtils.parcelable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
                    getBitmapFromFile(filePath, canvas.width, canvas.height, recycle = false) { bmp ->
                        bitmap = bmp
                    }
                }

                surfaceHolder.unlockCanvasAndPost(canvas)
                onSurfaceRedrawNeeded(surfaceHolder)
            }
        }

        override fun onSurfaceRedrawNeeded(holder: SurfaceHolder?) {
            super.onSurfaceRedrawNeeded(holder)
            holder?.let {
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