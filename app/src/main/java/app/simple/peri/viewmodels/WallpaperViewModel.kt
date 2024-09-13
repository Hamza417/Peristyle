package app.simple.peri.viewmodels

import android.app.Application
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.BitmapUtils.generatePalette
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.FileUtils.filterDotFiles
import app.simple.peri.utils.FileUtils.generateMD5
import app.simple.peri.utils.FileUtils.listCompleteFiles
import app.simple.peri.utils.FileUtils.listOnlyFirstLevelFiles
import app.simple.peri.utils.FileUtils.toUri
import app.simple.peri.utils.PermissionUtils
import app.simple.peri.utils.WallpaperSort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

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

    private val systemWallpaperData: MutableLiveData<ArrayList<Wallpaper>> by lazy {
        MutableLiveData<ArrayList<Wallpaper>>().also {
            postCurrentSystemWallpaper()
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

    fun getSystemWallpaper(): MutableLiveData<ArrayList<Wallpaper>> {
        return systemWallpaperData
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

            val uri = MainPreferences.getStorageUri()?.toUri() ?: return@launch
            val pickedDirectory = DocumentFile.fromTreeUri(getApplication(), uri)
            val files = pickedDirectory?.getFiles()?.dotFilter().orEmpty()
            val total = files.size

            if (files.isEmpty()) {
                loadingStatus.postValue("no files found")
                return@launch
            }

            if (wallpapersData.value.isNullOrEmpty()) {
                loadingStatus.postValue(getApplication<Application>().getString(app.simple.peri.R.string.preparing))
            }

            files.parallelStream().forEach { file ->
                try {
                    val wallpaper = createWallpaperFromFile(file)
                    if (wallpaper != null && alreadyLoaded?.containsKey(file.uri.toString()) == false) {
                        wallpapers.add(wallpaper)
                        newWallpapersData.postValue(wallpaper)
                        WallpaperDatabase.getInstance(getApplication())?.wallpaperDao()?.insert(wallpaper)
                        updateLoadingStatus(wallpapers.size, total)
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

            initDatabase()
            loadingStatus.postValue("") // clear loading status
            isLoading = false
        }
    }

    private fun createWallpaperFromFile(file: DocumentFile): Wallpaper? {
        val wallpaper = Wallpaper().apply {
            name = file.name
            uri = file.uri.toString()
            dateModified = file.lastModified()
            size = file.length()
        }

        getApplication<Application>().contentResolver.openInputStream(file.uri)?.use { inputStream ->
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = false }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            wallpaper.width = options.outWidth
            wallpaper.height = options.outHeight
            wallpaper.prominentColor = bitmap?.generatePalette()?.vibrantSwatch?.rgb ?: 0
            bitmap?.recycle()
        }

        getApplication<Application>().contentResolver.openInputStream(file.uri)?.use { inputStream ->
            wallpaper.md5 = inputStream.generateMD5()
            Log.i(TAG, "loadWallpaperImages: ${wallpaper.name} - ${wallpaper.md5}")
        }

        return if (wallpaper.isNull()) null else wallpaper
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
                getApplication<Application>().contentResolver.openInputStream(Uri.parse(it.uri))?.use { _ ->
                    Log.d(TAG, "initDatabase: ${it.name} exists")
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
                Wallpaper().createFromUri(systemUri.toString(), getApplication()),
                Wallpaper().createFromUri(lockUri.toString(), getApplication())
        )
    }

    private fun createTempFile(fileName: String): File {
        val file = File(getApplication<Application>().filesDir,
                        fileName.replace("$", System.currentTimeMillis().div(1000).toString()))
        if (file.exists()) file.delete()
        return file
    }

    private fun getFileUri(file: File): Uri {
        return FileProvider.getUriForFile(
                getApplication(), "${getApplication<Application>().packageName}.provider", file
        )
    }

    fun postCurrentSystemWallpaper() {
        viewModelScope.launch(Dispatchers.IO) {
            if (PermissionUtils.checkStoragePermission(getApplication())) {
                systemWallpaperData.postValue(getCurrentSystemWallpaper())
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
        private const val SYSTEM_WALLPAPER = "system_wallpaper_$.png"
        private const val LOCK_WALLPAPER = "lock_wallpaper_$.png"
    }
}
