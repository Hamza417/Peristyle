package app.simple.peri.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Folder
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.FileUtils.filterDotFiles
import app.simple.peri.utils.FileUtils.listCompleteFiles
import app.simple.peri.utils.FileUtils.listOnlyFirstLevelFiles
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.utils.FileUtils.toFileUri
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
import java.io.IOException

class ComposeWallpaperViewModel(application: Application) : AndroidViewModel(application) {

    private var loadWallpaperImagesJobs: MutableSet<Job> = mutableSetOf()
    private var alreadyLoaded: Map<String, Wallpaper>? = null
    private val wallpaperDatabase: WallpaperDatabase by lazy {
        WallpaperDatabase.getInstance(getApplication())!!
    }

    private val foldersData: MutableLiveData<ArrayList<Folder>> by lazy {
        MutableLiveData<ArrayList<Folder>>().also {
            refreshFolders()
        }
    }

    fun getFoldersLiveData(): MutableLiveData<ArrayList<Folder>> {
        return foldersData
    }

    private fun refreshFolders(shouldPurge: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            val folders = ArrayList<Folder>()
            MainComposePreferences.getAllowedPaths().forEach { path ->
                val pickedDirectory = File(path)
                if (pickedDirectory.exists()) {
                    val folder = Folder().apply {
                        name = pickedDirectory.name
                        this.path = path
                        count = wallpaperDatabase.wallpaperDao().getWallpapersCountByPathHashcode(path.hashCode())
                        hashcode = path.hashCode()
                        isNomedia = pickedDirectory.listFiles()?.any { it.name == ".nomedia" } == true
                    }
                    if (folder.count > 0) {
                        folders.add(folder)
                    }
                }
            }
            foldersData.postValue(folders)
            if (shouldPurge) {
                wallpaperDatabase.wallpaperDao().purgeNonExistingWallpapers(wallpaperDatabase)
            }
        }
    }

    private fun loadWallpaperImages() {
        Log.i(TAG, "loadWallpaperImages: starting to load wallpaper images")
        val semaphore = Semaphore(MainComposePreferences.getSemaphoreCount()) // Limit the number of concurrent jobs
        val wallpaperImageJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                alreadyLoaded = wallpaperDatabase?.wallpaperDao()?.getWallpapers()?.associateBy { it.filePath }

                MainComposePreferences.getAllowedPaths().forEach { folderPath ->
                    val pickedDirectory = File(folderPath)
                    ensureActive()
                    if (pickedDirectory.exists()) {
                        val files = pickedDirectory.getFiles().dotFilter()

                        files.map { file ->
                            ensureActive()
                            async {
                                ensureActive()
                                semaphore.withPermit {
                                    ensureActive()

                                    try {
                                        if (alreadyLoaded?.containsKey(file.absolutePath.toString()) == false) {
                                            if (file.exists()) {
                                                val wallpaper = Wallpaper.createFromFile(file)
                                                wallpaper.folderID = folderPath.hashCode()
                                                // Make sure legacy interface stays compatible
                                                wallpaper.uri = file.toFileUri(getApplication()).toString()
                                                ensureActive()
                                                WallpaperDatabase.getInstance(getApplication())
                                                    ?.wallpaperDao()?.insert(wallpaper)
                                                refreshFolders(shouldPurge = false)
                                            }
                                        } else {
                                            Log.i(TAG, "loadWallpaperImages: already loaded ${file.absolutePath}")
                                        }
                                    } catch (e: IllegalStateException) {
                                        Log.e(TAG, "Error loading wallpaper from file: ${file.absolutePath}", e)
                                    } catch (e: IOException) {
                                        Log.e(TAG, e.message ?: "IOException occurred", e)
                                    }
                                }
                            }
                        }.awaitAll()

                        Log.i(TAG, "loadWallpaperImages: finished loading wallpapers from $folderPath")
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
            refreshFolders()
        }
    }

    fun recreateDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d(TAG, "recreateDatabase: recreating database")
            loadWallpaperImagesJobs.cancelAll("recreating database")
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.nukeTable()

            withContext(Dispatchers.Main) {
                refresh()
            }
        }
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

    fun revokeFolder(folder: Folder) {
        viewModelScope.launch(Dispatchers.IO) {
            loadWallpaperImagesJobs.cancelAll("deleting folder")
            MainComposePreferences.removeWallpaperPath(folder.path)
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.removeByPathHashcode(folder.path.hashCode())
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

    fun moveWallpapers(wallpapers: List<Wallpaper>, toPath: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                wallpapers.forEach { wallpaper ->
                    val originalFile = wallpaper.filePath.toFile()
                    val dest = File(toPath, originalFile.name)

                    originalFile.copyTo(dest, overwrite = true)
                    originalFile.delete()

                    WallpaperDatabase.getInstance(getApplication())?.wallpaperDao()?.delete(wallpaper)
                }

                refresh()

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            }.getOrElse {
                Log.e(TAG, "Error moving wallpapers", it)
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

    override fun onCleared() {
        super.onCleared()
        WallpaperDatabase.destroyInstance()
    }

    companion object {
        private const val TAG = "ComposeWallpaperViewModel"
    }
}
