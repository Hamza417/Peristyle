package app.simple.peri.viewmodels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.peri.constants.BundleConstants
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.FileUtils.isImageFile
import app.simple.peri.utils.WallpaperSort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private var broadcastReceiver: BroadcastReceiver? = null
    private val intentFilter = IntentFilter().apply {
        addAction(BundleConstants.INTENT_RECREATE_DATABASE)
    }
    private var wallpapers: ArrayList<Wallpaper> = ArrayList()
    private var isDatabaseLoaded = false

    init {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BundleConstants.INTENT_RECREATE_DATABASE) {
                    recreateDatabase()
                }
            }
        }

        LocalBroadcastManager.getInstance(application).registerReceiver(broadcastReceiver!!, intentFilter)
    }

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

    private val loadingStatus: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val isNomediaDirectory: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            if (MainPreferences.getShowNomediaDialog()) {
                viewModelScope.launch(Dispatchers.Default) {
                    val storageUri = MainPreferences.getStorageUri()
                    val pickedDirectory = DocumentFile.fromTreeUri(getApplication(), Uri.parse(storageUri))
                    it.postValue(
                            pickedDirectory?.findFile(".nomedia")?.exists()?.invert()
                                    ?: false.invert() || pickedDirectory?.name?.startsWith(".") ?: false.invert())
                }
            }
        }
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

    fun getLoadingStatusLiveData(): MutableLiveData<String> {
        return loadingStatus
    }

    fun getIsNomediaDirectoryLiveData(): MutableLiveData<Boolean> {
        return isNomediaDirectory
    }

    private fun loadWallpaperDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            val wallpaperList = wallpaperDao?.getWallpapers()
            (wallpaperList as ArrayList<Wallpaper>).getSortedList()

            for (i in wallpaperList.indices) {
                wallpaperList[i].isSelected = false
            }

            @Suppress("UNCHECKED_CAST")
            wallpapersData.postValue(wallpaperList.clone() as ArrayList<Wallpaper>)

            loadWallpaperImages()
        }
    }

    private fun loadWallpaperImages() {
        viewModelScope.launch(Dispatchers.IO) {
            isDatabaseLoaded = false
            val alreadyLoaded = WallpaperDatabase.getInstance(getApplication())?.wallpaperDao()?.getWallpapers()?.associateBy { it.uri }
            val storageUri = MainPreferences.getStorageUri()
            val pickedDirectory = DocumentFile.fromTreeUri(getApplication(), Uri.parse(storageUri))
            var count = 0
            val total = pickedDirectory?.listFiles()?.size ?: 0

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
                                wallpaper.size = file.length()

                                getApplication<Application>().contentResolver.openInputStream(file.uri)?.use { inputStream ->
                                    val options = BitmapFactory.Options()
                                    options.inJustDecodeBounds = true
                                    BitmapFactory.decodeStream(inputStream, null, options)
                                    wallpaper.width = options.outWidth
                                    wallpaper.height = options.outHeight
                                }

                                // Log.d(TAG, "loadWallpaperImages: ${wallpaper.name}, ${wallpaper.width}, ${wallpaper.height}")
                            } else {
                                // Log.d(TAG, "loadWallpaperImages: ${file.name} already loaded")
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
                        wallpaper.size = it.length()

                        getApplication<Application>().contentResolver.openInputStream(it.uri)?.use { inputStream ->
                            val options = BitmapFactory.Options()
                            options.inJustDecodeBounds = true
                            BitmapFactory.decodeStream(inputStream, null, options)
                            wallpaper.width = options.outWidth
                            wallpaper.height = options.outHeight
                        }

                        // Log.d(TAG, "loadWallpaperImages: ${wallpaper.name}, ${wallpaper.width}, ${wallpaper.height}")
                    } else {
                        // Log.d(TAG, "loadWallpaperImages: ${it.name} already loaded")
                        alreadyLoadedWallpaper = true
                    }
                }

                if (wallpaper.isNull().invert()) {
                    if (alreadyLoadedWallpaper.invert()) {
                        count++
                        wallpapers.add(wallpaper)
                        if (alreadyLoaded?.isNotEmpty() == true) {
                            newWallpapersData.postValue(wallpaper)
                        }
                        loadingStatus.postValue("$count : ${(count / total.toFloat() * 100).toInt()}%")
                    }
                }
            }

            wallpapers.sortByDescending { it.dateModified }

            if (alreadyLoaded?.isEmpty() == true) {
                wallpapers.getSortedList()

                for (i in 0 until wallpapers.size) {
                    if (wallpapers[i].isSelected) {
                        Log.d(TAG, "loadWallpaperImages: ${wallpapers[i].isSelected}")
                    }
                    wallpapers[i].isSelected = false
                }

                wallpapersData.postValue(wallpapers)
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
                        // Log.d(TAG, "initDatabase: ${it.name} exists")
                    }
                } catch (e: Exception) {
                    // Log.d(TAG, "initDatabase: ${it.name} doesn't exist")
                    wallpaperDao.delete(it)
                    removedWallpapersData.postValue(it)
                }
            }

            try {
                wallpapers.forEach {
                    wallpaperDao?.insert(it)
                }
            } catch (e: ConcurrentModificationException) {
                Log.e(TAG, "initDatabase: ConcurrentModificationException occurred while inserting wallpapers into database - ${e.message}")
            }

            isDatabaseLoaded = true
        }
    }

    private fun ArrayList<Wallpaper>.toHashMap(): HashMap<Long, Wallpaper> {
        val hashMap = HashMap<Long, Wallpaper>()
        this.forEach {
            hashMap[it.dateModified] = it
        }
        return hashMap
    }

    fun sortWallpapers() {
        if (isDatabaseLoaded) {
            wallpapers = wallpapersData.value ?: ArrayList()
            wallpapers.getSortedList()
            wallpapersData.postValue(wallpapers)
        }
    }

    fun isDatabaseLoaded(): Boolean {
        return isDatabaseLoaded
    }

    fun updateWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.update(wallpaper)
        }
    }

    fun removeWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.delete(wallpaper)
        }
    }

    fun recreateDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            isDatabaseLoaded = false
            wallpapers.clear()
            wallpapersData.postValue(wallpapers)
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.nukeTable()

            withContext(Dispatchers.Main) {
                loadWallpaperImages()
            }
        }
    }

    fun refreshWallpapers(func: () -> Unit) {
        if (isDatabaseLoaded) {
            loadWallpaperImages()
            Log.d(TAG, "refreshWallpapers: refreshing wallpapers")
        } else {
            Log.d(TAG, "refreshWallpapers: database not loaded")
            func()
        }
    }

    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(broadcastReceiver!!)
    }

    companion object {
        private const val TAG = "WallpaperViewModel"
    }
}