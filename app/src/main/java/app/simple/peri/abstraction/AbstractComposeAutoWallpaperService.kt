package app.simple.peri.abstraction

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.util.Log
import app.simple.peri.abstraction.AutoWallpaperUtils.getBitmapFromFile
import app.simple.peri.database.instances.LastHomeWallpapersDatabase
import app.simple.peri.database.instances.LastLockWallpapersDatabase
import app.simple.peri.database.instances.TagsDatabase
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Effect
import app.simple.peri.models.Wallpaper
import app.simple.peri.models.WallpaperUsage
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.BitmapUtils.applyEffects
import app.simple.peri.utils.ConditionUtils.isNotNull
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.utils.WallpaperServiceNotification.showErrorNotification
import app.simple.peri.utils.WallpaperServiceNotification.showWallpaperChangedNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class AbstractComposeAutoWallpaperService : AbstractAutoWallpaperService() {

    protected fun setComposeWallpaper(onComplete: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            validateCollection()

            runCatching {
                when {
                    MainPreferences.isSettingForBoth() -> {
                        if (shouldSetSameWallpaper()) {
                            getHomeScreenWallpaper()?.let {
                                setSameWallpaper(it)
                            }
                        } else {
                            getHomeScreenWallpaper()?.let {
                                setHomeScreenWallpaper(it)
                            }

                            getLockScreenWallpaper()?.let {
                                setLockScreenWallpaper(it)
                            }
                        }
                    }
                    MainPreferences.isSettingForHomeScreen() -> {
                        getHomeScreenWallpaper()?.let {
                            setHomeScreenWallpaper(it)
                        }
                    }
                    MainPreferences.isSettingForLockScreen() -> {
                        getLockScreenWallpaper()?.let {
                            setLockScreenWallpaper(it)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    onComplete()
                    validateUsage()
                    stopSelf()
                }
            }.getOrElse {
                it.printStackTrace()
                Log.e(TAG, "Error setting wallpaper: $it")

                withContext(Dispatchers.Main) {
                    showErrorNotification(it.stackTraceToString())
                    onComplete()
                    stopSelf()
                }
            }
        }
    }

    private fun setWallpaper(wallpaper: Wallpaper, effects: Effect, flag: Int, isHomeScreen: Boolean) {
        Log.d(TAG, "${if (isHomeScreen) "Home" else "Lock"} wallpaper found: ${wallpaper.filePath}")
        getBitmapFromFile(wallpaper.filePath, displayWidth, displayHeight, MainPreferences.getCropWallpaper()) { bitmap ->
            val modifiedBitmap = bitmap.copy(bitmap.config ?: Bitmap.Config.ARGB_8888, true)
                .applyEffects(effects)

            when (MainComposePreferences.getWallpaperSetMethod()) {
                MainComposePreferences.BITMAP -> {
                    wallpaperManager.setBitmap(modifiedBitmap, null, true, flag)
                }
                MainComposePreferences.STREAM -> {
                    wallpaperManager.setStream(
                            AutoWallpaperUtils.bitmapToInputStream(modifiedBitmap),
                            null,
                            true,
                            flag
                    )
                }
            }

            showWallpaperChangedNotification(isHomeScreen, wallpaper.filePath.toFile(), modifiedBitmap)
        }
    }

    open fun setHomeScreenWallpaper(wallpaper: Wallpaper) {
        setWallpaper(
                wallpaper,
                MainComposePreferences.getHomeScreenEffects(),
                WallpaperManager.FLAG_SYSTEM,
                isHomeScreen = true
        )
    }

    open fun setLockScreenWallpaper(wallpaper: Wallpaper) {
        setWallpaper(
                wallpaper,
                MainComposePreferences.getLockScreenEffects(),
                WallpaperManager.FLAG_LOCK,
                isHomeScreen = false
        )
    }

    open fun setSameWallpaper(wallpaper: Wallpaper) {
        MainComposePreferences.setLastLockWallpaperPosition(MainComposePreferences.getLastHomeWallpaperPosition())
        Log.d(TAG, "Wallpaper found: ${wallpaper.filePath}")
        getBitmapFromFile(wallpaper.filePath, displayWidth, displayHeight, MainPreferences.getCropWallpaper()) { bitmap ->
            val homeBitmap = bitmap.copy(MainComposePreferences.getWallpaperColorSpace(), true)
                .applyEffects(MainComposePreferences.getHomeScreenEffects())
            val lockBitmap = bitmap.copy(MainComposePreferences.getWallpaperColorSpace(), true)
                .applyEffects(MainComposePreferences.getLockScreenEffects())

            // Home screen
            when (MainComposePreferences.getWallpaperSetMethod()) {
                MainComposePreferences.BITMAP -> {
                    wallpaperManager.setBitmap(homeBitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                }
                MainComposePreferences.STREAM -> {
                    wallpaperManager.setStream(
                            AutoWallpaperUtils.bitmapToInputStream(homeBitmap),
                            null,
                            true,
                            WallpaperManager.FLAG_SYSTEM
                    )
                }
            }

            showWallpaperChangedNotification(true, wallpaper.filePath.toFile(), homeBitmap)

            // Lock screen
            when (MainComposePreferences.getWallpaperSetMethod()) {
                MainComposePreferences.BITMAP -> {
                    wallpaperManager.setBitmap(lockBitmap, null, true, WallpaperManager.FLAG_LOCK)
                }
                MainComposePreferences.STREAM -> {
                    wallpaperManager.setStream(
                            AutoWallpaperUtils.bitmapToInputStream(lockBitmap),
                            null,
                            true,
                            WallpaperManager.FLAG_LOCK
                    )
                }
            }
            showWallpaperChangedNotification(false, wallpaper.filePath.toFile(), lockBitmap)
        }
    }

    private fun shouldSetSameWallpaper(): Boolean {
        if (!MainPreferences.isSettingForHomeScreen() || !MainPreferences.isSettingForLockScreen()) {
            return false
        }

        return isSameFolderOrTagUsed() && MainPreferences.isLinearAutoWallpaper()
    }

    private fun isSameFolderOrTagUsed(): Boolean {
        val homeSourceSet = MainComposePreferences.isHomeSourceSet()
        val lockSourceSet = MainComposePreferences.isLockSourceSet()

        if (!homeSourceSet && !lockSourceSet) {
            return true
        }

        if (homeSourceSet && lockSourceSet) {
            val sameTag = MainComposePreferences.getHomeTagId() == MainComposePreferences.getLockTagId()
            val sameFolder = MainComposePreferences.getHomeFolderId() == MainComposePreferences.getLockFolderId()
            return sameTag || sameFolder
        }

        return false
    }

    protected suspend fun getHomeScreenWallpaper(): Wallpaper? {
        val wallpaperDatabase = WallpaperDatabase.getInstance(applicationContext)
        val wallpaperDao = wallpaperDatabase?.wallpaperDao()
        val position = MainComposePreferences.getLastHomeWallpaperPosition().plus(1)
        var wallpaper: Wallpaper? = null

        val tagId = MainComposePreferences.getHomeTagId()
        val folderId = MainComposePreferences.getHomeFolderId()

        when {
            tagId.isNotNull() -> {
                runCatching {
                    val tagsDatabase = TagsDatabase.getInstance(applicationContext)
                    val tagsDao = tagsDatabase?.tagsDao()
                    val tag = tagsDao?.getTagByID(tagId!!)
                    val wallpapers = wallpaperDao?.getWallpapersByMD5s(tag?.sum!!)

                    wallpaper = getWallpaperFromList(wallpapers, position, isHomeScreen = true)
                }.getOrElse {
                    Log.e(TAG, "Error getting wallpaper by tag: $it")
                    showErrorNotification("Error getting wallpaper by tag: $it")
                }
            }

            folderId != -1 -> {
                runCatching {
                    val wallpapers = wallpaperDao?.getWallpapersByPathHashcode(folderId)
                    wallpaper = getWallpaperFromList(wallpapers, position, isHomeScreen = true)
                }.getOrElse {
                    Log.e(TAG, "Error getting wallpaper by folder: $it")
                    showErrorNotification("Error getting wallpaper by folder: $it")
                }
            }

            else -> {
                wallpaper = getWallpaperFromList(wallpaperDao?.getWallpapers(), position, isHomeScreen = true)
            }
        }

        wallpaper?.let {
            insertOrUpdateWallpaperUsage(it)
        }

        return wallpaper
    }

    private suspend fun getLockScreenWallpaper(): Wallpaper? {
        val wallpaperDatabase = WallpaperDatabase.getInstance(applicationContext)
        val wallpaperDao = wallpaperDatabase?.wallpaperDao()
        val position = MainComposePreferences.getLastLockWallpaperPosition().plus(1)
        var wallpaper: Wallpaper? = null

        val tagId = MainComposePreferences.getLockTagId()
        val folderId = MainComposePreferences.getLockFolderId()

        when {
            tagId.isNotNull() -> {
                runCatching {
                    val tagsDatabase = TagsDatabase.getInstance(applicationContext)
                    val tagsDao = tagsDatabase?.tagsDao()
                    val tag = tagsDao?.getTagByID(tagId!!)
                    val wallpapers = wallpaperDao?.getWallpapersByMD5s(tag?.sum!!)

                    wallpaper = getWallpaperFromList(wallpapers, position, false)
                }.getOrElse {
                    Log.e(TAG, "Error getting wallpaper by tag: $it")
                    showErrorNotification("Error getting wallpaper by tag: $it")
                    return null
                }
            }

            folderId != -1 -> {
                runCatching {
                    val wallpapers = wallpaperDao?.getWallpapersByPathHashcode(folderId)
                    wallpaper = getWallpaperFromList(wallpapers, position, false)
                }.getOrElse {
                    Log.e(TAG, "Error getting wallpaper by folder: $it")
                    showErrorNotification("Error getting wallpaper by folder: $it")
                    return null
                }
            }

            else -> {
                wallpaper = getWallpaperFromList(wallpaperDao?.getWallpapers(), position, false)
            }
        }

        wallpaper?.let {
            insertOrUpdateWallpaperUsage(it)
        }

        return wallpaper
    }

    private suspend fun getWallpaperFromList(wallpapers: List<Wallpaper>?, position: Int, isHomeScreen: Boolean): Wallpaper? {
        if (isHomeScreen) {
            Log.i(TAG, "Getting home screen wallpaper at position: $position")
        } else {
            Log.i(TAG, "Getting lock screen wallpaper at position: $position")
        }

        return if (MainPreferences.isLinearAutoWallpaper()) {
            Log.i(TAG, "Linear auto wallpaper mode enabled, skipping non-repeating wallpapers")
            try {
                wallpapers?.get(position).also {
                    MainComposePreferences.setLastWallpaperPosition(isHomeScreen, position)
                }
            } catch (_: IndexOutOfBoundsException) {
                MainComposePreferences.resetLastWallpaperPosition(isHomeScreen)
                wallpapers?.get(0)
            }
        } else {
            Log.i(TAG, "Random auto wallpaper mode enabled, selecting a non-repeating random wallpaper")
            val wallpaper = try {
                Log.i(TAG, "Selecting a random wallpaper from the list")
                val usedIds = getLastUsedWallpapers(isHomeScreen, wallpapers)?.map { it.id }?.toSet() ?: emptySet()
                wallpapers
                    ?.filterNot { wallpaper -> wallpaper.id in usedIds }
                    ?.random()
            } catch (_: NoSuchElementException) {
                Log.i(TAG, "No non-repeating wallpapers found, selecting a random wallpaper from the entire list")
                wallpapers?.random()
            }

            Log.i(TAG, "Selected wallpaper: ${wallpaper?.id}")

            wallpaper?.let {
                insertWallpaperToLastUsedDatabase(it, isHomeScreen) // mark this wallpaper as used
            }

            if (isHomeScreen) {
                Log.i(TAG, "Home screen wallpaper processed")
            } else {
                Log.i(TAG, "Lock screen wallpaper processed")
            }

            wallpaper
        }
    }

    private fun getLastUsedWallpapers(homeScreen: Boolean = true, wallpapers: List<Wallpaper>?): List<Wallpaper>? {
        if (homeScreen) {
            val usedWallpapers = LastHomeWallpapersDatabase.getInstance(applicationContext)
                ?.wallpaperDao()?.getWallpapers()

            val usedIds: List<String> = usedWallpapers?.map { it.id } ?: emptyList()
            val wallpaperIds: List<String> = wallpapers?.map { it.id } ?: emptyList()

            if (wallpaperIds.size - usedIds.size == 0) {
                LastHomeWallpapersDatabase.getInstance(applicationContext)?.wallpaperDao()?.nukeTable()
                Log.i(TAG, "All home screen wallpapers have been used, clearing the last used database")
                return emptyList()
            }

            Log.i(TAG, "Number of unused home wallpapers left: ${wallpaperIds.size - usedIds.size}")

            return usedWallpapers
        } else {
            val usedWallpapers = LastLockWallpapersDatabase.getInstance(applicationContext)
                ?.wallpaperDao()?.getWallpapers()

            val usedIds: List<String> = usedWallpapers?.map { it.id } ?: emptyList()
            val wallpaperIds: List<String> = wallpapers?.map { it.id } ?: emptyList()

            if (wallpaperIds.size - usedIds.size == 0) {
                LastLockWallpapersDatabase.getInstance(applicationContext)?.wallpaperDao()?.nukeTable()
                Log.i(TAG, "All lock screen wallpapers have been used, clearing the last used database")
                return emptyList()
            }

            Log.i(TAG, "Number of unused lock wallpapers left: ${wallpaperIds.size - usedIds.size}")

            return usedWallpapers
        }
    }

    private suspend fun insertWallpaperToLastUsedDatabase(wallpaper: Wallpaper, homeScreen: Boolean) {
        if (homeScreen) {
            LastHomeWallpapersDatabase.getInstance(applicationContext)
                ?.wallpaperDao()?.insert(wallpaper)
        } else {
            LastLockWallpapersDatabase.getInstance(applicationContext)
                ?.wallpaperDao()?.insert(wallpaper)
        }
    }

    private suspend fun insertOrUpdateWallpaperUsage(wallpaper: Wallpaper) {
        val wallpaperDatabase = WallpaperDatabase.getInstance(applicationContext)
        val wallpaperUsage: WallpaperUsage? = wallpaperDatabase?.wallpaperDao()?.getWallpaperUsageById(wallpaper.id)

        if (wallpaperUsage != null) {
            wallpaperUsage.usageCount += 1
            wallpaperDatabase.wallpaperDao().insert(wallpaperUsage)
            Log.i(TAG, "Incremented usage count for wallpaper: ${wallpaper.id} to ${wallpaperUsage.usageCount}")
        } else {
            val newWallpaperUsage = WallpaperUsage(
                    wallpaper.id, 1,
            )

            wallpaperDatabase?.wallpaperDao()?.insert(newWallpaperUsage)
        }
    }

    companion object {
        const val ACTION_DELETE_WALLPAPER: String = "app.simple.peri.services.action.DELETE_WALLPAPER"
        const val ACTION_DELETE_WALLPAPER_HOME = "app.simple.peri.services.action.DELETE_WALLPAPER_HOME"
        const val ACTION_DELETE_WALLPAPER_LOCK = "app.simple.peri.services.action.DELETE_WALLPAPER_LOCK"
        const val ACTION_COPY_ERROR_MESSAGE = "COPY_ERROR_MESSAGE"

        const val EXTRA_IS_HOME_SCREEN = "app.simple.peri.services.extra.IS_HOME_SCREEN"
        const val EXTRA_WALLPAPER_PATH = "app.simple.peri.services.extra.PATH"
        const val EXTRA_NOTIFICATION_ID = "app.simple.peri.services.extra.NOTIFICATION_ID"
        const val EXTRA_ERROR_MESSAGE = "app.simple.peri.services.extra.ERROR_MESSAGE"

        const val TAG = "AutoWallpaperService"
    }
}
