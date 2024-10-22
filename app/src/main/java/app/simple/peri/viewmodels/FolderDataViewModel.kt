package app.simple.peri.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.documentfile.provider.DocumentFile
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

class FolderDataViewModel(application: Application, private val folder: Folder) :
        CompressorViewModel(application), OnSharedPreferenceChangeListener {

    private val wallpaperRepository: WallpaperRepository
    private val _wallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())
    private val wallpapers: StateFlow<List<Wallpaper>> = _wallpapers

    init {
        val wallpaperDao = WallpaperDatabase.getInstance(application)?.wallpaperDao()
        wallpaperRepository = WallpaperRepository(wallpaperDao!!)

        viewModelScope.launch {
            wallpaperRepository.getAllWallpapers().collect { updatedWallpapers ->
                val filteredWallpapers = updatedWallpapers.filter { it.uriHashcode == folder.hashcode }
                _wallpapers.value = filteredWallpapers
            }
        }

        registerSharedPreferenceChangeListener()
    }

    fun getWallpapers(): StateFlow<List<Wallpaper>> = wallpapers

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.SORT, MainPreferences.ORDER -> {
                _wallpapers.value = _wallpapers.value.getSortedList()
            }
        }
    }

    override fun onCompressionDone(wallpaper: Wallpaper, documentFile: DocumentFile): Wallpaper {
        return postNewWallpaper(documentFile, wallpaper)
    }

    private fun postNewWallpaper(documentFile: DocumentFile, previousWallpaper: Wallpaper): Wallpaper {
        val wallpaper = Wallpaper().createFromUri(documentFile.uri.toString(), getApplication())
        wallpaper.md5 = previousWallpaper.md5
        wallpaper.uriHashcode = previousWallpaper.uriHashcode
        wallpaper.dateModified = previousWallpaper.dateModified
        val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
        val wallpaperDao = wallpaperDatabase?.wallpaperDao()
        wallpaperDao?.insert(wallpaper)
        return wallpaper
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
