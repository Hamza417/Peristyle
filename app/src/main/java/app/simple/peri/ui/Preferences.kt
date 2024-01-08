package app.simple.peri.ui

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import app.simple.peri.R
import app.simple.peri.constants.BundleConstants
import app.simple.peri.databinding.DialogDeleteBinding
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.FileUtils.toSize
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class Preferences : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    private val storageResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
        if (result != null) {
            val uri = result.data?.data
            if (uri != null) {
                requireActivity().contentResolver.persistedUriPermissions.forEach {
                    requireActivity().contentResolver.releasePersistableUriPermission(it.uri, flags)
                }
                MainPreferences.setStorageUri(null)
                requireActivity().contentResolver.takePersistableUriPermission(uri, flags)
                MainPreferences.setStorageUri(uri.toString())
                Log.d("Preferences", "Storage Uri: $uri")
                LocalBroadcastManager.getInstance(requireContext())
                    .sendBroadcast(Intent().apply {
                        action = BundleConstants.INTENT_RECREATE_DATABASE
                    })
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        requireActivity().findViewById<CoordinatorLayout>(R.id.mainContainer)
            .setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.mainBackground))

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        preferenceScreen.findPreference<Preference>("positional")?.setOnPreferenceClickListener {
            val url = "https://github.com/Hamza417/Positional"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
            true
        }

        preferenceScreen.findPreference<Preference>("inure")?.setOnPreferenceClickListener {
            val url = "https://github.com/Hamza417/Inure"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
            true
        }

        preferenceScreen.findPreference<Preference>("recreate_database")?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.recreate_database_message)
                .setPositiveButton(R.string.yes) { dialog, _ ->
                    LocalBroadcastManager.getInstance(requireContext())
                        .sendBroadcast(Intent().apply {
                            action = BundleConstants.INTENT_RECREATE_DATABASE
                        })
                    dialog.dismiss()
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(R.string.initiate_recreate_database_message)
                        .setPositiveButton(R.string.close) { dialog1, _ ->
                            dialog1.dismiss()
                        }
                        .show()
                }
                .setNegativeButton(R.string.close) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            true
        }

        preferenceScreen.findPreference<Preference>("clear_cache")?.setOnPreferenceClickListener {
            // Create a background thread
            lifecycleScope.launch(Dispatchers.IO) {
                val imagesCachePath = File("${requireContext().cacheDir}/image_manager_disk_cache/")
                val size = imagesCachePath.walkTopDown().sumOf { it.length() }

                Glide.get(requireContext()).clearDiskCache()

                requireActivity().runOnUiThread {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(getString(R.string.clear_cache_message, size.toSize()))
                        .setPositiveButton(R.string.close) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }

            Glide.get(requireContext()).clearMemory()

            true
        }

        preferenceScreen.findPreference<Preference>("change_directory")?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(getString(R.string.change_directory_desc, MainPreferences.getStorageUri()))
                .setPositiveButton(R.string.change_directory) { dialog, _ ->
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    storageResult.launch(intent)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.close) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
            true
        }

        preferenceScreen.findPreference<Preference>("library_stats")?.setOnPreferenceClickListener {
            val dialogDeleteBinding = DialogDeleteBinding.inflate(layoutInflater)
            dialogDeleteBinding.progress.text = getString(R.string.fetching)

            val progressDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.library_stats)
                .setView(dialogDeleteBinding.root)
                .show()

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.Main) {
                    dialogDeleteBinding.progress.text = getString(R.string.total_info)
                }

                val totalWallpapers = DocumentFile.fromTreeUri(requireContext(), Uri.parse(MainPreferences.getStorageUri()))?.listFiles()?.size

                withContext(Dispatchers.Main) {
                    dialogDeleteBinding.progress.text = getString(R.string.size_info)
                }

                val size = DocumentFile.fromTreeUri(requireContext(), Uri.parse(MainPreferences.getStorageUri()))?.listFiles()?.sumOf { it.length() }!!

                withContext(Dispatchers.Main) {
                    dialogDeleteBinding.progress.text = getString(R.string.cache_info)
                }

                val cacheSize = File("${requireContext().cacheDir}/image_manager_disk_cache/").walkTopDown().sumOf { it.length() }

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()

                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(getString(R.string.library_stats_message, totalWallpapers, size.toSize(), cacheSize.toSize()))
                        .setPositiveButton(R.string.close) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            true
        }

        preferenceScreen.findPreference<Preference>("change_directory")?.summary =
            getString(R.string.change_directory_desc, MainPreferences.getStorageUri())
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
                MainPreferences.setGridSpan(p0?.getString(p1, MainPreferences.SPAN_RANDOM.toString())!!.toInt())
            }

            "blur" -> {
                MainPreferences.setBlur(p0?.getBoolean(p1, true)!!)
            }

            "is_details" -> {
                MainPreferences.setDetails(p0?.getBoolean(p1, true)!!)
            }

            "is_app_engine" -> {
                MainPreferences.setAppEngine(p0?.getBoolean(p1, true)!!)
            }

            "is_biometric" -> {
                if (p0?.getBoolean(p1, false) == true) {
                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                        .setTitle(getString(R.string.app_name))
                        .setSubtitle(getString(R.string.biometric_subtitle))
                        .setNegativeButtonText(getString(R.string.close))
                        .build()

                    val biometricPrompt = BiometricPrompt(
                            this, ContextCompat.getMainExecutor(requireContext()),
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    super.onAuthenticationError(errorCode, errString)
                                    p0.edit().putBoolean(p1, false).apply()
                                    MainPreferences.setBiometric(false)

                                    MaterialAlertDialogBuilder(requireContext())
                                        .setMessage(errString.toString())
                                        .setPositiveButton(R.string.close) { dialog, _ ->
                                            preferenceScreen.findPreference<CheckBoxPreference>("is_biometric")?.isChecked = false
                                            dialog.dismiss()
                                        }
                                        .show()
                                }

                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    MainPreferences.setBiometric(boolean = true)

                                    MaterialAlertDialogBuilder(requireContext())
                                        .setMessage(R.string.biometric_success)
                                        .setPositiveButton(R.string.close) { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                        .show()
                                }

                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                    Log.d("Preferences", "Biometric: failed")
                                }
                            })

                    biometricPrompt.authenticate(promptInfo)
                } else {
                    preferenceScreen.findPreference<CheckBoxPreference>("is_biometric")?.isChecked = false
                    MainPreferences.setBiometric(false)
                    p0!!.edit().putBoolean(p1, false).apply()
                }
            }
            "remember_scroll_position" -> {
                MainPreferences.setRememberScrollPosition(p0?.getBoolean(p1, false)!!)
            }
        }
    }

    /**
     * Making the Navigation system bar not overlapping with the activity
     */
    @Suppress("unused")
    private fun fixNavigationBarOverlap() {
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            /**
             * Setting the bottom margin to the view to make it not overlap with the
             * navigation system bar
             */
            view.layoutParams = (view.layoutParams as MarginLayoutParams).apply {
                if (bottomMargin < insets.bottom.times(4)) {
                    bottomMargin = insets.bottom.times(4)
                }
            }

            /**
             * Return CONSUMED if you don't want want the window insets to keep being
             * passed down to descendant views.
             */
            WindowInsetsCompat.CONSUMED
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