package app.simple.peri.ui

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import app.simple.peri.BuildConfig
import app.simple.peri.R
import app.simple.peri.activities.LegacyActivity
import app.simple.peri.activities.MainComposeActivity
import app.simple.peri.constants.BundleConstants
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.databinding.DialogDeleteBinding
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.FileUtils.listCompleteFiles
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.utils.PermissionUtils
import app.simple.peri.utils.PermissionUtils.isBatteryOptimizationDisabled
import app.simple.peri.utils.PermissionUtils.requestIgnoreBatteryOptimizations
import app.simple.peri.utils.ScreenUtils
import app.simple.peri.utils.ViewUtils.firstChild
import app.simple.peri.utils.ViewUtils.setPaddingBottom
import app.simple.peri.utils.ViewUtils.setPaddingTop
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class Preferences : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    private val storageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (uri != null) {
                requireActivity().contentResolver.persistedUriPermissions.forEach {
                    requireActivity().contentResolver.releasePersistableUriPermission(it.uri, flags)
                }
                MainPreferences.setStorageUri(null)
                requireActivity().contentResolver.takePersistableUriPermission(uri, flags)
                MainPreferences.setStorageUri(uri.toString())
                recreateDatabase()
            }
        }

    private var requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.forEach {
            when (it.key) {
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    if (it.value) {
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage(R.string.permission_granted)
                            .setPositiveButton(R.string.close) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        requireActivity().findViewById<CoordinatorLayout>(R.id.mainContainer)
            .setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.mainBackground))

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fixNavigationBarOverlap()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        preferenceScreen.findPreference<Preference>("positional")?.setOnPreferenceClickListener {
            val list = listOf("GitHub", "Play Store")

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.positional)
                .setItems(list.toTypedArray()) { dialog, which ->
                    val url = when (which) {
                        0 -> POSITIONAL_GITHUB_URL
                        1 -> POSITIONAL_PLAY_STORE_URL
                        else -> POSITIONAL_GITHUB_URL
                    }
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                    dialog.dismiss()
                }
                .show()

            true
        }

        preferenceScreen.findPreference<Preference>("inure")?.setOnPreferenceClickListener {
            val list = listOf("GitHub", "Play Store", "F-Droid")

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.inure_app_manager)
                .setItems(list.toTypedArray()) { dialog, which ->
                    val url = when (which) {
                        0 -> INURE_GITHUB_URL
                        1 -> INURE_PLAY_STORE_URL
                        2 -> INURE_F_DROID_URL
                        else -> INURE_GITHUB_URL
                    }

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                    dialog.dismiss()
                }
                .show()

            true
        }

        preferenceScreen.findPreference<Preference>("telegram")?.setOnPreferenceClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://t.me/peristyle_app")
            startActivity(intent)
            true
        }

        preferenceScreen.findPreference<Preference>("recreate_database")?.setOnPreferenceClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setMessage(R.string.recreate_database_message)
                .setPositiveButton(R.string.yes) { dialog, _ ->
                    recreateDatabase()
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

                val totalWallpapers = DocumentFile.fromTreeUri(
                        requireContext(), Uri.parse(MainPreferences.getStorageUri()))?.listFiles()?.size

                withContext(Dispatchers.Main) {
                    dialogDeleteBinding.progress.text = getString(R.string.size_info)
                }

                val size = DocumentFile.fromTreeUri(requireContext(), Uri.parse(MainPreferences.getStorageUri()))
                    ?.listCompleteFiles()?.toList()?.parallelStream()?.mapToLong { it.length() }?.sum()

                withContext(Dispatchers.Main) {
                    dialogDeleteBinding.progress.text = getString(R.string.cache_info)
                }

                val cacheSize = File("${requireContext().cacheDir}/image_manager_disk_cache/").walkTopDown().sumOf {
                    it.length()
                }

                val displayWidth = ScreenUtils.getScreenSize(requireContext()).width
                val displayHeight = ScreenUtils.getScreenSize(requireContext()).height
                Log.d("Preferences", "Display width: $displayWidth, Display height: $displayHeight")
                val wallpaperDatabase = WallpaperDatabase.getInstance(requireContext())?.wallpaperDao()

                val normalWallpapers = wallpaperDatabase?.getWallpapersByWidthAndHeight(displayWidth, displayHeight)?.size
                val inadequateWallpapers = wallpaperDatabase?.getInadequateWallpapers(displayWidth, displayHeight)?.size
                val excessiveWallpapers = wallpaperDatabase?.getExcessivelyLargeWallpapers(displayWidth, displayHeight)?.size

                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()

                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(
                                getString(R.string.library_stats_message,
                                          totalWallpapers,
                                          size?.toSize(),
                                          cacheSize.toSize(),
                                          normalWallpapers,
                                          inadequateWallpapers,
                                          excessiveWallpapers))
                        .setPositiveButton(R.string.close) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
            true
        }

        preferenceScreen.findPreference<Preference>("external_storage")?.setOnPreferenceClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(R.string.permission_granted)
                        .setPositiveButton(R.string.close) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    requestManageExternalStoragePermission()
                }
            } else {
                if (PermissionUtils.checkStoragePermission(requireContext())) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setMessage(R.string.permission_granted)
                        .setPositiveButton(R.string.close) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    requestPermissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                }
            }

            true
        }

        preferenceScreen.findPreference<Preference>("battery_optimization")?.setOnPreferenceClickListener {
            if (requireContext().isBatteryOptimizationDisabled()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(R.string.permission_granted)
                    .setPositiveButton(R.string.close) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                requireContext().requestIgnoreBatteryOptimizations()
            }
            true
        }

        preferenceScreen.findPreference<Preference>("change_directory")?.summary =
            getString(R.string.change_directory_desc, MainPreferences.getStorageUri())

        preferenceScreen.findPreference<Preference>("info")?.summary =
            getString(R.string.full_info, BuildConfig.VERSION_NAME)

        preferenceScreen.findPreference<SwitchPreferenceCompat>("legacy_interface")?.isChecked =
            requireContext().packageManager.getComponentEnabledSetting(
                    ComponentName(requireContext(), LegacyActivity::class.java)
            ) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED

        preferenceScreen.findPreference<SwitchPreferenceCompat>("legacy_interface")?.setOnPreferenceChangeListener { _, newValue ->
            val newState = when {
                newValue as Boolean -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                else -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }

            val composeState = when {
                newValue -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                else -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            }

            requireContext().packageManager.setComponentEnabledSetting(
                    ComponentName(requireContext(), LegacyActivity::class.java), newState, PackageManager.DONT_KILL_APP)

            requireContext().packageManager.setComponentEnabledSetting(
                    ComponentName(requireContext(), MainComposeActivity::class.java), composeState, PackageManager.DONT_KILL_APP)

            true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, p1: String?) {
        when (p1) {
            MainPreferences.IS_BIOMETRIC -> {
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

                                    MaterialAlertDialogBuilder(requireContext())
                                        .setMessage(errString.toString())
                                        .setPositiveButton(R.string.close) { dialog, _ ->
                                            preferenceScreen.findPreference<SwitchPreferenceCompat>("is_biometric")?.isChecked = false
                                            dialog.dismiss()
                                        }
                                        .show()
                                }

                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)

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
                    preferenceScreen.findPreference<SwitchPreferenceCompat>("is_biometric")?.isChecked = false
                    p0!!.edit().putBoolean(p1, false).apply()
                }
            }

            MainPreferences.AUTO_WALLPAPER_INTERVAL -> {
                if (MainPreferences.isAutoWallpaperIntervalEnabled()) {
                    if (requireContext().isBatteryOptimizationDisabled().invert()) {
                        requireContext().requestIgnoreBatteryOptimizations()
                    }
                }
            }

            MainPreferences.TWEAKS -> {
                when {
                    MainPreferences.isTweakOptionSelected(MainPreferences.IGNORE_SUB_DIRS) ||
                            MainPreferences.isTweakOptionSelected(MainPreferences.IGNORE_DOT_FILES) -> {
                        recreateDatabase()
                    }
                }
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

            view.firstChild()?.apply {
                setPaddingTop(insets.top + paddingTop)
                setPaddingBottom(insets.bottom + paddingBottom)
                with(this as ViewGroup) {
                    clipToPadding = false
                    clipChildren = false
                }
            }

            /**
             * Return CONSUMED if you don't want want the window insets to keep being
             * passed down to descendant views.
             */
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun recreateDatabase() {
        LocalBroadcastManager.getInstance(requireContext())
            .sendBroadcast(Intent().apply {
                action = BundleConstants.INTENT_RECREATE_DATABASE
            })
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestManageExternalStoragePermission() {
        try {
            val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri))
        } catch (ignored: ActivityNotFoundException) {

        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {

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

        //     <string name="inure_url"></string>
        //    <string name="positional_url">https://github.com/Hamza417/Positional</string>

        private const val INURE_GITHUB_URL = "https://github.com/Hamza417/Inure"
        private const val INURE_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=app.simple.inure.play"
        private const val INURE_F_DROID_URL = "https://f-droid.org/packages/app.simple.inure"

        private const val POSITIONAL_GITHUB_URL = "https://github.com/Hamza417/Positional"
        private const val POSITIONAL_PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=app.simple.positional"
    }
}
