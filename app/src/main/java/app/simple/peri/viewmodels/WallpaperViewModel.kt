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
import app.simple.peri.utils.FileUtils.filterDotFiles
import app.simple.peri.utils.FileUtils.listCompleteFiles
import app.simple.peri.utils.FileUtils.listOnlyFirstLevelFiles
import app.simple.peri.utils.FileUtils.toUri
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
    private val failedURIs: ArrayList<String> = ArrayList()
    private var alreadyLoaded: Map<String, Wallpaper>? = null

    private var isDatabaseLoaded: MutableLiveData<Boolean> = MutableLiveData(false)

    private var isLoading = false

    init {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BundleConstants.INTENT_RECREATE_DATABASE) {
                    Log.d(TAG, "onReceive: recreating database")
                    recreateDatabase()
                } else {
                    Log.d(TAG, "onReceive: unsupported action: ${intent?.action ?: "unknown"}")
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

    private val failedURIsData: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
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

    fun getFailedURIs(): MutableLiveData<ArrayList<String>> {
        return failedURIsData
    }

    fun getDatabaseLoaded(): MutableLiveData<Boolean> {
        return isDatabaseLoaded
    }

    private fun loadWallpaperDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.sanitizeEntries() // Sanitize the database
            val wallpaperList = wallpaperDao?.getWallpapers()
            (wallpaperList as ArrayList<Wallpaper>).getSortedList()

            for (i in wallpaperList.indices) {
                wallpaperList[i].isSelected = false
            }

            @Suppress("UNCHECKED_CAST")
            wallpapersData.postValue(wallpaperList.clone() as ArrayList<Wallpaper>)

            alreadyLoaded = WallpaperDatabase.getInstance(getApplication())?.wallpaperDao()?.getWallpapers()?.associateBy { it.uri }
            loadWallpaperImages()
        }
    }

    private fun loadWallpaperImages() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            isDatabaseLoaded.postValue(false)

            val uri = MainPreferences.getStorageUri()?.toUri()!!
            val pickedDirectory = DocumentFile.fromTreeUri(getApplication(), uri)
            loadingStatus.postValue(getApplication<Application>().getString(app.simple.peri.R.string.preparing))
            val files = pickedDirectory?.getFiles()?.dotFilter()
            var count = 0
            val total = files?.size ?: 0

            if (files.isNullOrEmpty()) {
                loadingStatus.postValue("no files found")
                return@launch
            }

            loadingStatus.postValue("0 : 0%")
            files.parallelStream().forEach { file ->
                try {
                    val wallpaper = Wallpaper()

                    if ((alreadyLoaded?.containsKey(file.uri.toString()) == true).invert()) {
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
                    }

                    if (wallpaper.isNull().invert()) {
                        if ((alreadyLoaded?.containsKey(file.uri.toString()) == true).invert()) {
                            count++
                            wallpapers.add(wallpaper)
                            if (alreadyLoaded?.isNotEmpty() == true) {
                                newWallpapersData.postValue(wallpaper)
                                WallpaperDatabase.getInstance(getApplication())?.wallpaperDao()?.insert(wallpaper)
                            }

                            loadingStatus.postValue("$count : ${(count / total.toFloat() * 100).toInt()}%")
                        }
                    }
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                    failedURIs.add(file.uri.toString())
                }
            }

            if (alreadyLoaded?.isEmpty() == true) {
                wallpapers.getSortedList()

                wallpapers.forEach {
                    it.isSelected = false
                }

                wallpapersData.postValue(wallpapers)
            }

            initDatabase()
            loadingStatus.postValue("Done")
            isLoading = false
        }
    }

    private fun initDatabase() {
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
            // rarely occurs
            Log.e(TAG, "initDatabase: ConcurrentModificationException occurred while inserting wallpapers into database - ${e.message}")
        }

        isDatabaseLoaded.postValue(true)
        Log.d(TAG, "initDatabase: database loaded")

        if (failedURIs.isNotEmpty()) {
            failedURIsData.postValue(failedURIs)
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
        if (isDatabaseLoaded.value == true) {
            wallpapers = wallpapersData.value ?: ArrayList()
            wallpapers.getSortedList()
            wallpapersData.postValue(wallpapers)
        }
    }

    fun isDatabaseLoaded(): Boolean {
        return isDatabaseLoaded.value ?: false
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
            Log.d(TAG, "recreateDatabase: recreating database")

            if (isLoading.invert()) {
                isDatabaseLoaded.postValue(false)
                wallpapers.clear()
                wallpapersData.postValue(wallpapers)
                val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
                val wallpaperDao = wallpaperDatabase?.wallpaperDao()
                wallpaperDao?.nukeTable()

                withContext(Dispatchers.Main) {
                    loadWallpaperImages()
                }
            } else {
                Log.d(TAG, "recreateDatabase: previous session is still loading, skipping...")
            }
        }
    }

    fun refreshWallpapers(func: () -> Unit) {
        if (isDatabaseLoaded.value == true) {
            Log.d(TAG, "refreshWallpapers: refreshing wallpapers")
            loadWallpaperImages()
        } else {
            Log.d(TAG, "refreshWallpapers: database not loaded")
            func()
        }
    }

    override fun onCleared() {
        super.onCleared()
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(broadcastReceiver!!)
    }

    fun reloadMetadata(wallpaper: Wallpaper, func: (Wallpaper) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val documentFile = DocumentFile.fromSingleUri(getApplication(), Uri.parse(wallpaper.uri))

            getApplication<Application>().contentResolver.openInputStream(Uri.parse(wallpaper.uri))?.use { inputStream ->
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
                wallpaper.width = options.outWidth
                wallpaper.height = options.outHeight
            }

            if (documentFile?.exists() == true) {
                wallpaper.size = documentFile.length()
                wallpaper.dateModified = documentFile.lastModified()
            }

            wallpaperDatabase?.wallpaperDao()?.update(wallpaper)

            withContext(Dispatchers.Main) {
                func(wallpaper)
            }
        }
    }

    private fun DocumentFile.getFiles(): List<DocumentFile> {
        return if (MainPreferences.isTweakOptionSelected(MainPreferences.IGNORE_SUB_DIRS)) {
            listCompleteFiles()
        } else {
            listOnlyFirstLevelFiles()
        }
    }

    private fun List<DocumentFile>.dotFilter(): List<DocumentFile> {
        return if (MainPreferences.isTweakOptionSelected(MainPreferences.IGNORE_DOT_FILES)) {
            filterDotFiles()
        } else {
            this
        }
    }

    companion object {
        private const val TAG = "WallpaperViewModel"
    }
}
