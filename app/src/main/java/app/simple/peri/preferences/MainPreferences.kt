package app.simple.peri.preferences

import app.simple.peri.utils.WallpaperSort

object MainPreferences {

    private const val storageUri = "storageUri"
    private const val nomediaDialog = "nomediaDialog"
    private const val blur = "blur"
    private const val isBiometric = "is_biometric"
    private const val rememberScrollPosition = "remember_scroll_position"
    private const val scrollPosition = "scrollPosition"
    private const val cropWallpaper = "crop_wallpaper"
    private const val differentWallpaper = "different_wallpaper_for_lock_screen"
    private const val wallpaperWhenSleeping = "wallpaper_when_sleeping"
    private const val wallpaperSetFor = "auto_wallpaper_set_for"

    const val sort = "sort"
    const val order = "order"
    const val name = "is_name"
    const val gridSpan = "grid_span"
    const val details = "is_details"
    const val marginBetween = "margin_between_wallpapers"
    const val mainScreenBackground = "main_screen_background"
    const val swipeToDelete = "swipe_to_delete"
    const val autoWallpaperInterval = "auto_wallpaper_interval_1"

    const val SPAN_ONE = "1"
    const val SPAN_TWO = "2"
    const val SPAN_DYNAMIC = "3"

    const val BOTH = "3"
    const val HOME = "1"
    const val LOCK = "2"

    fun getStorageUri(): String? {
        return SharedPreferences.getSharedPreferences().getString(storageUri, null)
    }

    fun setStorageUri(uri: String?) {
        SharedPreferences.getSharedPreferences().edit().putString(storageUri, uri).apply()
    }

    fun getSort(): String? {
        return SharedPreferences.getSharedPreferences().getString(sort, WallpaperSort.DATE)
    }

    fun setSort(sort: String) {
        SharedPreferences.getSharedPreferences().edit().putString(MainPreferences.sort, sort).apply()
    }

    fun getOrder(): String? {
        return SharedPreferences.getSharedPreferences().getString(order, WallpaperSort.DESC)
    }

    fun setOrder(order: String) {
        SharedPreferences.getSharedPreferences().edit().putString(MainPreferences.order, order).apply()
    }

    fun getName(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(name, true)
    }

    fun getGridSpan(): String {
        return SharedPreferences.getSharedPreferences().getString(gridSpan, SPAN_DYNAMIC)!!
    }

    fun getShowNomediaDialog(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(nomediaDialog, true)
    }

    fun setShowNomediaDialog(show: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(nomediaDialog, show).apply()
    }

    fun getBlur(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(blur, true)
    }

    fun getDetails(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(details, true)
    }

    fun isBiometric(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isBiometric, false)
    }

    fun isRememberScrollPosition(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(rememberScrollPosition, false)
    }

    fun getScrollPosition(): Int {
        return SharedPreferences.getSharedPreferences().getInt(scrollPosition, 0)
    }

    fun setScrollPosition(position: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(scrollPosition, position).apply()
    }

    fun getMarginBetween(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(marginBetween, false)
    }

    fun getMainScreenBackground(): String? {
        return SharedPreferences.getSharedPreferences().getString(mainScreenBackground, "1")
    }

    fun getSwipeToDelete(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(swipeToDelete, true)
    }

    fun getAutoWallpaperInterval(): String {
        return SharedPreferences.getSharedPreferences().getString(autoWallpaperInterval, "0")!!
    }

    fun turnOffAutoWallpaperInterval() {
        SharedPreferences.getSharedPreferences().edit().putString(autoWallpaperInterval, "0").apply()
    }

    fun getCropWallpaper(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(cropWallpaper, false)
    }

    fun isDifferentWallpaperForLockScreen(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(differentWallpaper, false)
    }

    fun isWallpaperWhenSleeping(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(wallpaperWhenSleeping, true)
    }

    fun setWallpaperWhenSleeping(isWallpaperWhenSleeping: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(wallpaperWhenSleeping, isWallpaperWhenSleeping).apply()
    }

    fun getWallpaperSetFor(): String {
        return SharedPreferences.getSharedPreferences().getString(wallpaperSetFor, "3")!!
    }

    fun setWallpaperSetFor(wallpaperSetFor: String) {
        SharedPreferences.getSharedPreferences().edit().putString(MainPreferences.wallpaperSetFor, wallpaperSetFor).apply()
    }
}
