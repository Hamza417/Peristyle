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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StateViewModel(application: Application) : AndroidViewModel(application) {
    var tag by mutableStateOf<Tag?>(null)
    var folder by mutableStateOf<Folder?>(null)
    var blurValue by mutableFloatStateOf(0f) // 0F..25F
    var brightnessValue by mutableFloatStateOf(0f) // -255F..255F
    var contrastValue by mutableFloatStateOf(1f) // 0F..10F
    var saturationValue by mutableFloatStateOf(1f) // 0F..2F
    var hueValueRed by mutableFloatStateOf(0f) // 0F..360F
    var hueValueGreen by mutableFloatStateOf(0f) // 0F..360F
    var hueValueBlue by mutableFloatStateOf(0f) // 0F..360F

    fun saveEffectInDatabase(effect: Effect, onEffectSaved: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val effectDao = EffectsDatabase.getInstance(getApplication())?.effectsDao()
            effectDao?.insertEffect(effect)

            withContext(Dispatchers.Main) {
                onEffectSaved()
            }
        }
    }
}
