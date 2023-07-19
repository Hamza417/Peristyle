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
import app.simple.waller.utils.ConditionUtils.invert
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

    private val newWallpapersData: MutableLiveData<Wallpaper> by lazy {
        MutableLiveData<Wallpaper>()
    }

    private val removedWallpapersData: MutableLiveData<Wallpaper> by lazy {
        MutableLiveData<Wallpaper>()
    }

    fun getWallpapersLiveData(): MutableLiveData<ArrayList<Wallpaper>> {
        return wallpapersData
    }

    fun getNewWallpapersLiveData(): MutableLiveData<Wallpaper> {
        return newWallpapersData
    }

    fun getRemovedWallpapersLiveData(): MutableLiveData<Wallpaper> {
        return removedWallpapersData
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
            val alreadyLoaded = WallpaperDatabase.getInstance(getApplication())?.wallpaperDao()?.getWallpapers()?.associateBy { it.uri }
            Log.d(TAG, "SIZE: ${alreadyLoaded?.size}")
            val storageUri = MainPreferences.getStorageUri()
            val pickedDirectory = DocumentFile.fromTreeUri(getApplication(), Uri.parse(storageUri))

            pickedDirectory?.listFiles()?.forEach {
                val wallpaper = Wallpaper()
                var alreadyLoadedWallpaper = false

                if (it.isDirectory) {
                    it.listFiles().forEach { file ->
                        if (file.isFile && file.isImageFile()) {
                            val exists = kotlin.runCatching {
                                alreadyLoaded?.containsKey(file.uri.toString()) ?: false
                            }.getOrElse {
                                false
                            }

                            if (exists.invert()) {
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

                                Log.d(TAG, "loadWallpaperImages: ${wallpaper.name}, ${wallpaper.width}, ${wallpaper.height}")
                            } else {
                                Log.d(TAG, "loadWallpaperImages: ${file.name} already loaded")
                                alreadyLoadedWallpaper = true
                            }
                        }
                    }
                } else if (it.isFile && it.isImageFile()) {
                    val exists = kotlin.runCatching {
                        alreadyLoaded?.containsKey(it.uri.toString()) ?: false
                    }.getOrElse {
                        false
                    }

                    if (exists.invert()) {
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

                        Log.d(TAG, "loadWallpaperImages: ${wallpaper.name}, ${wallpaper.width}, ${wallpaper.height}")
                    } else {
                        Log.d(TAG, "loadWallpaperImages: ${it.name} already loaded")
                        alreadyLoadedWallpaper = true
                    }
                }

                if (wallpaper.isNull().invert()) {
                    if (alreadyLoadedWallpaper.invert()) {
                        wallpapers.add(wallpaper)
                        newWallpapersData.postValue(wallpaper)
                        Log.d(TAG, "loadWallpaperImages: ${wallpaper.name} added")
                    }
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
                    getApplication<Application>().contentResolver.openInputStream(Uri.parse(it.uri))?.use { _ ->
                        Log.d(TAG, "initDatabase: ${it.name} exists")
                    }
                } catch (e: Exception) {
                    Log.d(TAG, "initDatabase: ${it.name} doesn't exist")
                    wallpaperDao.delete(it)
                    removedWallpapersData.postValue(it)
                }
            }

            wallpapers.forEach {
                wallpaperDao?.insert(it)
            }
        }
    }

    private fun ArrayList<Wallpaper>.toHashMap(): HashMap<Long, Wallpaper> {
        val hashMap = HashMap<Long, Wallpaper>()
        this.forEach {
            hashMap[it.dateModified] = it
        }
        return hashMap
    }

    companion object {
        private const val TAG = "WallpaperViewModel"
    }
}