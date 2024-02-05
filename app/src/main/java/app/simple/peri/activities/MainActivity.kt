package app.simple.peri.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import app.simple.peri.R
import app.simple.peri.databinding.ActivityMainBinding
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.preferences.SharedPreferences
import app.simple.peri.services.AutoWallpaperService
import app.simple.peri.ui.MainScreen
import app.simple.peri.utils.ConditionUtils.isNull
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity(), android.content.SharedPreferences.OnSharedPreferenceChangeListener {

    private var binding: ActivityMainBinding? = null
    private var biometricPrompt: BiometricPrompt? = null
    private var biometricPromptInfo: BiometricPrompt.PromptInfo? = null

    private val modeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

    private val storageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result != null) {
            val uri = result.data?.data
            if (uri != null) {
                contentResolver.takePersistableUriPermission(uri, modeFlags)
                MainPreferences.setStorageUri(uri.toString())
                Log.d("MainActivity", "Storage Uri: $uri")

                binding?.mainContainer?.id?.let {
                    supportFragmentManager.beginTransaction()
                        .replace(it, MainScreen.newInstance())
                        .commit()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferences.init(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        makeAppFullScreen()

        if (contentResolver.persistedUriPermissions.isNotEmpty()) {
            setAutoWallpaperAlarm()
        }

        biometricPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.app_name))
            .setDescription(getString(R.string.biometric_desc))
            .setNegativeButtonText(getString(R.string.close))
            .build()

        if (MainPreferences.getStorageUri() == null) {
            Log.d("MainActivity", "Storage Uri: no permission")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            storageResult.launch(intent)
        } else {
            Log.d("MainActivity", "Storage Uri: ${MainPreferences.getStorageUri()}")
            if (contentResolver.persistedUriPermissions.isNotEmpty()) {
                if (savedInstanceState.isNull()) {
                    if (MainPreferences.isBiometric()) {
                        biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                super.onAuthenticationError(errorCode, errString)
                                when (errorCode) {
                                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                                        finish()
                                    }

                                    BiometricPrompt.ERROR_NO_BIOMETRICS,
                                    BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                                        binding?.mainContainer?.id?.let {
                                            supportFragmentManager.beginTransaction()
                                                .replace(it, MainScreen.newInstance())
                                                .commit()
                                        }
                                    }

                                    else -> {
                                        MaterialAlertDialogBuilder(this@MainActivity)
                                            .setTitle(getString(R.string.app_name))
                                            .setMessage(errString)
                                            .setPositiveButton(getString(R.string.close)) { _, _ ->
                                                finish()
                                            }
                                            .show()
                                    }
                                }
                            }

                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                super.onAuthenticationSucceeded(result)
                                Log.d("MainActivity", "Biometric: success")
                                binding?.mainContainer?.id?.let {
                                    supportFragmentManager.beginTransaction()
                                        .replace(it, MainScreen.newInstance())
                                        .commit()
                                }
                            }

                            override fun onAuthenticationFailed() {
                                super.onAuthenticationFailed()
                                Log.d("MainActivity", "Biometric: failed")
                            }
                        })

                        biometricPrompt?.authenticate(biometricPromptInfo!!)
                    } else {
                        binding?.mainContainer?.id?.let {
                            supportFragmentManager.beginTransaction()
                                .replace(it, MainScreen.newInstance())
                                .commit()
                        }
                    }
                }
            } else {
                Log.d("MainActivity", "Storage Uri: no permission")
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                storageResult.launch(intent)
            }
        }
    }

    private fun makeAppFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // Disable navigation bar contrast
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = Color.TRANSPARENT
        }
    }

    private fun setAutoWallpaperAlarm() {
        if (MainPreferences.getAutoWallpaperInterval().toInt() > 0) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AutoWallpaperService::class.java)
            val pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            val interval = MainPreferences.getAutoWallpaperInterval().toInt()
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval.toLong(), pendingIntent)
            Log.d("MainActivity", "Auto wallpaper alarm set for every ${MainPreferences.getAutoWallpaperInterval()} minutes")
        } else {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            val intent = Intent(this, AutoWallpaperService::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            alarmManager.cancel(pendingIntent)
            Log.d("MainActivity", "Auto wallpaper alarm cancelled")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: android.content.SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.autoWallpaperInterval -> {
                setAutoWallpaperAlarm()
            }
        }
    }
}
