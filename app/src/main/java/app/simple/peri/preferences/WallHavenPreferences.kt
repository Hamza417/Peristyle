package app.simple.peri.preferences

import androidx.core.content.edit

object WallHavenPreferences {
    private const val WALLHAVEN_API_KEY = "wallhaven_api_key"

    fun setAPIKey(apiKey: String) {
        SharedPreferences.getSharedPreferences()
            .edit {
                putString(WALLHAVEN_API_KEY, apiKey)
            }
    }

    fun getAPIKey(): String? {
        return SharedPreferences.getSharedPreferences()
            .getString(WALLHAVEN_API_KEY, null)
    }
}