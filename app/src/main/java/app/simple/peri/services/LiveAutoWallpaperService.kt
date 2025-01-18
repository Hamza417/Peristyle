package app.simple.peri.services

import android.content.Context
import android.content.Intent
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder

class LiveAutoWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return LiveAutoWallpaperEngine()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            SAME_WALLPAPER -> {
                Log.i("LiveAutoWallpaperService", "Setting the same wallpaper")
            }
            HOME_SCREEN_WALLPAPER -> {
                Log.i("LiveAutoWallpaperService", "Setting the wallpaper for the home screen")
            }
            LOCK_SCREEN_WALLPAPER -> {
                Log.i("LiveAutoWallpaperService", "Setting the wallpaper for the lock screen")
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private inner class LiveAutoWallpaperEngine : Engine() {
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
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
        }
    }

    companion object {
        const val SAME_WALLPAPER = "action.SAME_WALLPAPER"
        const val HOME_SCREEN_WALLPAPER = "action.HOME_SCREEN_WALLPAPER"
        const val LOCK_SCREEN_WALLPAPER = "action.LOCK_SCREEN_WALLPAPER"
        const val EXTRA_WALLPAER = "extra.WALLPAPER"

        fun getIntent(context: Context, action: String): Intent {
            return Intent(context, LiveAutoWallpaperService::class.java).apply {
                this.action = action
            }
        }
    }
}
