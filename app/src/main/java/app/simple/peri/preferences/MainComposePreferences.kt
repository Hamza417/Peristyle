package app.simple.peri.preferences

import app.simple.peri.preferences.SharedPreferences.getSharedPreferences

object MainComposePreferences {

    private const val GRID_SPAN_COUNT = "grid_span_count"
    private const val WARNING_INDICATOR = "warning_indicator"
    private const val IMAGE_SHADOW = "image_shadow"
    private const val IS_LOCK_SOURCE_SET = "is_lock_source_set"
    private const val IS_HOME_SOURCE_SET = "is_home_source_set"
    private const val LOCK_TAG_ID = "lock_tag_id"
    private const val LOCK_FOLDER_ID = "lock_folder_id_"
    private const val LOCK_FOLDER_NAME = "lock_folder_name_"
    private const val HOME_TAG_ID = "home_tag_id"
    private const val HOME_FOLDER_ID = "home_folder_id_"
    private const val HOME_FOLDER_NAME = "home_folder_name_"

    // ----------------------------------------------------------------------------------------------------- //

    fun getGridSpanCount(): Int {
        return getSharedPreferences().getInt(GRID_SPAN_COUNT, 2)
    }

    fun setGridSpanCount(value: Int) {
        getSharedPreferences().edit().putInt(GRID_SPAN_COUNT, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getShowWarningIndicator(): Boolean {
        return getSharedPreferences().getBoolean(WARNING_INDICATOR, false)
    }

    fun setShowWarningIndicator(value: Boolean) {
        getSharedPreferences().edit().putBoolean(WARNING_INDICATOR, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getShowImageShadow(): Boolean {
        return getSharedPreferences().getBoolean(IMAGE_SHADOW, true)
    }

    fun setShowImageShadow(value: Boolean) {
        getSharedPreferences().edit().putBoolean(IMAGE_SHADOW, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getIsLockSourceSet(): Boolean {
        return getSharedPreferences().getBoolean(IS_LOCK_SOURCE_SET, false)
    }

    fun setIsLockSourceSet(value: Boolean) {
        getSharedPreferences().edit().putBoolean(IS_LOCK_SOURCE_SET, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getIsHomeSourceSet(): Boolean {
        return getSharedPreferences().getBoolean(IS_HOME_SOURCE_SET, false)
    }

    fun setIsHomeSourceSet(value: Boolean) {
        getSharedPreferences().edit().putBoolean(IS_HOME_SOURCE_SET, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getLockTagId(): String? {
        return getSharedPreferences().getString(LOCK_TAG_ID, null)
    }

    fun setLockTagId(value: String) {
        getSharedPreferences().edit().putString(LOCK_TAG_ID, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getLockFolderId(): Int {
        return getSharedPreferences().getInt(LOCK_FOLDER_ID, 0)
    }

    fun setLockFolderId(value: String) {
        getSharedPreferences().edit().putString(LOCK_FOLDER_ID, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getLockFolderName(): String? {
        return getSharedPreferences().getString(LOCK_FOLDER_NAME, null)
    }

    fun setLockFolderName(value: String) {
        getSharedPreferences().edit().putString(LOCK_FOLDER_NAME, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getHomeTagId(): String? {
        return getSharedPreferences().getString(HOME_TAG_ID, null)
    }

    fun setHomeTagId(value: String) {
        getSharedPreferences().edit().putString(HOME_TAG_ID, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getHomeFolderId(): Int {
        return getSharedPreferences().getInt(HOME_FOLDER_ID, 0)
    }

    fun setHomeFolderId(value: String) {
        getSharedPreferences().edit().putString(HOME_FOLDER_ID, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getHomeFolderName(): String? {
        return getSharedPreferences().getString(HOME_FOLDER_NAME, null)
    }

    fun setHomeFolderName(value: String) {
        getSharedPreferences().edit().putString(HOME_FOLDER_NAME, value).apply()
    }
}
