package app.simple.peri.services

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

class LiveAutoWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return LiveAutoWallpaperEngine()
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
}
