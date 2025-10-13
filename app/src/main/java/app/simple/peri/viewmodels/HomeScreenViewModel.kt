package app.simple.peri.viewmodels

import android.app.Application
import android.app.WallpaperManager
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.SharedPreferences.registerSharedPreferenceChangeListener
import app.simple.peri.preferences.SharedPreferences.unregisterSharedPreferenceChangeListener
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.utils.PermissionUtils
import app.simple.peri.utils.ServiceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class HomeScreenViewModel(application: Application) : AndroidViewModel(application), OnSharedPreferenceChangeListener {

    private var countDownJobs: ArrayList<Job> = ArrayList()
    private val countDownMutex = Mutex()

    val countDownFlow: MutableStateFlow<Long> = MutableStateFlow(RANDOM_WALLPAPER_DELAY)

    init {
        registerSharedPreferenceChangeListener()
    }

    private val systemWallpaperData: MutableLiveData<Wallpaper> by lazy {
        MutableLiveData<Wallpaper>().also {
            postCurrentSystemWallpaper()
        }
    }

    private val lockWallpaperData: MutableLiveData<Wallpaper> by lazy {
        MutableLiveData<Wallpaper>().also {
            postCurrentLockWallpaper()
        }
    }

    private val randomWallpaperData: MutableLiveData<Wallpaper> by lazy {
        MutableLiveData<Wallpaper>().also {
            if (ServiceUtils.isWallpaperServiceRunning(getApplication())) {
                postLastLiveWallpaper()
            }
        }
    }

    private val lastLiveWallpaper: MutableLiveData<Wallpaper> by lazy {
        MutableLiveData<Wallpaper>().also {
            postRandomWallpaper()
        }
    }

    fun getSystemWallpaper(): MutableLiveData<Wallpaper> {
        return systemWallpaperData
    }

    fun getLockWallpaper(): MutableLiveData<Wallpaper> {
        return lockWallpaperData
    }

    fun getRandomWallpaper(): MutableLiveData<Wallpaper> {
        return randomWallpaperData
    }

    fun getLastLiveWallpaper(): LiveData<Wallpaper> {
        return lastLiveWallpaper
    }

    init {
        fun post() {
            postCurrentSystemWallpaper()
            Log.i("HomeScreenViewModel", "Wallpaper colors changed")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            WallpaperManager.getInstance(application)
                .addOnColorsChangedListener({ _, _ -> post() }, Handler(Looper.getMainLooper()))
        }

        viewModelScope.launch(Dispatchers.IO) {
            clearLegacyResidualFiles()
        }
    }

    private fun startCountDownFlow() {
        val job = viewModelScope.launch {
            countDownMutex.withLock {
                ensureActive()
                val interval = 16L // Update 60 times per second
                while (countDownFlow.value > 0) {
                    ensureActive()
                    delay(interval)
                    countDownFlow.value -= interval
                }

                ensureActive()
                postRandomWallpaper()
            }
        }

        countDownJobs.add(job)
    }

    fun stopCountDownFlow() {
        countDownJobs.forEach { it.cancel() }
        countDownJobs.clear()
        Log.i("HomeScreenViewModel", "Countdown flow stopped")
    }

    fun resumeCountDownFlow() {
        stopCountDownFlow()
        startCountDownFlow()
        Log.i("HomeScreenViewModel", "Countdown flow resumed")
    }

    private fun postCurrentSystemWallpaper() {
        Log.i("HomeScreenViewModel", "Posting current system wallpaper")
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperManager = WallpaperManager.getInstance(getApplication())
            val systemBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                wallpaperManager.getDrawable(WallpaperManager.FLAG_SYSTEM)?.toBitmap()
            } else {
                wallpaperManager.drawable?.toBitmap()
            }

            val systemFile = createTempFile(SYSTEM_WALLPAPER.replace("$", System.currentTimeMillis().div(1000).toString()))

            systemFile.outputStream().use { systemBitmap?.compress(Bitmap.CompressFormat.PNG, 100, it) }

            if (PermissionUtils.checkStoragePermission(getApplication())) {
                try {
                    systemWallpaperData.postValue(Wallpaper.createFromFile(systemFile, getApplication()))
                } catch (_: IOException) {
                    // bad system wallpaper??
                }
            }
        }
    }

    private fun postCurrentLockWallpaper() {
        Log.i("HomeScreenViewModel", "Posting current lock wallpaper")
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperManager = WallpaperManager.getInstance(getApplication())
            val lockBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                wallpaperManager.getDrawable(WallpaperManager.FLAG_LOCK)?.toBitmap()
            } else {
                wallpaperManager.drawable?.toBitmap()
            }

            val lockFile = createTempFile(LOCK_WALLPAPER.replace("$", System.currentTimeMillis().div(1000).toString()))

            lockFile.outputStream().use { lockBitmap?.compress(Bitmap.CompressFormat.PNG, 100, it) }

            if (PermissionUtils.checkStoragePermission(getApplication())) {
                try {
                    lockWallpaperData.postValue(Wallpaper.createFromFile(lockFile, getApplication()))
                } catch (_: IOException) {
                    // bad lock wallpaper??
                }
            }
        }
    }

    private fun postRandomWallpaper() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                randomWallpaperData.postValue(getRandomWallpaperFromDatabase())
            } catch (_: NoSuchElementException) {
            }
        }

        countDownFlow.value = RANDOM_WALLPAPER_DELAY
        startCountDownFlow()
    }

    private fun postLastLiveWallpaper() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                MainComposePreferences.getLastLiveWallpaperPath()?.toFile()?.let {
                    Wallpaper.createFromFile(it, getApplication()).let {
                        lastLiveWallpaper.postValue(it)
                    }
                }
            } catch (_: NullPointerException) {
            } catch (_: FileNotFoundException) {
            }
        }

        countDownFlow.value = RANDOM_WALLPAPER_DELAY
        startCountDownFlow()
    }

    private fun createTempFile(fileName: String): File {
        val file = File(getApplication<Application>().cacheDir, fileName)
        if (file.exists()) file.delete()
        return file
    }

    private fun clearLegacyResidualFiles() {
        val filesDir = getApplication<Application>().filesDir
        val cacheDir = getApplication<Application>().cacheDir
        val twoDaysInMillis = 2 * 24 * 60 * 60 * 1000L
        val currentTime = System.currentTimeMillis()

        filesDir.listFiles()?.forEach {
            if (it.name.startsWith("system_wallpaper_")
                    || it.name.startsWith("lock_wallpaper_")) {
                it.delete()
            }
        }

        cacheDir.listFiles()?.forEach {
            if ((it.name.startsWith("system_wallpaper_")
                            || it.name.startsWith("lock_wallpaper_"))
                    && (currentTime - it.lastModified() > twoDaysInMillis)) {
                it.delete()
            }
        }
    }

    @Throws(NoSuchElementException::class)
    private fun getRandomWallpaperFromDatabase(): Wallpaper {
        val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
        val wallpaper = wallpaperDatabase?.wallpaperDao()?.getRandomWallpaper()
        return wallpaper ?: Wallpaper()
    }

    fun refetchSystemWallpapers() {
        postCurrentSystemWallpaper()
        postCurrentLockWallpaper()
    }

    override fun onCleared() {
        super.onCleared()
        stopCountDownFlow() // Unnecessary, but just in case
        unregisterSharedPreferenceChangeListener()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainComposePreferences.LAST_LIVE_WALLPAPER_PATH -> {
                postLastLiveWallpaper()
            }
        }
    }

    fun nextRandomWallpaper() {
        stopCountDownFlow()
        postRandomWallpaper()
    }

    fun deleteWallpaper(wallpaper: Wallpaper?, onDelete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            if (wallpaper != null) {
                if (wallpaper.filePath.toFile().delete()) {
                    val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
                    wallpaperDatabase?.wallpaperDao()?.delete(wallpaper)
                    onDelete()
                } else {
                    Log.e("HomeScreenViewModel", "Failed to delete wallpaper: ${wallpaper.name}")
                }
            }
        }
    }

    companion object {
        private const val SYSTEM_WALLPAPER = "system_wallpaper_$.png"
        private const val LOCK_WALLPAPER = "lock_wallpaper_$.png"
        const val RANDOM_WALLPAPER_DELAY = 15000L
    }
}
