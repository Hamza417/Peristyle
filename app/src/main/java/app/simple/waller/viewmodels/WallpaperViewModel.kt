package app.simple.waller.viewmodels

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.waller.database.instances.WallpaperDatabase
import app.simple.waller.models.Wallpaper
import app.simple.waller.preferences.MainPreferences
import app.simple.waller.utils.FileUtils.isImageFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private val wallpapers: ArrayList<Wallpaper> = ArrayList()

    private val wallpapersData: MutableLiveData<ArrayList<Wallpaper>> by lazy {
        MutableLiveData<ArrayList<Wallpaper>>().also {
            loadWallpaperDatabase()
        }
    }

    fun getWallpapersLiveData(): MutableLiveData<ArrayList<Wallpaper>> {
        return wallpapersData
    }

    private fun loadWallpaperDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            val wallpaperList = wallpaperDao?.getWallpapers()
            wallpapersData.postValue(wallpaperList as ArrayList<Wallpaper>)

            loadWallpaperImages()
        }
    }

    private fun loadWallpaperImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val storageUri = MainPreferences.getStorageUri()
            val pickedDirectory = DocumentFile.fromTreeUri(getApplication(), Uri.parse(storageUri))

            pickedDirectory?.listFiles()?.forEach {
                val wallpaper = Wallpaper()

                if (it.isDirectory) {
                    it.listFiles().forEach { file ->
                        if (file.isFile && file.isImageFile()) {
                            wallpaper.name = file.name
                            wallpaper.uri = file.uri.toString()
                            wallpaper.dateModified = file.lastModified()

                            getApplication<Application>().contentResolver.openInputStream(file.uri)?.use { inputStream ->
                                val options = BitmapFactory.Options()
                                options.inJustDecodeBounds = true
                                BitmapFactory.decodeStream(inputStream, null, options)
                                wallpaper.width = options.outWidth
                                wallpaper.height = options.outHeight
                            }

                            // Log.d(TAG, "loadWallpaperImages: ${wallpaper.name} ${wallpaper.uri} ${wallpaper.width} ${wallpaper.height}")
                        }
                    }
                } else if (it.isFile && it.isImageFile()) {
                    wallpaper.name = it.name
                    wallpaper.uri = it.uri.toString()
                    wallpaper.dateModified = it.lastModified()

                    getApplication<Application>().contentResolver.openInputStream(it.uri)?.use { inputStream ->
                        val options = BitmapFactory.Options()
                        options.inJustDecodeBounds = true
                        BitmapFactory.decodeStream(inputStream, null, options)
                        wallpaper.width = options.outWidth
                        wallpaper.height = options.outHeight
                    }

                    // Log.d(TAG, "loadWallpaperImages: ${wallpaper.name} ${wallpaper.uri} ${wallpaper.width} ${wallpaper.height}")
                }

                if (!wallpaper.isNull()) {
                    wallpapers.add(wallpaper)
                }
            }

            initDatabase()
        }
    }

    private fun initDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()

            wallpaperDao?.getWallpapers()?.forEach {
                try {
                    getApplication<Application>().contentResolver.openInputStream(Uri.parse(it.uri))?.use { inputStream ->
                        Log.d(TAG, "initDatabase: ${inputStream.available()}")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "initDatabase: ${e.message}")
                    wallpaperDao.delete(it)
                }
            }

            wallpapers.forEach {
                wallpaperDao?.insert(it)
            }
        }
    }

    companion object {
        private const val TAG = "WallpaperViewModel"
    }
}