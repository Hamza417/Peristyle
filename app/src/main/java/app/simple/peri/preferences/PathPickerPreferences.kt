package app.simple.peri.preferences

object PathPickerPreferences {

    private const val LAST_PICKED_PATH = "last_picked_path"

    fun getLastPickedPathKey(): String {
        return LAST_PICKED_PATH
    }

    fun getLastPickedPath(): String {
        return SharedPreferences.getSharedPreferences().getString(LAST_PICKED_PATH, "") ?: ""
    }

    fun setLastPickedPath(path: String) {
        SharedPreferences.getSharedPreferences().edit().putString(LAST_PICKED_PATH, path).apply()
    }
}