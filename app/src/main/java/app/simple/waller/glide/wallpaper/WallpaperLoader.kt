package app.simple.waller.glide.wallpaper

import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class WallpaperLoader : ModelLoader<Wallpaper, Bitmap> {
    override fun buildLoadData(model: Wallpaper, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap>? {
        return ModelLoader.LoadData(ObjectKey(model), WallpaperFetcher(model))
    }

    override fun handles(model: Wallpaper): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<Wallpaper, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Wallpaper, Bitmap> {
            return WallpaperLoader()
        }

        override fun teardown() {
            /* no-op */
        }
    }
}