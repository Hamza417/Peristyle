package app.simple.peri.abstraction

import android.app.Service
import android.app.WallpaperManager
import android.content.res.Resources
import android.util.Log
import android.util.Size
import app.simple.peri.database.instances.LastWallpapersDatabase
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.WallpaperServiceNotification.createNotificationChannels
import kotlinx.coroutines.Dispatchers
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
        getScreenSizeFromService().width
    }

    protected val displayHeight: Int by lazy {
        getScreenSizeFromService().height
    }

    @Throws(NoSuchElementException::class)
    protected suspend fun getRandomWallpaperFromDatabase(): Wallpaper? {
        return withContext(Dispatchers.IO) {
            try {
                val dao = WallpaperDatabase.getInstance(applicationContext)?.wallpaperDao()!!
                val dupDao = LastWallpapersDatabase.getInstance(applicationContext)?.wallpaperDao()!!
                val areIdsEqual = dao.getWallpapers().map { it.id }.toSet() == dupDao.getWallpapers().map { it.id }.toSet()

                if (areIdsEqual) {
                    Log.i(TAG, "WallpaperDatabase and LastWallpapersDatabase have the same IDs, clearing LastWallpapersDatabase")
                    LastWallpapersDatabase.getInstance(applicationContext)?.clearAllTables()
                }

                val wallpaper = try {
                    val allWallpapers = dao.getWallpapers()
                    val usedIds = dupDao.getWallpapers().map { it.id }.toSet()
                    val unusedWallpapers = allWallpapers.filterNot { it.id in usedIds }

                    if (unusedWallpapers.isNotEmpty()) {
                        unusedWallpapers.random()
                    } else {
                        allWallpapers.random()
                    }
                } catch (_: NoSuchElementException) {
                    dao.getWallpapers().random()
                }

                wallpaper.let { dupDao.insert(it) }
                LastWallpapersDatabase.destroyInstance()

                wallpaper
            } catch (_: NoSuchElementException) {
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

    protected suspend fun validateUsage() {
        withContext(Dispatchers.IO) {
            kotlin.runCatching {
                Log.i(TAG, "Validating wallpaper usage counts")

                if (MainComposePreferences.getMaxSetCount() > 0) {
                    Log.i(TAG, "Max set count is enabled, checking wallpaper usage")
                    val dao = WallpaperDatabase.getInstance(applicationContext)?.wallpaperDao()
                    Log.i(TAG, "Checking wallpaper usage in database ${dao?.getAllWallpaperUsage()?.size ?: 0} entries")
                    dao!!.getAllWallpaperUsage().forEach {
                        Log.i(TAG, "Wallpaper ID: ${it.wallpaperId}, Usage Count: ${it.usageCount}")
                        if (it.usageCount >= MainComposePreferences.getMaxSetCount()) {
                            val wallpaper = dao.getWallpaperByID(it.wallpaperId)
                            if (wallpaper != null) {
                                Log.i(TAG, "Deleting wallpaper with ID: ${wallpaper.id} due to usage count limit")
                                if (File(wallpaper.filePath).delete()) {
                                    Log.i(TAG, "Wallpaper file deleted: ${wallpaper.filePath}")
                                    dao.delete(it)
                                }
                            } else {
                                Log.w(TAG, "Wallpaper with ID: ${it.wallpaperId} not found for deletion")
                            }
                        }
                    }
                }
            }.onFailure {
                Log.e(TAG, "Error validating wallpaper usage: ${it.message}", it)
            }
        }
    }

    fun getScreenSizeFromService(): Size {
        val metrics = Resources.getSystem().displayMetrics
        return Size(metrics.widthPixels, metrics.heightPixels)
    }

    companion object {
        private const val TAG = "AbstractAutoWallpaperService"
    }
}
