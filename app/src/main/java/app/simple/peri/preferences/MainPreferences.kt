package app.simple.peri.preferences

import app.simple.peri.utils.WallpaperSort

object MainPreferences {

    private const val storageUri = "storageUri"
    private const val nomediaDialog = "nomediaDialog"
    private const val blur = "blur_main_list"
    const val sort = "sort"
    const val order = "order"
    const val name = "is_name"
    const val gridSpan = "gridSpan"
    const val details = "is_details"

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
        return SharedPreferences.getSharedPreferences().getInt(gridSpan, 2)
            .coerceAtLeast(1)
            .coerceAtMost(4)
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
}