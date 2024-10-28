package app.simple.peri.glide.effect

import android.graphics.Bitmap
import app.simple.peri.utils.BitmapUtils.applyEffects
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.engine.DiskCacheStrategy

class EffectsFetcher(private val effect: Effect) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        val bitmap = Glide.with(effect.context)
            .asBitmap()
            .load(effect.wallpaper.filePath)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .submit(effect.wallpaper.width!!.div(2), effect.wallpaper.height!!.div(2))

        callback.onDataReady(bitmap.get().applyEffects(
                effect.effect.blurValue,
                effect.effect.brightnessValue,
                effect.effect.contrastValue,
                effect.effect.saturationValue,
                effect.effect.hueRedValue,
                effect.effect.hueGreenValue,
                effect.effect.hueBlueValue))

        Glide.with(effect.context).clear(bitmap)
    }

    override fun cleanup() {
        /* no-op */
    }

    override fun cancel() {
        /* no-op */
    }

    override fun getDataClass(): Class<Bitmap> {
        return Bitmap::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

}
