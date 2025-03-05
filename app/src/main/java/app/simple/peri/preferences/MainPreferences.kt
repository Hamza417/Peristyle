package app.simple.peri.preferences

import android.annotation.SuppressLint
import app.simple.peri.utils.WallpaperSort

@SuppressLint("UseKtx")
object MainPreferences {

    private const val CROP_WALLPAPER = "crop_wallpaper"
    private const val WALLPAPER_SET_FOR = "auto_wallpaper_set_for"

    const val SORT = "sort"
    const val ORDER = "order"
    const val MARGIN_BETWEEN = "margin_between_wallpapers"
    const val AUTO_WALLPAPER_INTERVAL = "auto_wallpaper_interval_1"
    private const val TWEAKS = "tweaks"

    const val BOTH = "3"
    const val HOME = "1"
    const val LOCK = "2"

    const val IGNORE_DOT_FILES = "1"
    const val IGNORE_SUB_DIRS = "2"
    private const val LINEAR_AUTO_WALLPAPER = "3"

    fun getSort(): String? {
        return SharedPreferences.getSharedPreferences().getString(SORT, WallpaperSort.DATE)
    }

    fun setSort(sort: String) {
        SharedPreferences.getSharedPreferences().edit().putString(SORT, sort).apply()
    }

    fun getOrder(): String? {
        return SharedPreferences.getSharedPreferences().getString(ORDER, WallpaperSort.DESC)
    }

    fun setOrder(order: String) {
        SharedPreferences.getSharedPreferences().edit().putString(ORDER, order).apply()
    }

    fun getAutoWallpaperInterval(): String {
        return SharedPreferences.getSharedPreferences().getString(AUTO_WALLPAPER_INTERVAL, "0")!!
    }

    fun setAutoWallpaperInterval(interval: String) {
        SharedPreferences.getSharedPreferences().edit().putString(AUTO_WALLPAPER_INTERVAL, interval).apply()
    }

    fun getCropWallpaper(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(CROP_WALLPAPER, false)
    }

    fun setCropWallpaper(cropWallpaper: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(CROP_WALLPAPER, cropWallpaper).apply()
    }

    fun getWallpaperSetFor(): String {
        return SharedPreferences.getSharedPreferences().getString(WALLPAPER_SET_FOR, BOTH)!!
    }

    fun isSettingForHomeScreen(): Boolean {
        return getWallpaperSetFor() == HOME || getWallpaperSetFor() == BOTH
    }

    fun isSettingForLockScreen(): Boolean {
        return getWallpaperSetFor() == LOCK || getWallpaperSetFor() == BOTH
    }

    fun isSettingForBoth(): Boolean {
        return getWallpaperSetFor() == BOTH
    }

    fun setWallpaperSetFor(wallpaperSetFor: String) {
        SharedPreferences.getSharedPreferences().edit().putString(WALLPAPER_SET_FOR, wallpaperSetFor).apply()
    }

    private fun getTweaks(): Set<String>? {
        return SharedPreferences.getSharedPreferences().getStringSet(TWEAKS, null)
    }

    fun isTweakOptionSelected(option: String): Boolean {
        val tweaks = getTweaks()
        return tweaks?.contains(option) ?: false
    }

    fun isIgnoreDotFiles(): Boolean {
        return isTweakOptionSelected(IGNORE_DOT_FILES)
    }

    fun isIgnoreSubDirs(): Boolean {
        return isTweakOptionSelected(IGNORE_SUB_DIRS)
    }

    fun isLinearAutoWallpaper(): Boolean {
        return isTweakOptionSelected(LINEAR_AUTO_WALLPAPER)
    }

    fun setIgnoreDotFiles(ignoreDotFiles: Boolean) {
        setTweakOption(IGNORE_DOT_FILES, ignoreDotFiles)
    }

    fun setIgnoreSubDirs(ignoreSubDirs: Boolean) {
        setTweakOption(IGNORE_SUB_DIRS, ignoreSubDirs)
    }

    fun setLinearAutoWallpaper(linearAutoWallpaper: Boolean) {
        setTweakOption(LINEAR_AUTO_WALLPAPER, linearAutoWallpaper)
    }

    private fun setTweakOption(option: String, enabled: Boolean) {
        val tweaks = getTweaks()?.toMutableSet() ?: mutableSetOf()
        if (enabled) {
            tweaks.add(option)
        } else {
            tweaks.remove(option)
        }
        SharedPreferences.getSharedPreferences().edit().putStringSet(TWEAKS, tweaks).apply()
    }
}
