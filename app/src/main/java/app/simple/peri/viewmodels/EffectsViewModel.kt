package app.simple.peri.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.EffectsDatabase
import app.simple.peri.models.Effect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EffectsViewModel(application: Application) : AndroidViewModel(application) {

    private val effects: MutableLiveData<List<Effect>> by lazy {
        MutableLiveData<List<Effect>>().also {
            viewModelScope.launch(Dispatchers.IO) {
                val effectsDatabase = EffectsDatabase.getInstance(application)!!
                it.postValue(effectsDatabase.effectsDao().getAllEffects())
            }
        }
    }

    fun getEffects(): LiveData<List<Effect>> {
        return effects
    }

    fun deleteEffect(effect: Effect, onDeleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val effectsDatabase = EffectsDatabase.getInstance(getApplication())!!
            effectsDatabase.effectsDao().deleteEffect(effect)
            effects.postValue(effectsDatabase.effectsDao().getAllEffects())

            withContext(Dispatchers.Main) {
                onDeleted()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        EffectsDatabase.destroy()
    }
}
