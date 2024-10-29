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
import app.simple.peri.utils.FileUtils.filterDotFiles
import app.simple.peri.utils.FileUtils.listCompleteFiles
import app.simple.peri.utils.FileUtils.listOnlyFirstLevelFiles
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.utils.PermissionUtils
import app.simple.peri.utils.ProcessUtils.cancelAll
import app.simple.peri.utils.WallpaperSort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File

class ComposeWallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private var loadWallpaperImagesJobs: MutableSet<Job> = mutableSetOf()
    private var broadcastReceiver: BroadcastReceiver? = null
    private val intentFilter = IntentFilter().apply {
        addAction(BundleConstants.INTENT_RECREATE_DATABASE)
    }

    private var wallpapers: ArrayList<Wallpaper> = ArrayList()
    private var alreadyLoaded: Map<String, Wallpaper>? = null

    private var isDatabaseLoaded: MutableLiveData<Boolean> = MutableLiveData(false)

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

    private val wallpapersData: MutableLiveData<ArrayList<Wallpaper>> by lazy {
        MutableLiveData<ArrayList<Wallpaper>>().also {
            loadWallpaperDatabase()
        }
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

    fun getWallpapersLiveData(): MutableLiveData<ArrayList<Wallpaper>> {
        return wallpapersData
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

                    folders.add(folder)
                }
            }

            foldersData.postValue(folders)
        }
    }

    private fun loadWallpaperDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.sanitizeEntries()
            wallpaperDao?.purgeNonExistingWallpapers()
            val wallpaperList = wallpaperDao?.getWallpapers()
            (wallpaperList as ArrayList<Wallpaper>).getSortedList()

            for (i in wallpaperList.indices) {
                wallpaperList[i].isSelected = false
            }

            @Suppress("UNCHECKED_CAST")
            wallpapersData.postValue(wallpaperList.clone() as ArrayList<Wallpaper>)
        }

        loadWallpaperImages()
    }

    private fun loadWallpaperImages() {
        Log.i(TAG, "loadWallpaperImages: starting to load wallpaper images")
        val semaphore = Semaphore(5) // Limit to 5 concurrent tasks
        val wallpaperImageJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                alreadyLoaded = WallpaperDatabase.getInstance(getApplication())
                    ?.wallpaperDao()?.getWallpapers()?.associateBy { it.filePath }
                isDatabaseLoaded.postValue(false)

                MainComposePreferences.getAllWallpaperPaths().forEach { path ->
                    val pickedDirectory = File(path)
                    ensureActive()
                    if (pickedDirectory.exists()) {
                        val files = pickedDirectory.getFiles()
                        Log.d(TAG, "loadWallpaperImages: files count: ${files.size}")

                        files.map { file ->
                            async {
                                semaphore.withPermit {
                                    ensureActive()
                                    try {
                                        if (alreadyLoaded?.containsKey(file.absolutePath.toString()) == false) {
                                            Log.i(TAG, "loadWallpaperImages: loading ${file.absolutePath}")
                                            val wallpaper = Wallpaper.createFromFile(file)
                                            wallpaper.folderID = path.hashCode()
                                            wallpapers.add(wallpaper)
                                            WallpaperDatabase.getInstance(getApplication())
                                                ?.wallpaperDao()?.insert(wallpaper)
                                            loadFolders()
                                        } else {
                                            Log.i(TAG, "loadWallpaperImages: already loaded ${file.absolutePath}")
                                        }
                                    } catch (e: IllegalStateException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }.awaitAll()
                    } else {
                        Log.e(TAG, "loadWallpaperImages: directory does not exist: $path")
                    }
                }

                initDatabase()
                loadFolders()
            }.getOrElse {
                it.printStackTrace()
            }
        }

        loadWallpaperImagesJobs.add(wallpaperImageJob)
    }

    private fun initDatabase() {
        val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
        val wallpaperDao = wallpaperDatabase?.wallpaperDao()

        wallpaperDao?.getWallpapers()?.forEach {
            try {
                if (File(it.filePath).exists().not()) {
                    wallpaperDao.delete(it)
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
            updatedWallpaper.folderID = wallpaper.folderID
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

    fun refresh() {
        Log.i(TAG, "refresh: refreshing wallpapers")
        loadWallpaperImagesJobs.cancelAll("refreshing wallpapers")
        loadFolders()
        loadWallpaperImages()
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch(Dispatchers.IO) {
            loadWallpaperImagesJobs.cancelAll("deleting folder")
            MainComposePreferences.removeWallpaperPath(folder.path)
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.deleteByPathHashcode(folder.hashcode)
            loadFolders()
            loadWallpaperDatabase()
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
