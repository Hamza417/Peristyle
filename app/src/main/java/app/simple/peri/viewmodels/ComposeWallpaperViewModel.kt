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
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.peri.constants.BundleConstants
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Folder
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.CommonUtils.withBooleanScope
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.FileUtils.filterDotFiles
import app.simple.peri.utils.FileUtils.listCompleteFiles
import app.simple.peri.utils.FileUtils.listOnlyFirstLevelFiles
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
    private val failedURIs: ArrayList<String> = ArrayList()
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

    private val failedURIsData: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }

    private val isNomediaDirectory: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>().also {
            if (MainPreferences.getShowNomediaDialog()) {
                viewModelScope.launch(Dispatchers.Default) {
                    val storageUri = MainPreferences.getStorageUri()
                    val pickedDirectory =
                        DocumentFile.fromTreeUri(getApplication(), Uri.parse(storageUri))
                    it.postValue(
                            pickedDirectory?.findFile(".nomedia")?.exists()?.invert()
                                    ?: false.invert() || pickedDirectory?.name?.startsWith(".") ?: false.invert()
                    )
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
            getApplication<Application>().contentResolver.persistedUriPermissions.forEach { uri ->
                val pickedDirectory = DocumentFile.fromTreeUri(getApplication(), uri.uri)
                if (pickedDirectory?.exists() == true) {
                    val folder = Folder().apply {
                        name = pickedDirectory.name
                        this.uri = uri.uri.toString()
                        count = wallpaperDao?.getWallpapersCountByUriHashcode(uri.uri.hashCode()) ?: 0
                        hashcode = uri.uri.hashCode()
                        isNomedia = false
                    }

                    folders.add(folder)
                }
            }

            foldersData.postValue(folders)

            /**
             * Finding .nomedia files can take a long time
             * So we post the folders first and then find the .nomedia files
             */
            folders.forEach { folder ->
                val pickedDirectory = DocumentFile.fromTreeUri(getApplication(), Uri.parse(folder.uri))
                if (pickedDirectory?.exists() == true) {
                    if (pickedDirectory.findFile(".nomedia")?.exists() == true) {
                        folder.isNomedia = true
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
                    alreadyLoaded = WallpaperDatabase.getInstance(
                            getApplication()
                    )?.wallpaperDao()?.getWallpapers()?.associateBy { it.uri }
                    isDatabaseLoaded.postValue(false)

                    getApplication<Application>().contentResolver.persistedUriPermissions.forEach { folder ->
                        val pickedDirectory = DocumentFile.fromTreeUri(getApplication(), folder.uri)
                        if (pickedDirectory?.exists() == true) {
                            val files = pickedDirectory.getFiles().dotFilter()
                            val total = files.size
                            var count = 0
                            setFolderLoadingState(getApplication<Application>().getString(app.simple.peri.R.string.preparing))

                            if (files.isEmpty()) {
                                loadingStatus.postValue("no files found")
                                return@launch
                            }

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
                                    if (alreadyLoaded?.containsKey(file.uri.toString()) == false) {
                                        Log.i(TAG, "loadWallpaperImages: loading ${file.uri}")
                                        val wallpaper = Wallpaper.createWallpaperFromFile(file, getApplication())
                                        wallpaper.folderUriHashcode = folder.uri.hashCode()
                                        wallpapers.add(wallpaper)
                                        newWallpapersData.postValue(wallpaper)
                                        WallpaperDatabase.getInstance(getApplication())
                                            ?.wallpaperDao()?.insert(wallpaper)
                                        updateLoadingStatus(count, total)
                                        setFolderLoadingState("$count / $total")
                                    } else {
                                        Log.i(
                                                TAG,
                                                "loadWallpaperImages: already loaded ${file.uri}"
                                        )
                                        updateLoadingStatus(count, total)
                                        setFolderLoadingState("$count / $total")
                                    }
                                } catch (e: IllegalStateException) {
                                    e.printStackTrace()
                                    failedURIs.add(file.uri.toString())
                                }
                            }

                            if (alreadyLoaded.isNullOrEmpty()) {
                                wallpapers.getSortedList()
                                wallpapers.forEach { it.isSelected = false }
                                wallpapersData.postValue(wallpapers)
                            }
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
                getApplication<Application>().contentResolver.openInputStream(Uri.parse(it.uri))
                    ?.use { _ ->
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
            // rarely occurs but occurs :)))))))))))
        }

        isDatabaseLoaded.postValue(true)
        Log.d(TAG, "database loaded")

        if (failedURIs.isNotEmpty()) {
            failedURIsData.postValue(failedURIs)
        }
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
            DocumentFile.fromSingleUri(getApplication(), Uri.parse(wallpaper.uri))?.delete()
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
            val updatedWallpaper = Wallpaper.createFromUri(wallpaper.uri, getApplication())
            updatedWallpaper.md5 = wallpaper.md5
            updatedWallpaper.folderUriHashcode = wallpaper.folderUriHashcode
            wallpaperDatabase?.wallpaperDao()?.update(updatedWallpaper)

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
            getApplication<Application>().contentResolver.releasePersistableUriPermission(
                    Uri.parse(it.uri),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.deleteByUriHashcode(it.hashcode)
            loadWallpaperDatabase()
            loadFolders()
        }
    }

    fun addNoMediaFile(folder: Folder, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val pickedDirectory = DocumentFile.fromTreeUri(getApplication(), Uri.parse(folder.uri))

            if (pickedDirectory?.exists() == true) {
                if (pickedDirectory.findFile(".nomedia") == null) {
                    pickedDirectory.createFile("*/*", ".nomedia")
                }

                if (pickedDirectory.findFile(".nomedia")?.exists() == true) {
                    isNomediaDirectory.postValue(false)
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
            val pickedDirectory =
                DocumentFile.fromTreeUri(getApplication(), Uri.parse(folder.uri))!!

            if (pickedDirectory.exists()) {
                pickedDirectory.findFile(".nomedia")?.delete()

                if (pickedDirectory.findFile(".nomedia") == null
                        || pickedDirectory.findFile(".nomedia")?.exists() == false
                ) {
                    isNomediaDirectory.postValue(true)
                    withContext(Dispatchers.Main) {
                        onSuccess()
                        loadFolders()
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "WallpaperViewModel"
        private const val SYSTEM_WALLPAPER = "system_wallpaper_$.png"
        private const val LOCK_WALLPAPER = "lock_wallpaper_$.png"
    }
}
