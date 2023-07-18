package app.simple.waller.glide.modules

import android.content.Context
import android.graphics.Bitmap
import app.simple.waller.glide.wallpaper.Wallpaper
import app.simple.waller.glide.wallpaper.WallpaperLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class WallpaperModule : AppGlideModule() {
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun applyOptions(context: Context, builder: GlideBuilder) {

    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(Wallpaper::class.java, Bitmap::class.java, WallpaperLoader.Factory())
    }
}