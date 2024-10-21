package app.simple.peri.viewmodels

import android.app.Application
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class HomeScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val handler = Handler(Looper.getMainLooper())

    private val systemWallpaperData: MutableLiveData<ArrayList<Wallpaper>> by lazy {
        MutableLiveData<ArrayList<Wallpaper>>().also {
            postCurrentSystemWallpaper()
        }
    }

    fun getSystemWallpaper(): MutableLiveData<ArrayList<Wallpaper>> {
        return systemWallpaperData
    }

    private fun postCurrentSystemWallpaper() {
        Log.i("HomeScreenViewModel", "Posting current system wallpaper")
        viewModelScope.launch(Dispatchers.IO) {
            if (PermissionUtils.checkStoragePermission(getApplication())) {
                systemWallpaperData.postValue(getCurrentSystemWallpaper())
            }
        }

        handler.postDelayed(randomWallpaperRepeatRunnable, 10000)
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
            Wallpaper().createFromUri(lockUri.toString(), getApplication()),
            getRandomWallpaperFromDatabase()
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

    private fun getRandomWallpaperFromDatabase(): Wallpaper {
        val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
        val wallpaper = wallpaperDatabase?.wallpaperDao()?.getRandomWallpaper()
        return wallpaper ?: Wallpaper()
    }

    private val randomWallpaperRepeatRunnable = object : Runnable {
        override fun run() {
            viewModelScope.launch(Dispatchers.Default) {
                systemWallpaperData.postValue(systemWallpaperData.value?.apply {
                    this[2] = getRandomWallpaperFromDatabase()
                })

                Log.i("HomeScreenViewModel", "Posting random wallpaper")
            }

            handler.removeCallbacksAndMessages(null)
            handler.postDelayed(this, 10000)
        }
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val SYSTEM_WALLPAPER = "system_wallpaper_$.png"
        private const val LOCK_WALLPAPER = "lock_wallpaper_$.png"
    }
}
