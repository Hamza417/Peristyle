package app.simple.peri.preferences

import app.simple.peri.preferences.SharedPreferences.getSharedPreferences

object MainComposePreferences {

    private const val GRID_SPAN_COUNT = "grid_span_count"
    private const val WARNING_INDICATOR = "warning_indicator"
    private const val IMAGE_SHADOW = "image_shadow"

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

}
