package app.simple.peri.glide.modules

import android.content.Context
import android.graphics.Bitmap
import app.simple.peri.glide.tags.ContextTag
import app.simple.peri.glide.tags.TagsLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.LibraryGlideModule

@GlideModule
class TagModule : LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(ContextTag::class.java, Bitmap::class.java, TagsLoader.Factory())
    }
}
