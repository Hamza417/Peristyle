package app.simple.peri.glide.tags

import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class TagsLoader : ModelLoader<Tag, Bitmap> {
    override fun buildLoadData(model: Tag, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        return ModelLoader.LoadData(ObjectKey(model), TagsFetcher(model))
    }

    override fun handles(model: Tag): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<Tag, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Tag, Bitmap> {
            return TagsLoader()
        }

        override fun teardown() {
            /* no-op */
        }
    }
}
