package app.simple.peri.viewmodels

import android.app.Application
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.peri.constants.BundleConstants
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Folder
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.CommonUtils.withBooleanScope
import app.simple.peri.utils.FileUtils.filterDotFiles
import app.simple.peri.utils.FileUtils.listCompleteFiles
import app.simple.peri.utils.FileUtils.listOnlyFirstLevelFiles
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.utils.PermissionUtils
import app.simple.peri.utils.WallpaperSort.getSortedList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ComposeWallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private var loadDatabaseJob: Job? = null
    private var isWallpaperLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var shouldThrowError: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private var broadcastReceiver: BroadcastReceiver? = null
    private val intentFilter = IntentFilter().apply {
        addAction(BundleConstants.INTENT_RECREATE_DATABASE)
    }

    private var wallpapers: ArrayList<Wallpaper> = ArrayList()
    private var alreadyLoaded: Map<String, Wallpaper>? = null

    private var isDatabaseLoaded: MutableLiveData<Boolean> = MutableLiveData(false)

    private var _folderLoadingState = MutableStateFlow("")

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

        LocalBroadcastManager.getInstance(application)
            .registerReceiver(broadcastReceiver!!, intentFilter)
    }

    fun getLoadingImage(): MutableStateFlow<String> {
        return _folderLoadingState
    }

    private fun setFolderLoadingState(loadingImage: String) {
        _folderLoadingState.value = loadingImage
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

    private val systemWallpaperData: MutableLiveData<ArrayList<Wallpaper>> by lazy {
        MutableLiveData<ArrayList<Wallpaper>>().also {
            postCurrentSystemWallpaper()
        }
    }

    private val foldersData: MutableLiveData<ArrayList<Folder>> by lazy {
        MutableLiveData<ArrayList<Folder>>().also {
            loadFolders()
        }
    }

    private val loadingStatus: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
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

    fun getDatabaseLoaded(): MutableLiveData<Boolean> {
        return isDatabaseLoaded
    }

    fun getSystemWallpaper(): MutableLiveData<ArrayList<Wallpaper>> {
        return systemWallpaperData
    }

    fun getFoldersLiveData(): MutableLiveData<ArrayList<Folder>> {
        return foldersData
    }

    private fun loadFolders() {
        viewModelScope.launch(Dispatchers.IO) {
            val folders = ArrayList<Folder>()
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            MainComposePreferences.getAllWallpaperPaths().forEach { path ->
                val pickedDirectory = File(path)
                if (pickedDirectory.exists()) {
                    val folder = Folder().apply {
                        name = pickedDirectory.name
                        this.path = path
                        count = wallpaperDao?.getWallpapersCountByPathHashcode(path.hashCode()) ?: 0
                        hashcode = path.hashCode()
                        isNomedia = pickedDirectory.listFiles()?.any { it.name == ".nomedia" } == true
                    }

                    if (folder.count > 0) {
                        folders.add(folder)
                    } else {
                        Log.i(TAG, "loadFolders: folder is empty: ${folder.name}")
                    }
                }
            }

            foldersData.postValue(folders)
        }
    }

    private fun loadWallpaperDatabase() {
        loadDatabaseJob = viewModelScope.launch(Dispatchers.IO) {
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

            loadWallpaperImages()
        }
    }

    private fun loadWallpaperImages() {
        if (isWallpaperLoading.value) {
            Log.d(TAG, "loadWallpaperImages: previous session is still loading, skipping...")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            withBooleanScope(isWallpaperLoading) {
                runCatching {
                    alreadyLoaded = WallpaperDatabase.getInstance(getApplication())
                        ?.wallpaperDao()?.getWallpapers()?.associateBy { it.filePath }
                    isDatabaseLoaded.postValue(false)

                    MainComposePreferences.getAllWallpaperPaths().forEach { path ->
                        val pickedDirectory = File(path)
                        if (pickedDirectory.exists()) {
                            val files = pickedDirectory.getFiles()
                            Log.d(TAG, "loadWallpaperImages: files count: ${files.size}")
                            val total = files.size
                            var count = 0
                            setFolderLoadingState(getApplication<Application>().getString(app.simple.peri.R.string.preparing))

                            if (wallpapersData.value.isNullOrEmpty()) {
                                loadingStatus.postValue(getApplication<Application>().getString(app.simple.peri.R.string.preparing))
                            }

                            files.forEach { file ->
                                count = count.inc()
                                if (shouldThrowError.value) {
                                    Log.e(TAG, "loadWallpaperImages: job cancelled")
                                    shouldThrowError.value = false
                                    throw CancellationException("Job cancelled")
                                }

                                try {
                                    if (alreadyLoaded?.containsKey(file.absolutePath.toString()) == false) {
                                        Log.i(TAG, "loadWallpaperImages: loading ${file.absolutePath}")
                                        val wallpaper = Wallpaper.createFromFile(file)
                                        wallpaper.folderUriHashcode = path.hashCode()
                                        wallpapers.add(wallpaper)
                                        newWallpapersData.postValue(wallpaper)
                                        WallpaperDatabase.getInstance(getApplication())
                                            ?.wallpaperDao()?.insert(wallpaper)
                                        updateLoadingStatus(count, total)
                                        setFolderLoadingState("$count / $total")
                                    } else {
                                        Log.i(TAG, "loadWallpaperImages: already loaded ${file.absolutePath}")
                                        updateLoadingStatus(count, total)
                                        setFolderLoadingState("$count / $total")
                                    }
                                } catch (e: IllegalStateException) {
                                    e.printStackTrace()
                                }
                            }

                            if (alreadyLoaded.isNullOrEmpty()) {
                                wallpapers.getSortedList()
                                wallpapers.forEach { it.isSelected = false }
                                wallpapersData.postValue(wallpapers)
                            }
                        } else {
                            Log.e(TAG, "loadWallpaperImages: directory does not exist: $path")
                        }
                    }

                    initDatabase()
                    loadingStatus.postValue("")
                    setFolderLoadingState("")
                    loadFolders()
                }.getOrElse {
                    it.printStackTrace()
                }
            }
        }
    }

    private fun updateLoadingStatus(count: Int, total: Int) {
        val progress = (count / total.toFloat() * 100).toInt()
        loadingStatus.postValue("$count : $progress%")
    }

    private fun initDatabase() {
        val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
        val wallpaperDao = wallpaperDatabase?.wallpaperDao()

        wallpaperDao?.getWallpapers()?.forEach {
            try {
                if (File(it.filePath).exists().not()) {
                    wallpaperDao.delete(it)
                    removedWallpapersData.postValue(it)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        try {
            wallpapers.forEach {
                wallpaperDao?.insert(it)
            }
        } catch (e: ConcurrentModificationException) {
            // rarely occurs but occurs :)))))))))))
        }

        isDatabaseLoaded.postValue(true)
        Log.d(TAG, "database loaded")
        wallpaperDao?.sanitizeEntries()
    }

    private fun getCurrentSystemWallpaper(): ArrayList<Wallpaper> {
        val wallpaperManager = WallpaperManager.getInstance(getApplication())
        val systemBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            wallpaperManager.getDrawable(WallpaperManager.FLAG_SYSTEM)?.toBitmap()
        } else {
            wallpaperManager.drawable?.toBitmap()
        }

        val lockBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            wallpaperManager.getDrawable(WallpaperManager.FLAG_LOCK)?.toBitmap()
        } else {
            wallpaperManager.drawable?.toBitmap()
        }

        val systemFile = createTempFile(SYSTEM_WALLPAPER)
        val lockFile = createTempFile(LOCK_WALLPAPER)

        systemFile.outputStream().use { systemBitmap?.compress(Bitmap.CompressFormat.PNG, 100, it) }
        lockFile.outputStream().use { lockBitmap?.compress(Bitmap.CompressFormat.PNG, 100, it) }

        val systemUri = getFileUri(systemFile)
        val lockUri = getFileUri(lockFile)

        return arrayListOf(
                Wallpaper.createFromUri(systemUri.toString(), getApplication()),
                Wallpaper.createFromUri(lockUri.toString(), getApplication())
        )
    }

    private fun createTempFile(fileName: String): File {
        val file = File(
                getApplication<Application>().filesDir,
                fileName.replace("$", System.currentTimeMillis().div(1000).toString())
        )
        if (file.exists()) file.delete()
        return file
    }

    private fun getFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
                getApplication(), "${getApplication<Application>().packageName}.provider", file
        )
    }

    private fun postCurrentSystemWallpaper() {
        viewModelScope.launch(Dispatchers.IO) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    when {
                        PermissionUtils.checkStoragePermission(getApplication())
                                && PermissionUtils.checkMediaImagesPermission(getApplication()) -> {
                            systemWallpaperData.postValue(getCurrentSystemWallpaper())
                        }
                    }
                }

                else -> {
                    when {
                        PermissionUtils.checkStoragePermission(getApplication()) -> {
                            systemWallpaperData.postValue(getCurrentSystemWallpaper())
                        }
                    }
                }
            }
        }
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
            wallpapers.remove(wallpaper)
            wallpapersData.postValue(wallpapers)
        }
    }

    fun recreateDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "recreateDatabase: recreating database")

            isDatabaseLoaded.postValue(false)
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
            val updatedWallpaper = Wallpaper.createFromFile(wallpaper.filePath.toFile())
            updatedWallpaper.folderUriHashcode = wallpaper.folderUriHashcode
            wallpaperDatabase?.wallpaperDao()?.update(updatedWallpaper)

            withContext(Dispatchers.Main) {
                func(wallpaper)
            }
        }
    }

    private fun File.getFiles(): List<File> {
        return if (MainPreferences.isTweakOptionSelected(MainPreferences.IGNORE_SUB_DIRS)) {
            Log.i(TAG, "getFiles: listing only first level files")
            listOnlyFirstLevelFiles()
        } else {
            Log.i(TAG, "getFiles: listing all files")
            listCompleteFiles()
        }
    }

    private fun List<File>.dotFilter(): List<File> {
        return if (MainPreferences.isTweakOptionSelected(MainPreferences.IGNORE_DOT_FILES)) {
            filterDotFiles()
        } else {
            this
        }
    }

    fun refresh(isForced: Boolean = false) {
        Log.i(TAG, "refresh: refreshing wallpapers")

        if (isForced) {
            isWallpaperLoading.value = false
            shouldThrowError.value = true
        }

        loadFolders()
        loadWallpaperImages()
    }

    fun deleteFolder(it: Folder) {
        viewModelScope.launch(Dispatchers.IO) {
            MainComposePreferences.removeWallpaperPath(it.path)
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.deleteByUriHashcode(it.hashcode)
            loadWallpaperDatabase()
            loadFolders()
        }
    }

    fun addNoMediaFile(folder: Folder, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val pickedDirectory = File(folder.path)

            if (pickedDirectory.exists()) {
                if (pickedDirectory.listFiles()?.any { it.name == ".nomedia" } == false) {
                    pickedDirectory.resolve(".nomedia").createNewFile()
                    withContext(Dispatchers.Main) {
                        onSuccess()
                        loadFolders()
                    }
                }
            }
        }
    }

    fun removeNoMediaFile(folder: Folder, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val pickedDirectory = File(folder.path)

            if (pickedDirectory.exists()) {
                pickedDirectory.listFiles()?.forEach {
                    if (it.name == ".nomedia") {
                        it.delete()
                    }
                }

                withContext(Dispatchers.Main) {
                    onSuccess()
                    loadFolders()
                }
            }
        }
    }

    companion object {
        private const val TAG = "ComposeWallpaperViewModel"
        private const val SYSTEM_WALLPAPER = "system_wallpaper_$.png"
        private const val LOCK_WALLPAPER = "lock_wallpaper_$.png"
    }
}
