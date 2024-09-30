package app.simple.peri.viewmodels

import android.app.Application
import android.app.WallpaperInfo
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.service.wallpaper.WallpaperService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.peri.models.LiveWallpaperInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LiveWallpapersViewModel(application: Application) : AndroidViewModel(application) {

    private val liveWallpapers: MutableLiveData<List<LiveWallpaperInfo>> by lazy {
        MutableLiveData<List<LiveWallpaperInfo>>().also {
            getLiveWallpapers()
        }
    }

    fun getLiveWallpapersLiveData() = liveWallpapers

    private fun getLiveWallpapers() {
        viewModelScope.launch(Dispatchers.Default) {
            val packageManager: PackageManager = getApplication<Application>().packageManager
            val availableWallpapersList: MutableList<ResolveInfo> = packageManager.queryIntentServices(
                    Intent(WallpaperService.SERVICE_INTERFACE), PackageManager.GET_META_DATA)

            val wallpapers = availableWallpapersList.mapNotNull {
                try {
                    val wallpaperInfo = WallpaperInfo(getApplication(), it)
                    LiveWallpaperInfo(
                            name = wallpaperInfo.loadLabel(packageManager).toString(),
                            icon = wallpaperInfo.loadThumbnail(packageManager),
                            resolveInfo = it
                    )
                } catch (e: Exception) {
                    null
                }
            }

            liveWallpapers.postValue(wallpapers)
        }
    }
}
