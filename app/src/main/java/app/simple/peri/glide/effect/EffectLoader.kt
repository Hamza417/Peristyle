package app.simple.peri.glide.effect

import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class EffectLoader : ModelLoader<Effect, Bitmap> {
    override fun handles(model: Effect): Boolean {
        return true
    }

    override fun buildLoadData(model: Effect, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap>? {
        return ModelLoader.LoadData(ObjectKey(model), EffectsFetcher(model))
    }

    internal class Factory : ModelLoaderFactory<Effect, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Effect, Bitmap> {
            return EffectLoader()
        }

        override fun teardown() {
            /* no-op */
        }
    }
}
