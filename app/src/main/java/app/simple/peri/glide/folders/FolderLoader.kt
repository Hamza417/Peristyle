package app.simple.peri.glide.folders

import android.graphics.Bitmap
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.signature.ObjectKey

class FolderLoader : ModelLoader<Folder, Bitmap> {
    override fun buildLoadData(model: Folder, width: Int, height: Int, options: Options): ModelLoader.LoadData<Bitmap> {
        return ModelLoader.LoadData(ObjectKey(model), FolderFetcher(model))
    }

    override fun handles(model: Folder): Boolean {
        return true
    }

    internal class Factory : ModelLoaderFactory<Folder, Bitmap> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<Folder, Bitmap> {
            return FolderLoader()
        }

        override fun teardown() {
            /* no-op */
        }
    }
}
