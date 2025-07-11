package app.simple.peri.abstraction

import android.app.Service
import android.app.WallpaperManager
import android.util.Log
import app.simple.peri.database.instances.LastWallpapersDatabase
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.ListUtils.deepEquals
import app.simple.peri.utils.ScreenUtils
import app.simple.peri.utils.WallpaperServiceNotification.createNotificationChannels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File

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

    @Throws(NoSuchElementException::class)
    protected suspend fun getRandomWallpaperFromDatabase(): Wallpaper? {
        return withContext(Dispatchers.IO) {
            try {
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
            } catch (e: NoSuchElementException) {
                Log.e(TAG, "No wallpapers found in database")
                null
            }
        }
    }

    protected suspend fun validateCollection() {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                val dao = WallpaperDatabase.getInstance(applicationContext)?.wallpaperDao()
                dao?.purgeNonExistingWallpapers(WallpaperDatabase.getInstance(applicationContext)!!)
            }
        }
    }

    protected fun validateUsage() {
        runBlocking {
            kotlin.runCatching {
                val dao = WallpaperDatabase.getInstance(applicationContext)?.wallpaperDao()
                dao?.getAllWallpaperUsage()?.forEach {
                    if (it.usageCount >= MainComposePreferences.getMaxSetCount() && MainComposePreferences.getMaxSetCount() != 0) {
                        val wallpaper = dao.getWallpaperByID(it.wallpaperId)
                        if (wallpaper != null) {
                            Log.i("WallpaperDao", "Deleting wallpaper with ID: ${wallpaper.id} due to usage count limit")
                            if (File(wallpaper.filePath).delete()) {
                                dao.delete(it)
                            }
                        } else {
                            Log.w("WallpaperDao", "Wallpaper with ID: ${it.wallpaperId} not found for deletion")
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "AbstractAutoWallpaperService"
    }
}
