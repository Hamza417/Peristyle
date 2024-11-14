package app.simple.peri.viewmodels

import android.app.Application
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.PermissionUtils
import id.zelory.compressor.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val handler = Handler(Looper.getMainLooper())

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
                systemWallpaperData.postValue(Wallpaper.createFromFile(systemFile))
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
                lockWallpaperData.postValue(Wallpaper.createFromFile(lockFile))
            }
        }
    }

    private fun postRandomWallpaper() {
        if (BuildConfig.DEBUG.invert()) {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    randomWallpaperData.postValue(getRandomWallpaperFromDatabase())
                } catch (_: NoSuchElementException) {
                }
            }
        }

        startPostingRandomWallpaper()
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

    private val randomWallpaperRepeatRunnable = Runnable {
        postRandomWallpaper()
    }

    fun stopPostingRandomWallpaper() {
        handler.removeCallbacksAndMessages(null)
    }

    fun startPostingRandomWallpaper() {
        stopPostingRandomWallpaper()
        handler.postDelayed(randomWallpaperRepeatRunnable, RANDOM_WALLPAPER_DELAY)
    }

    fun refetchSystemWallpapers() {
        postCurrentSystemWallpaper()
        postCurrentLockWallpaper()
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val SYSTEM_WALLPAPER = "system_wallpaper_$.png"
        private const val LOCK_WALLPAPER = "lock_wallpaper_$.png"
        private const val RANDOM_WALLPAPER_DELAY = 15L * 1000L // 30 seconds
    }
}
