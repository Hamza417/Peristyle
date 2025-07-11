package app.simple.peri.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.WallpaperUsage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class WallpaperUsageViewModel(application: Application) : AndroidViewModel(application) {

    private val _dataFlow = MutableStateFlow<List<WallpaperUsage>>(emptyList())
    val dataFlow = _dataFlow

    init {
        loadUsageData()
    }

    fun loadUsageData() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val usageList = WallpaperDatabase.getInstance(getApplication())?.wallpaperDao()?.getAllWallpaperUsage()!!
                _dataFlow.value = usageList
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}