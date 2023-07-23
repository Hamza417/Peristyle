package app.simple.peri.ui

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceFragmentCompat
import app.simple.peri.R
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.viewmodels.WallpaperViewModel
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Preferences : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var wallpaperViewModel: WallpaperViewModel
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        requireActivity().findViewById<CoordinatorLayout>(R.id.mainContainer)
            .setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.mainBackground))
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        wallpaperViewModel = ViewModelProvider(requireActivity()).get(WallpaperViewModel::class.java)

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

        preferenceScreen.findPreference<androidx.preference.Preference>("recreate_database")?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.recreate_database_message)
                .setPositiveButton(R.string.yes) { dialog, _ ->
                    wallpaperViewModel.recreateDatabase()
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.close) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            true
        }

        preferenceScreen.findPreference<androidx.preference.Preference>("clear_cache")?.setOnPreferenceClickListener {
            // Create a background thread
            lifecycleScope.launch(Dispatchers.IO) {
                Glide.get(requireContext()).clearDiskCache()
            }

            Glide.get(requireContext()).clearMemory()

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

            "blur" -> {
                MainPreferences.setBlur(p0?.getBoolean(p1, true)!!)
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