package app.simple.peri.abstraction

import android.app.Service
import android.app.WallpaperManager
import app.simple.peri.database.instances.LastWallpapersDatabase
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.ListUtils.deepEquals
import app.simple.peri.utils.ScreenUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class AbstractAutoWallpaperService : Service() {

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
}
