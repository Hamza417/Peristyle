package app.simple.peri.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import app.simple.peri.utils.ConditionUtils.isNull

object SharedPreferences {

    private const val preferences = "Preferences"
    private var sharedPreferences: SharedPreferences? = null

    fun init(context: Context) {
        if (sharedPreferences.isNull()) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        }
    }

    /**
     * Singleton to hold reference of SharedPreference.
     * Call [init] first before making a instance request
     *
     * @see init
     * @throws NullPointerException
     */
    fun getSharedPreferences(): SharedPreferences {
        return sharedPreferences ?: throw NullPointerException()
    }

    fun registerSharedPreferencesListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Use this function to register shared preference change listener if
     * the current context has [SharedPreferences.OnSharedPreferenceChangeListener]
     * implemented.
     */
    fun SharedPreferences.OnSharedPreferenceChangeListener.registerSharedPreferenceChangeListener() {
        registerSharedPreferencesListener(this)
    }

    /**
     * Use this function to unregister shared preference change listener if
     * the current context has [SharedPreferences.OnSharedPreferenceChangeListener]
     * implemented.
     */
    fun SharedPreferences.OnSharedPreferenceChangeListener.unregisterSharedPreferenceChangeListener() {
        unregisterListener(this)
    }

    /**
     * Singleton to hold reference of SharedPreference.
     *
     * @see init
     */
    fun getSharedPreferences(context: Context): SharedPreferences {
        kotlin.runCatching {
            return sharedPreferences ?: throw NullPointerException()
        }.getOrElse {
            init(context)
            return sharedPreferences!!
        }
    }

    fun getSharedPreferencesPath(context: Context): String {
        return context.applicationInfo.dataDir + "/shared_prefs/" + preferences + ".xml"
    }
}
