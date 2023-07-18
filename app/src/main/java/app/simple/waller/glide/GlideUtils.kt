package app.simple.waller.glide

import android.widget.ImageView
import app.simple.waller.glide.modules.GlideApp
import app.simple.waller.models.Wallpaper

object GlideUtils {

    fun ImageView.loadWallpaper(wallpaper: Wallpaper) {
        GlideApp.with(context)
            .load(app.simple.waller.glide.wallpaper.Wallpaper(wallpaper, context))
            .into(this)
    }
}