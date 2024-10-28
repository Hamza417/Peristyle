package app.simple.peri.glide.modules

import android.content.Context
import android.graphics.Bitmap
import app.simple.peri.glide.effect.Effect
import app.simple.peri.glide.effect.EffectLoader
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.LibraryGlideModule

@GlideModule
class EffectModule : LibraryGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(Effect::class.java, Bitmap::class.java, EffectLoader.Factory())
    }
}
