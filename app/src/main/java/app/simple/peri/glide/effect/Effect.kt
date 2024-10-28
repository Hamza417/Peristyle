package app.simple.peri.glide.effect

import android.content.Context
import app.simple.peri.models.Effect
import app.simple.peri.models.Wallpaper

class Effect(val context: Context, val effect: Effect, val wallpaper: Wallpaper) {
    override fun hashCode(): Int {
        return effect.hashCode() + wallpaper.filePath.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as app.simple.peri.glide.effect.Effect

        if (context != other.context) return false
        if (effect != other.effect) return false
        if (wallpaper != other.wallpaper) return false

        return true
    }
}
