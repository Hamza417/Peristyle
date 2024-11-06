package app.simple.peri.services

import android.content.Intent
import android.service.quicksettings.TileService

class NextWallpaperTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val intent = Intent(applicationContext, AutoWallpaperService::class.java)
        intent.action = AutoWallpaperService.ACTION_NEXT_WALLPAPER
        applicationContext.startService(intent)
    }
}
