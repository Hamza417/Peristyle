package app.simple.peri.services

import android.content.Intent
import android.service.quicksettings.TileService
import app.simple.peri.abstraction.AbstractComposeAutoWallpaperService

class NextWallpaperTileService : TileService() {

    override fun onClick() {
        super.onClick()
        val intent = Intent(applicationContext, AutoWallpaperService::class.java)
        intent.action = AbstractComposeAutoWallpaperService.ACTION_NEXT_WALLPAPER
        applicationContext.startService(intent)
    }
}
