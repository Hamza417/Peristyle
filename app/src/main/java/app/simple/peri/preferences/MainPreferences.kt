package app.simple.peri.preferences

object MainPreferences {

    private const val storageUri = "storageUri"

    fun getStorageUri(): String? {
        return SharedPreferences.getSharedPreferences().getString(storageUri, null)
    }

    fun setStorageUri(uri: String) {
        SharedPreferences.getSharedPreferences().edit().putString(storageUri, uri).apply()
    }
}