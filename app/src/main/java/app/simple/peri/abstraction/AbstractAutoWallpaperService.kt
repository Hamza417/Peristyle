package app.simple.peri.abstraction

import android.app.Service
import android.app.WallpaperManager
import android.util.Log
import app.simple.peri.database.instances.LastWallpapersDatabase
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.ListUtils.deepEquals
import app.simple.peri.utils.ScreenUtils
import app.simple.peri.utils.WallpaperServiceNotification.createNotificationChannels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class AbstractAutoWallpaperService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    protected val wallpaperManager: WallpaperManager by lazy {
        WallpaperManager.getInstance(applicationContext)
    }

    protected val displayWidth: Int by lazy {
        ScreenUtils.getScreenSize(applicationContext).width
    }

    protected val displayHeight: Int by lazy {
        ScreenUtils.getScreenSize(applicationContext).height
    }

    protected suspend fun getWallpapersFromDatabase(): List<Wallpaper>? {
        return withContext(Dispatchers.IO) {
            val dao = WallpaperDatabase.getInstance(applicationContext)?.wallpaperDao()
            dao?.sanitizeEntries()
            dao?.getWallpapers()
        }
    }

    protected suspend fun getRandomWallpaperFromDatabase(): Wallpaper {
        return withContext(Dispatchers.IO) {
            val dao = WallpaperDatabase.getInstance(applicationContext)?.wallpaperDao()!!
            val dupDao = LastWallpapersDatabase.getInstance(applicationContext)?.wallpaperDao()!!

            if (dao.getWallpapers().deepEquals(dupDao.getWallpapers())) {
                LastWallpapersDatabase.getInstance(applicationContext)?.clearAllTables()
                Log.i(TAG, "LastWallpapersDatabase cleared because it was equal to WallpaperDatabase")
            }

            val wallpaper = try {
                dao.getWallpapers().filterNot { it in dupDao.getWallpapers() }.random()
            } catch (e: NoSuchElementException) {
                dao.getWallpapers().random()
            }

            wallpaper.let { dupDao.insert(it) }
            LastWallpapersDatabase.destroyInstance()

            wallpaper
        }
    }

    companion object {
        private const val TAG = "AbstractAutoWallpaperService"
    }
}
