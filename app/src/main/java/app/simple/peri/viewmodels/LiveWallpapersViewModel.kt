package app.simple.peri.viewmodels

import android.app.Application
import android.app.WallpaperInfo
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.UserHandle
import android.service.wallpaper.WallpaperService
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.peri.models.LiveWallpaperInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LiveWallpapersViewModel(application: Application) : AndroidViewModel(application) {

    private val launcherAppsService: LauncherApps by lazy {
        getApplication<Application>().getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    }

    private var launcherAppsCallback: LauncherApps.Callback? = null

    init {
        if (launcherAppsCallback == null) {
            launcherAppsCallback = object : LauncherApps.Callback() {
                override fun onPackageRemoved(packageName: String, user: UserHandle) {
                    getLiveWallpapers()
                }

                override fun onPackageAdded(packageName: String, user: UserHandle) {
                    getLiveWallpapers()
                }

                override fun onPackageChanged(packageName: String, user: UserHandle) {
                    getLiveWallpapers()
                }

                override fun onPackagesAvailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) {
                    getLiveWallpapers()
                }

                override fun onPackagesUnavailable(packageNames: Array<out String>?, user: UserHandle?, replacing: Boolean) {
                    getLiveWallpapers()
                }
            }

            launcherAppsService.registerCallback(launcherAppsCallback!!)
        }
    }

    private val liveWallpapers: MutableLiveData<List<LiveWallpaperInfo>> by lazy {
        MutableLiveData<List<LiveWallpaperInfo>>().also {
            getLiveWallpapers()
        }
    }

    fun getLiveWallpapersLiveData() = liveWallpapers

    private fun getLiveWallpapers() {
        viewModelScope.launch(Dispatchers.Default) {
            Log.i("LiveWallpapersViewModel", "getLiveWallpapers")
            val packageManager: PackageManager = getApplication<Application>().packageManager
            val availableWallpapersList: MutableList<ResolveInfo> = packageManager.queryIntentServices(
                    Intent(WallpaperService.SERVICE_INTERFACE), PackageManager.GET_META_DATA)

            val wallpapers = availableWallpapersList.mapNotNull {
                try {
                    val wallpaperInfo = WallpaperInfo(getApplication(), it)
                    LiveWallpaperInfo(
                            name = getLabelForWallpaper(wallpaperInfo, packageManager),
                            description = getDescriptionForWallpaper(wallpaperInfo, packageManager),
                            icon = wallpaperInfo.loadThumbnail(packageManager),
                            resolveInfo = it
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            liveWallpapers.postValue(wallpapers.sortedBy { it.name })
        }
    }

    private fun getLabelForWallpaper(wallpaperInfo: WallpaperInfo, packageManager: PackageManager): String {
        return try {
            wallpaperInfo.loadLabel(packageManager).toString()
        } catch (_: Exception) {
            "Unknown"
        }
    }

    private fun getDescriptionForWallpaper(wallpaperInfo: WallpaperInfo, packageManager: PackageManager): String {
        return try {
            wallpaperInfo.loadDescription(packageManager)?.toString() ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        launcherAppsService.unregisterCallback(launcherAppsCallback!!)
    }
}
