package app.simple.peri.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
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

    fun incrementUsageCount(wallpaper: Wallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val wallpaperDao = WallpaperDatabase.getInstance(getApplication())?.wallpaperDao()
                val usage = wallpaperDao?.getWallpaperUsageById(wallpaper.id)
                if (usage != null) {
                    usage.usageCount += 1
                    wallpaperDao.insert(usage)
                    Log.d("WallpaperUsageViewModel", "Incremented usage count for wallpaper ID: ${wallpaper.id}, new count: ${usage.usageCount}")
                } else {
                    val newUsage = WallpaperUsage(wallpaper.id, 1)
                    wallpaperDao?.insert(newUsage)
                    Log.d("WallpaperUsageViewModel", "Created new usage entry for wallpaper ID: ${wallpaper.id}, count: 1")
                }
                loadUsageData() // Refresh the data after updating
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}