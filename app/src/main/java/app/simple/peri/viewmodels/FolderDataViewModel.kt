package app.simple.peri.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.extensions.CompressorViewModel
import app.simple.peri.models.Folder
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.preferences.SharedPreferences.registerSharedPreferenceChangeListener
import app.simple.peri.preferences.SharedPreferences.unregisterSharedPreferenceChangeListener
import app.simple.peri.repository.WallpaperRepository
import app.simple.peri.utils.WallpaperSort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

class FolderDataViewModel(application: Application, private val folder: Folder) :
        CompressorViewModel(application), OnSharedPreferenceChangeListener {

    private val wallpaperRepository: WallpaperRepository
    private val _wallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())
    private val wallpapers: StateFlow<List<Wallpaper>> = _wallpapers

    init {
        val wallpaperDao = WallpaperDatabase.getInstance(application)?.wallpaperDao()
        wallpaperRepository = WallpaperRepository(wallpaperDao!!)
        registerSharedPreferenceChangeListener()
        loadWallpapers()
    }

    fun getWallpapers(): StateFlow<List<Wallpaper>> = wallpapers

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.SORT, MainPreferences.ORDER -> {
                _wallpapers.value = _wallpapers.value.getSortedList()
            }
        }
    }

    override suspend fun onCompressionDone(wallpaper: Wallpaper, file: File): Wallpaper {
        return postNewWallpaper(file, wallpaper)
    }

    private suspend fun postNewWallpaper(file: File, previousWallpaper: Wallpaper): Wallpaper {
        val wallpaper = Wallpaper.createFromFile(file, getApplication())
        wallpaper.id = previousWallpaper.id
        wallpaper.folderID = previousWallpaper.folderID
        val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
        val wallpaperDao = wallpaperDatabase?.wallpaperDao()
        wallpaperDao?.insert(wallpaper)
        _wallpapers.value = emptyList() // TODO = Flow will not emit the list until everything clears here
        return wallpaper
    }

    private fun loadWallpapers() {
        viewModelScope.launch {
            wallpaperRepository.getAllWallpapers().collect { updatedWallpapers ->
                val filteredWallpapers = updatedWallpapers.filter { it.folderID == folder.hashcode }
                _wallpapers.value = filteredWallpapers.getSortedList()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        this.unregisterSharedPreferenceChangeListener()
    }

    fun deleteWallpaper(deletedWallpaper: Wallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.delete(deletedWallpaper)
        }
    }
}
