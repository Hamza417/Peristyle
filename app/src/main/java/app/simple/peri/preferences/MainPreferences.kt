package app.simple.peri.preferences

import app.simple.peri.utils.WallpaperSort

object MainPreferences {

    private const val storageUri = "storageUri"
    private const val nomediaDialog = "nomediaDialog"
    private const val blur = "blur_main_list"
    private const val isAppEngine = "is_app_engine"
    private const val isBiometric = "is_biometric"
    private const val rememberScrollPosition = "rememberScrollPosition"
    private const val scrollPosition = "scrollPosition"

    const val sort = "sort"
    const val order = "order"
    const val name = "is_name"
    const val gridSpan = "gridSpan"
    const val details = "is_details"
    const val marginBetween = "marginBetween"
    const val mainScreenBackground = "mainScreenBackground"
    const val swipeToDelete = "swipeToDelete"

    const val SPAN_RANDOM = 3

    fun getStorageUri(): String? {
        return SharedPreferences.getSharedPreferences().getString(storageUri, null)
    }

    fun setStorageUri(uri: String?): Boolean {
        return SharedPreferences.getSharedPreferences().edit().putString(storageUri, uri).commit()
    }

    fun getSort(): String? {
        return SharedPreferences.getSharedPreferences().getString(sort, WallpaperSort.DATE)
    }

    fun setSort(sort: String) {
        SharedPreferences.getSharedPreferences().edit().putString(this.sort, sort).apply()
    }

    fun getOrder(): String? {
        return SharedPreferences.getSharedPreferences().getString(order, WallpaperSort.DESC)
    }

    fun setOrder(order: String) {
        SharedPreferences.getSharedPreferences().edit().putString(this.order, order).apply()
    }

    fun getName(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(name, true)
    }

    fun setName(name: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(this.name, name).apply()
    }

    fun getGridSpan(): Int {
        return SharedPreferences.getSharedPreferences().getInt(gridSpan, SPAN_RANDOM)
    }

    fun setGridSpan(span: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(gridSpan, span).apply()
    }

    fun setShowNomediaDialog(b: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(nomediaDialog, b).apply()
    }

    fun getShowNomediaDialog(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(nomediaDialog, true)
    }

    fun setBlur(b: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(blur, b).apply()
    }

    fun getBlur(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(blur, true)
    }

    fun setDetails(b: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(details, b).apply()
    }

    fun getDetails(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(details, true)
    }

    fun setAppEngine(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isAppEngine, boolean).apply()
    }

    fun getAppEngine(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isAppEngine, true)
    }

    fun setBiometric(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(isBiometric, boolean).apply()
    }

    fun isBiometric(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(isBiometric, false)
    }

    fun setRememberScrollPosition(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(rememberScrollPosition, boolean).apply()
    }

    fun isRememberScrollPosition(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(rememberScrollPosition, false)
    }

    fun setScrollPosition(position: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(scrollPosition, position).apply()
    }

    fun getScrollPosition(): Int {
        return SharedPreferences.getSharedPreferences().getInt(scrollPosition, 0)
    }

    fun getMarginBetween(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(marginBetween, false)
    }

    fun setMarginBetween(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(marginBetween, boolean).apply()
    }

    fun getMainScreenBackground(): Int {
        return SharedPreferences.getSharedPreferences().getInt(mainScreenBackground, 0)
    }

    fun setMainScreenBackground(int: Int) {
        SharedPreferences.getSharedPreferences().edit().putInt(mainScreenBackground, int).apply()
    }

    fun getSwipeToDelete(): Boolean {
        return SharedPreferences.getSharedPreferences().getBoolean(swipeToDelete, true)
    }

    fun setSwipeToDelete(boolean: Boolean) {
        SharedPreferences.getSharedPreferences().edit().putBoolean(swipeToDelete, boolean).apply()
    }
}