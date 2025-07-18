package app.simple.peri.preferences

import androidx.core.content.edit

object WallHavenPreferences {

    const val WALLHAVEN_ATLEAST = "wallhaven_atleast"
    const val WALLHAVEN_SORT = "wallhaven_sort"
    const val WALLHAVEN_ORDER = "wallhaven_order"
    const val WALLHAVEN_CATEGORY = "wallhaven_category"
    const val WALLHAVEN_RESOLUTION = "wallhaven_resolution"
    const val WALLHAVEN_RATIO = "wallhaven_ratio"
    const val WALLHAVEN_QUERY = "wallhaven_query"

    // --------------------------------------------------------------------------------------------------- //

    fun getAtleast(): String? {
        return SharedPreferences.getSharedPreferences().getString(WALLHAVEN_ATLEAST, null)
    }

    fun setAtleast(value: String) {
        SharedPreferences.getSharedPreferences().edit { putString(WALLHAVEN_ATLEAST, value) }
    }

    // --------------------------------------------------------------------------------------------------- //

    fun getSort(): String {
        return SharedPreferences.getSharedPreferences().getString(WALLHAVEN_SORT, "relevance")!!
    }

    fun setSort(value: String) {
        SharedPreferences.getSharedPreferences().edit { putString(WALLHAVEN_SORT, value) }
    }

    // --------------------------------------------------------------------------------------------------- //

    fun getOrder(): String {
        return SharedPreferences.getSharedPreferences().getString(WALLHAVEN_ORDER, "desc")!!
    }

    fun setOrder(value: String) {
        SharedPreferences.getSharedPreferences().edit { putString(WALLHAVEN_ORDER, value) }
    }

    // --------------------------------------------------------------------------------------------------- //

    fun getCategory(): String {
        return SharedPreferences.getSharedPreferences().getString(WALLHAVEN_CATEGORY, "111")!!
    }

    fun setCategory(value: String) {
        SharedPreferences.getSharedPreferences().edit { putString(WALLHAVEN_CATEGORY, value) }
    }

    // --------------------------------------------------------------------------------------------------- //

    fun getResolution(): String? {
        return SharedPreferences.getSharedPreferences().getString(WALLHAVEN_RESOLUTION, null)
    }

    fun setResolution(value: String) {
        SharedPreferences.getSharedPreferences().edit { putString(WALLHAVEN_RESOLUTION, value) }
    }

    // --------------------------------------------------------------------------------------------------- //

    fun getRatio(): String {
        return SharedPreferences.getSharedPreferences().getString(WALLHAVEN_RATIO, "portrait")!!
    }

    fun setRatio(value: String) {
        SharedPreferences.getSharedPreferences().edit { putString(WALLHAVEN_RATIO, value) }
    }

    // --------------------------------------------------------------------------------------------------- //

    fun getQuery(): String {
        return SharedPreferences.getSharedPreferences().getString(WALLHAVEN_QUERY, "nature")!!
    }

    fun setQuery(value: String) {
        SharedPreferences.getSharedPreferences().edit { putString(WALLHAVEN_QUERY, value) }
    }
}