package app.simple.waller.glide.utils

import android.widget.ImageView
import app.simple.waller.glide.modules.GlideApp
import app.simple.waller.models.Wallpaper
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions

object GlideUtils {

    fun ImageView.loadWallpaper(wallpaper: Wallpaper) {
        GlideApp.with(context)
            .asBitmap()
            .load(app.simple.waller.glide.wallpaper.Wallpaper(wallpaper, context))
            .transition(BitmapTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(this)
    }
}