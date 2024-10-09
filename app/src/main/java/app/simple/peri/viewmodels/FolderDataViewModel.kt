package app.simple.peri.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.extensions.CompressorViewModel
import app.simple.peri.models.Folder
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.preferences.SharedPreferences.registerSharedPreferenceChangeListener
import app.simple.peri.preferences.SharedPreferences.unregisterSharedPreferenceChangeListener
import app.simple.peri.utils.WallpaperSort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FolderDataViewModel(application: Application, private val folder: Folder) :
    CompressorViewModel(application), OnSharedPreferenceChangeListener {

    init {
        registerSharedPreferenceChangeListener()
    }

    private val wallpapersData: MutableLiveData<ArrayList<Wallpaper>> by lazy {
        MutableLiveData<ArrayList<Wallpaper>>().also {
            loadWallpaperDatabase()
        }
    }

    fun getWallpapers(): MutableLiveData<ArrayList<Wallpaper>> {
        return wallpapersData
    }

    private fun loadWallpaperDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.sanitizeEntries() // Sanitize the database
            val wallpaperList =
                wallpaperDao?.getWallpapersByUriHashcode(folder.hashcode)?.toMutableList()
                    ?: throw NullPointerException("Wallpaper list is null")

            (wallpaperList as ArrayList<Wallpaper>).getSortedList()

            for (i in wallpaperList.indices) {
                wallpaperList[i].isSelected = false
            }

            @Suppress("UNCHECKED_CAST")
            wallpapersData.postValue(wallpaperList.clone() as ArrayList<Wallpaper>)
        }
    }

    private fun postNewWallpaper(documentFile: DocumentFile, previousWallpaper: Wallpaper): Wallpaper {
        val wallpaper = Wallpaper().createFromUri(documentFile.uri.toString(), getApplication())
        wallpaper.md5 = previousWallpaper.md5
        wallpaper.uriHashcode = previousWallpaper.uriHashcode
        wallpaper.dateModified = previousWallpaper.dateModified
        val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
        val wallpaperDao = wallpaperDatabase?.wallpaperDao()
        wallpaperDao?.insert(wallpaper)
        loadWallpaperDatabase()
        return wallpaper
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.SORT -> {
                wallpapersData.value?.getSortedList()
                wallpapersData.postValue(wallpapersData.value)
            }

            MainPreferences.ORDER -> {
                wallpapersData.value?.getSortedList()
                wallpapersData.postValue(wallpapersData.value)
            }
        }
    }

    override fun onCompressionDone(wallpaper: Wallpaper, documentFile: DocumentFile): Wallpaper {
        return postNewWallpaper(documentFile, wallpaper)
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
            loadWallpaperDatabase()
        }
    }
}
