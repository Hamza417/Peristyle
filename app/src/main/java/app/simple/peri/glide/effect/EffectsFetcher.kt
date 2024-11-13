package app.simple.peri.glide.effect

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import app.simple.peri.utils.BitmapUtils.applyEffects
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EffectsFetcher(private val effect: Effect) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        Glide.with(effect.context)
            .asBitmap()
            .load(effect.wallpaper.filePath)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : CustomTarget<Bitmap>(effect.wallpaper.width!!.div(2), effect.wallpaper.height!!.div(2)) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val processedBitmap = withContext(Dispatchers.Default) {
                            resource.applyEffects(
                                    effect.effect.blurValue,
                                    effect.effect.brightnessValue,
                                    effect.effect.contrastValue,
                                    effect.effect.saturationValue,
                                    effect.effect.hueRedValue,
                                    effect.effect.hueGreenValue,
                                    effect.effect.hueBlueValue,
                                    effect.effect.scaleRedValue,
                                    effect.effect.scaleGreenValue,
                                    effect.effect.scaleBlueValue
                            )
                        }
                        callback.onDataReady(processedBitmap)
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle cleanup if necessary
                }
            })
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
