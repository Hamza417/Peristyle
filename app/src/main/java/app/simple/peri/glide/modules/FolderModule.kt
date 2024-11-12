package app.simple.peri.glide.modules

import android.content.Context
import android.graphics.Bitmap
import app.simple.peri.glide.folders.ContextFolder
import app.simple.peri.glide.folders.FolderLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.LibraryGlideModule

@GlideModule
class FolderModule : LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(ContextFolder::class.java, Bitmap::class.java, FolderLoader.Factory())
    }
}
