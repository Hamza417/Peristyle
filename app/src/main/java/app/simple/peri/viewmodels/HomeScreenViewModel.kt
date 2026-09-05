package app.simple.peri.viewmodels

import android.app.Application
import android.app.WallpaperManager
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import kotlin.time.Duration.Companion.milliseconds

class HomeScreenViewModel(application: Application) : AndroidViewModel(application), OnSharedPreferenceChangeListener {

    private var countDownJobs: ArrayList<Job> = ArrayList()
    private val countDownMutex = Mutex()

    val countDownFlow: MutableStateFlow<Long> = MutableStateFlow(RANDOM_WALLPAPER_DELAY)
    val isCountdownPaused: MutableStateFlow<Boolean> = MutableStateFlow(false)

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
            postCurrentLockWallpaper()
            Log.i(TAG, "Wallpaper colors changed")
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
                    delay(interval.milliseconds)
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
        Log.i(TAG, "Countdown flow stopped")
    }

    fun resumeCountDownFlow() {
        stopCountDownFlow()
        startCountDownFlow()
        Log.i(TAG, "Countdown flow resumed")
    }

    fun pauseCountdown() {
        stopCountDownFlow()
        isCountdownPaused.value = true
        Log.i(TAG, "Countdown paused")
    }

    fun resumeCountdown() {
        isCountdownPaused.value = false
        startCountDownFlow()
        Log.i(TAG, "Countdown resumed")
    }

    fun toggleCountdownPause() {
        if (isCountdownPaused.value) {
            resumeCountdown()
        } else {
            pauseCountdown()
        }
    }

    private fun postCurrentSystemWallpaper() {
        Log.i(TAG, "Posting current system wallpaper")
        viewModelScope.launch(Dispatchers.IO) {
            if (PermissionUtils.checkStoragePermission(getApplication())) {
                try {
                    val wallpaperManager = WallpaperManager.getInstance(getApplication())
                    val systemBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        wallpaperManager.getDrawable(WallpaperManager.FLAG_SYSTEM)?.toBitmap()
                    } else {
                        wallpaperManager.drawable?.toBitmap()
                    }

                    val systemFile = createTempFile(SYSTEM_WALLPAPER.replace("$", System.currentTimeMillis().div(1000).toString()))

                    systemFile.outputStream().use { systemBitmap?.compress(Bitmap.CompressFormat.PNG, 100, it) }

                    systemWallpaperData.postValue(Wallpaper.createFromFile(systemFile, getApplication()))
                } catch (_: IOException) {
                    // bad system wallpaper??
                } catch (_: SecurityException) {
                    // we should not be here but just in case
                }
            }
        }
    }

    private fun postCurrentLockWallpaper() {
        Log.i(TAG, "Posting current lock wallpaper")
        viewModelScope.launch(Dispatchers.IO) {
            if (!PermissionUtils.checkStoragePermission(getApplication())) return@launch

            try {
                val wallpaperManager = WallpaperManager.getInstance(getApplication())
                var lockBitmap: Bitmap? = null

                // Fetch Lock Screen Wallpaper based on Android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    lockBitmap = wallpaperManager.getDrawable(WallpaperManager.FLAG_LOCK)?.toBitmap()
                } else
                    wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK)?.use { pfd ->
                        lockBitmap = BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor)
                    }

                // Fallback to system wallpaper if lock is null (Shared state or decode failure)
                if (lockBitmap == null) {
                    Log.i(TAG, "Lock wallpaper null, falling back to system")
                    lockBitmap = wallpaperManager.drawable?.toBitmap()
                }

                // Early return if both failed to prevent creating an empty file
                if (lockBitmap == null) {
                    Log.e(TAG, "Failed to retrieve any wallpaper bitmap")
                    return@launch
                }

                // Create file and compress ONLY after guaranteeing a valid bitmap
                val lockFile = createTempFile(LOCK_WALLPAPER.replace("$", System.currentTimeMillis().div(1000).toString()))

                lockFile.outputStream().use {
                    lockBitmap?.compress(Bitmap.CompressFormat.PNG, 100, it)
                }

                lockWallpaperData.postValue(Wallpaper.createFromFile(lockFile, getApplication()))

            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: SecurityException) {
                e.printStackTrace()
            } catch (e: Exception) {
                // Catch OOMs during decode/toBitmap
                e.printStackTrace()
            }
        }
    }

    private fun postRandomWallpaper() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                randomWallpaperData.postValue(getRandomWallpaperFromDatabase())
            } catch (e: NoSuchElementException) {
                e.printStackTrace()
            }
        }

        countDownFlow.value = RANDOM_WALLPAPER_DELAY
        startCountDownFlow()
    }

    private fun postLastLiveWallpaper() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                MainComposePreferences.getLastLiveWallpaperPath()?.toFile()?.let { file ->
                    Wallpaper.createFromFile(file, getApplication()).let {
                        lastLiveWallpaper.postValue(it)
                    }
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
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
        isCountdownPaused.value = false
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
                    Log.e(TAG, "Failed to delete wallpaper: ${wallpaper.name}")
                }
            }
        }
    }

    companion object {
        private const val SYSTEM_WALLPAPER = "system_wallpaper_$.png"
        private const val LOCK_WALLPAPER = "lock_wallpaper_$.png"
        private const val TAG = "HomeScreenViewModel"
        const val RANDOM_WALLPAPER_DELAY = 15000L
    }
}
