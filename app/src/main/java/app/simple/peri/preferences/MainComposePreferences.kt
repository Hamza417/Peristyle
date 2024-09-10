package app.simple.peri.preferences

import app.simple.peri.preferences.SharedPreferences.getSharedPreferences

object MainComposePreferences {

    private const val GRID_SPAN_COUNT = "grid_span_count"

    // ----------------------------------------------------------------------------------------------------- //

    fun getGridSpanCount(): Int {
        return getSharedPreferences().getInt(GRID_SPAN_COUNT, 2)
    }

    fun setGridSpanCount(value: Int) {
        getSharedPreferences().edit().putInt(GRID_SPAN_COUNT, value).apply()
    }
}
