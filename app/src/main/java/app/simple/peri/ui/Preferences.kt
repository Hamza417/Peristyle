package app.simple.peri.ui

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import app.simple.peri.R
import app.simple.peri.preferences.MainPreferences

class Preferences : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        preferenceScreen.findPreference<androidx.preference.Preference>("positional")?.setOnPreferenceClickListener {
            val url = "https://github.com/Hamza417/Positional"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
            true
        }

        preferenceScreen.findPreference<androidx.preference.Preference>("inure")?.setOnPreferenceClickListener {
            val url = "https://github.com/Hamza417/Inure"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        when (p1) {
            MainPreferences.name -> {
                MainPreferences.setName(p0?.getBoolean(p1, true)!!)
            }

            "grid_span" -> {
                MainPreferences.setGridSpan(
                        p0?.getString(p1, "2")!!.toInt()
                            .coerceAtLeast(1)
                            .coerceAtMost(4))
            }
        }
    }

    companion object {
        fun newInstance(): Preferences {
            val args = Bundle()
            val fragment = Preferences()
            fragment.arguments = args
            return fragment
        }
    }
}