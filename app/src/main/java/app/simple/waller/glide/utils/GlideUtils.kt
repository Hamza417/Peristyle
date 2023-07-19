package app.simple.waller.glide.utils

import android.graphics.Bitmap
import android.widget.ImageView
import app.simple.waller.glide.modules.GlideApp
import app.simple.waller.models.Wallpaper
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

object GlideUtils {

    fun ImageView.loadWallpaper(wallpaper: Wallpaper) {
        GlideApp.with(context)
            .asBitmap()
            .load(app.simple.waller.glide.wallpaper.Wallpaper(wallpaper, context))
            .transition(BitmapTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(this)
    }

    fun ImageView.loadWallpaper(wallpaper: Wallpaper, onLoad: () -> Unit) {
        GlideApp.with(context)
            .asBitmap()
            .load(app.simple.waller.glide.wallpaper.Wallpaper(wallpaper, context))
            .transition(BitmapTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    /* no-op */
                    return false
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    onLoad()
                    return false
                }
            })
            .into(this)
    }
}