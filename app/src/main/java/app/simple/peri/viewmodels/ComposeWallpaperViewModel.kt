package app.simple.peri.viewmodels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
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
import app.simple.peri.utils.ProcessUtils.cancelAll
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

    private val foldersData: MutableLiveData<ArrayList<Folder>> by lazy {
        MutableLiveData<ArrayList<Folder>>().also {
            refreshFolders()
        }
    }

    fun getFoldersLiveData(): MutableLiveData<ArrayList<Folder>> {
        return foldersData
    }

    private fun refreshFolders() {
        viewModelScope.launch(Dispatchers.IO) {
            val folders = ArrayList<Folder>()
            WallpaperDatabase.getInstance(getApplication())?.let {
                MainComposePreferences.getAllWallpaperPaths().forEach { path ->
                    val pickedDirectory = File(path)
                    if (pickedDirectory.exists()) {
                        val folder = Folder().apply {
                            name = pickedDirectory.name
                            this.path = path
                            count = it.wallpaperDao().getWallpapersCountByPathHashcode(path.hashCode())
                            hashcode = path.hashCode()
                            isNomedia = pickedDirectory.listFiles()?.any { it.name == ".nomedia" } == true
                        }

                        folders.add(folder)
                    }
                }

                foldersData.postValue(folders)
                it.wallpaperDao().purgeNonExistingWallpapers(it)
            }
        }
    }

    private fun loadWallpaperImages() {
        Log.i(TAG, "loadWallpaperImages: starting to load wallpaper images")
        val semaphore = Semaphore(5) // Limit to 5 concurrent tasks
        val wallpaperImageJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                alreadyLoaded = WallpaperDatabase.getInstance(getApplication())
                    ?.wallpaperDao()?.getWallpapers()?.associateBy { it.filePath }

                MainComposePreferences.getAllWallpaperPaths().forEach { folderPath ->
                    val pickedDirectory = File(folderPath)
                    ensureActive()
                    if (pickedDirectory.exists()) {
                        val files = pickedDirectory.getFiles().dotFilter()
                        Log.d(TAG, "loadWallpaperImages: files count: ${files.size}")
                        var count = 0
                        files.map { file ->
                            async {
                                semaphore.withPermit {
                                    count++
                                    ensureActive()

                                    try {
                                        if (alreadyLoaded?.containsKey(file.absolutePath.toString()) == false) {
                                            Log.i(TAG, "loadWallpaperImages: loading ${file.absolutePath} count: $count")
                                            val wallpaper = Wallpaper.createFromFile(file)
                                            wallpaper.folderID = folderPath.hashCode()
                                            wallpapers.add(wallpaper)
                                            WallpaperDatabase.getInstance(getApplication())
                                                ?.wallpaperDao()?.insert(wallpaper)
                                            refreshFolders()
                                        } else {
                                            Log.i(TAG, "loadWallpaperImages: already loaded ${file.absolutePath}")
                                        }
                                    } catch (e: IllegalStateException) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }.awaitAll()

                        Log.i(TAG, "loadWallpaperImages: total files processed: ${wallpapers.size}")
                    } else {
                        Log.e(TAG, "loadWallpaperImages: directory does not exist: $folderPath")
                    }
                }

                refreshFolders() // Refresh folders after loading all wallpapers just to avoid any inconsistency
            }.getOrElse { throwable ->
                throwable.printStackTrace()
                WallpaperDatabase.getInstance(getApplication())?.let {
                    it.wallpaperDao().purgeNonExistingWallpapers(it)
                }
            }
        }

        loadWallpaperImagesJobs.add(wallpaperImageJob)
    }

    fun removeWallpaper(wallpaper: Wallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.delete(wallpaper)
            wallpapers.remove(wallpaper)
        }
    }

    fun recreateDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "recreateDatabase: recreating database")
            loadWallpaperImagesJobs.cancelAll("recreating database")
            wallpapers.clear()
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.nukeTable()

            withContext(Dispatchers.Main) {
                refresh()
            }
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
        loadWallpaperImagesJobs.cancelAll("refreshing wallpapers")
        refreshFolders()
        loadWallpaperImages()
    }

    fun deleteFolder(folder: Folder) {
        viewModelScope.launch(Dispatchers.IO) {
            loadWallpaperImagesJobs.cancelAll("deleting folder")
            MainComposePreferences.removeWallpaperPath(folder.path)
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.deleteByPathHashcode(folder.path.hashCode())
            refresh()
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
                        refreshFolders()
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
                    refreshFolders()
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
