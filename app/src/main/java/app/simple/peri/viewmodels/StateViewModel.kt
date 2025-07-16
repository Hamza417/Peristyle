package app.simple.peri.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.EffectsDatabase
import app.simple.peri.models.Effect
import app.simple.peri.models.Folder
import app.simple.peri.models.Tag
import app.simple.peri.models.WallhavenWallpaper
import app.simple.peri.models.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StateViewModel(application: Application) : AndroidViewModel(application) {

    private var _wallpaper by mutableStateOf<Any?>(null)
    var tag by mutableStateOf<Tag?>(null)
    var folder by mutableStateOf<Folder?>(null)
    var blurValue by mutableFloatStateOf(0f) // 0F..25F
    var brightnessValue by mutableFloatStateOf(0f) // -255F..255F
    var contrastValue by mutableFloatStateOf(1f) // 0F..10F
    var saturationValue by mutableFloatStateOf(1f) // 0F..2F
    var hueValueRed by mutableFloatStateOf(0f) // 0F..360F
    var hueValueGreen by mutableFloatStateOf(0f) // 0F..360F
    var hueValueBlue by mutableFloatStateOf(0f) // 0F..360F
    var scaleValueRed by mutableFloatStateOf(1f) // 0F..1F
    var scaleValueGreen by mutableFloatStateOf(1f) // 0F..1F
    var scaleValueBlue by mutableFloatStateOf(1f) // 0F..1F

    fun saveEffectInDatabase(effect: Effect, onEffectSaved: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val effectDao = EffectsDatabase.getInstance(getApplication())?.effectsDao()
            effectDao?.insertEffect(effect)

            withContext(Dispatchers.Main) {
                onEffectSaved()
            }
        }
    }

    fun setWallpaper(newWallpaper: Any?) {
        _wallpaper = newWallpaper
    }

    fun getWallpaper(): Any? {
        return _wallpaper
    }

    fun getWallpaperName(): String? {
        return when (_wallpaper) {
            is Wallpaper -> (_wallpaper as Wallpaper).name
            is WallhavenWallpaper -> (_wallpaper as WallhavenWallpaper).id
            else -> ""
        }
    }

    fun getWallpaperWidth(): Int? {
        return when (_wallpaper) {
            is Wallpaper -> (_wallpaper as Wallpaper).width
            is WallhavenWallpaper -> (_wallpaper as WallhavenWallpaper).resolution.split("x").firstOrNull()?.toIntOrNull()
            else -> null
        }
    }

    fun getWallpaperHeight(): Int? {
        return when (_wallpaper) {
            is Wallpaper -> (_wallpaper as Wallpaper).height
            is WallhavenWallpaper -> (_wallpaper as WallhavenWallpaper).resolution.split("x").lastOrNull()?.toIntOrNull()
            else -> null
        }
    }

    fun getWallpaperSize(): Long? {
        return when (_wallpaper) {
            is Wallpaper -> (_wallpaper as Wallpaper).size
            is WallhavenWallpaper -> (_wallpaper as WallhavenWallpaper).fileSize
            else -> null
        }
    }
}
