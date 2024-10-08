package app.simple.peri.preferences

import app.simple.peri.preferences.SharedPreferences.getSharedPreferences

object MainComposePreferences {

    private const val GRID_SPAN_COUNT = "grid_span_count"
    private const val WARNING_INDICATOR = "warning_indicator"
    private const val IMAGE_SHADOW = "image_shadow"
    private const val GENERATE_MD5 = "generate_md5"
    private const val IS_LOCK_SOURCE_SET = "is_lock_source_set"
    private const val IS_HOME_SOURCE_SET = "is_home_source_set"
    private const val LOCK_TAG_ID = "lock_tag_id"
    private const val LOCK_FOLDER_ID = "lock_folder_id__"
    private const val LOCK_FOLDER_NAME = "lock_folder_name__"
    private const val HOME_TAG_ID = "home_tag_id"
    private const val HOME_FOLDER_ID = "home_folder_id__"
    private const val HOME_FOLDER_NAME = "home_folder_name__"
    private const val LAST_LOCK_WALLPAPER_POSITION = "last_lock_wallpaper_position"
    private const val LAST_HOME_WALLPAPER_POSITION = "last_home_wallpaper_position"
    private const val AUTO_WALLPAPER_BLUR = "auto_wallpaper_blur"
    private const val AUTO_WALLPAPER_BRIGHTNESS = "auto_wallpaper_brightness"
    private const val AUTO_WALLPAPER_CONTRAST = "auto_wallpaper_contrast"
    private const val AUTO_WALLPAPER_SATURATION = "auto_wallpaper_saturation"
    private const val AUTO_WALLPAPER_HUE = "auto_wallpaper_hue"
    private const val AUTO_WALLPAPER_HOME_BLUR = "auto_wallpaper_home_blur"
    private const val AUTO_WALLPAPER_HOME_BRIGHTNESS = "auto_wallpaper_home_brightness"
    private const val AUTO_WALLPAPER_HOME_CONTRAST = "auto_wallpaper_home_contrast"
    private const val AUTO_WALLPAPER_HOME_SATURATION = "auto_wallpaper_home_saturation"
    private const val AUTO_WALLPAPER_HOME_HUE = "auto_wallpaper_home_hue"
    private const val AUTO_WALLPAPER_LOCK_BLUR = "auto_wallpaper_lock_blur"
    private const val AUTO_WALLPAPER_LOCK_BRIGHTNESS = "auto_wallpaper_lock_brightness"
    private const val AUTO_WALLPAPER_LOCK_CONTRAST = "auto_wallpaper_lock_contrast"
    private const val AUTO_WALLPAPER_LOCK_SATURATION = "auto_wallpaper_lock_saturation"
    private const val AUTO_WALLPAPER_LOCK_HUE = "auto_wallpaper_lock_hue"

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

    fun getGenerateMD5(): Boolean {
        return getSharedPreferences().getBoolean(GENERATE_MD5, false)
    }

    fun setGenerateMD5(value: Boolean) {
        getSharedPreferences().edit().putBoolean(GENERATE_MD5, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getShowImageShadow(): Boolean {
        return getSharedPreferences().getBoolean(IMAGE_SHADOW, true)
    }

    fun setShowImageShadow(value: Boolean) {
        getSharedPreferences().edit().putBoolean(IMAGE_SHADOW, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun isLockSourceSet(): Boolean {
        return getSharedPreferences().getBoolean(IS_LOCK_SOURCE_SET, false)
    }

    fun setIsLockSourceSet(value: Boolean) {
        getSharedPreferences().edit().putBoolean(IS_LOCK_SOURCE_SET, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun isHomeSourceSet(): Boolean {
        return getSharedPreferences().getBoolean(IS_HOME_SOURCE_SET, false)
    }

    fun setIsHomeSourceSet(value: Boolean) {
        getSharedPreferences().edit().putBoolean(IS_HOME_SOURCE_SET, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getLockTagId(): String? {
        return getSharedPreferences().getString(LOCK_TAG_ID, null)
    }

    fun setLockTagId(value: String?) {
        getSharedPreferences().edit().putString(LOCK_TAG_ID, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getLockFolderId(): Int {
        return getSharedPreferences().getInt(LOCK_FOLDER_ID, 0)
    }

    fun setLockFolderId(value: Int) {
        getSharedPreferences().edit().putInt(LOCK_FOLDER_ID, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getLockFolderName(): String? {
        return getSharedPreferences().getString(LOCK_FOLDER_NAME, null)
    }

    fun setLockFolderName(value: String?) {
        getSharedPreferences().edit().putString(LOCK_FOLDER_NAME, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getHomeTagId(): String? {
        return getSharedPreferences().getString(HOME_TAG_ID, null)
    }

    fun setHomeTagId(value: String?) {
        getSharedPreferences().edit().putString(HOME_TAG_ID, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getHomeFolderId(): Int {
        return getSharedPreferences().getInt(HOME_FOLDER_ID, 0)
    }

    fun setHomeFolderId(value: Int) {
        getSharedPreferences().edit().putInt(HOME_FOLDER_ID, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getHomeFolderName(): String? {
        return getSharedPreferences().getString(HOME_FOLDER_NAME, null)
    }

    fun setHomeFolderName(value: String?) {
        getSharedPreferences().edit().putString(HOME_FOLDER_NAME, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getLastLockWallpaperPosition(): Int {
        return getSharedPreferences().getInt(LAST_LOCK_WALLPAPER_POSITION, 0)
    }

    fun setLastLockWallpaperPosition(value: Int) {
        getSharedPreferences().edit().putInt(LAST_LOCK_WALLPAPER_POSITION, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getLastHomeWallpaperPosition(): Int {
        return getSharedPreferences().getInt(LAST_HOME_WALLPAPER_POSITION, 0)
    }

    fun setLastHomeWallpaperPosition(value: Int) {
        getSharedPreferences().edit().putInt(LAST_HOME_WALLPAPER_POSITION, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperBlur(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_BLUR, 0f)
    }

    fun setAutoWallpaperBlur(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_BLUR, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperBrightness(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_BRIGHTNESS, 0f)
    }

    fun setAutoWallpaperBrightness(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_BRIGHTNESS, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperContrast(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_CONTRAST, 1f)
    }

    fun setAutoWallpaperContrast(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_CONTRAST, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperSaturation(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_SATURATION, 1f)
    }

    fun setAutoWallpaperSaturation(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_SATURATION, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperHue(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_HUE, 0f)
    }

    fun setAutoWallpaperHue(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_HUE, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperHomeBlur(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_HOME_BLUR, 0f)
    }

    fun setAutoWallpaperHomeBlur(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_HOME_BLUR, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperHomeBrightness(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_HOME_BRIGHTNESS, 0f)
    }

    fun setAutoWallpaperHomeBrightness(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_HOME_BRIGHTNESS, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperHomeContrast(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_HOME_CONTRAST, 1f)
    }

    fun setAutoWallpaperHomeContrast(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_HOME_CONTRAST, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperHomeSaturation(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_HOME_SATURATION, 1f)
    }

    fun setAutoWallpaperHomeSaturation(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_HOME_SATURATION, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperHomeHue(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_HOME_HUE, 0f)
    }

    fun setAutoWallpaperHomeHue(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_HOME_HUE, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperLockBlur(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_LOCK_BLUR, 0f)
    }

    fun setAutoWallpaperLockBlur(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_LOCK_BLUR, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperLockBrightness(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_LOCK_BRIGHTNESS, 0f)
    }

    fun setAutoWallpaperLockBrightness(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_LOCK_BRIGHTNESS, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperLockContrast(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_LOCK_CONTRAST, 1f)
    }

    fun setAutoWallpaperLockContrast(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_LOCK_CONTRAST, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperLockSaturation(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_LOCK_SATURATION, 1f)
    }

    fun setAutoWallpaperLockSaturation(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_LOCK_SATURATION, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun getAutoWallpaperLockHue(): Float {
        return getSharedPreferences().getFloat(AUTO_WALLPAPER_LOCK_HUE, 0f)
    }

    fun setAutoWallpaperLockHue(value: Float) {
        getSharedPreferences().edit().putFloat(AUTO_WALLPAPER_LOCK_HUE, value).apply()
    }

    // ----------------------------------------------------------------------------------------------------- //

    fun clearAutoWallpaperValues() {
        getSharedPreferences().edit().remove(AUTO_WALLPAPER_BLUR).apply()
        getSharedPreferences().edit().remove(AUTO_WALLPAPER_BRIGHTNESS).apply()
        getSharedPreferences().edit().remove(AUTO_WALLPAPER_CONTRAST).apply()
        getSharedPreferences().edit().remove(AUTO_WALLPAPER_HOME_BLUR).apply()
        getSharedPreferences().edit().remove(AUTO_WALLPAPER_HOME_BRIGHTNESS).apply()
        getSharedPreferences().edit().remove(AUTO_WALLPAPER_HOME_CONTRAST).apply()
        getSharedPreferences().edit().remove(AUTO_WALLPAPER_LOCK_BLUR).apply()
        getSharedPreferences().edit().remove(AUTO_WALLPAPER_LOCK_BRIGHTNESS).apply()
        getSharedPreferences().edit().remove(AUTO_WALLPAPER_LOCK_CONTRAST).apply()
    }
}
