package app.simple.peri.glide.tags

import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class TagsLoader : ModelLoader<ContextTag, Bitmap> {
    override fun buildLoadData(model: ContextTag, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        return ModelLoader.LoadData(ObjectKey(model), TagsFetcher(model))
    }

    override fun handles(model: ContextTag): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<ContextTag, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ContextTag, Bitmap> {
            return TagsLoader()
        }

        override fun teardown() {
            /* no-op */
        }
    }
}
