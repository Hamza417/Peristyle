package app.simple.peri.activities.main

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import app.simple.peri.BuildConfig
import app.simple.peri.compose.nav.PeristyleNavigation
import app.simple.peri.compose.theme.PeristyleTheme
import app.simple.peri.crash.CrashReport
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.extensions.BaseComponentActivity
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.preferences.SharedPreferences
import app.simple.peri.services.AutoWallpaperService
import app.simple.peri.viewmodels.ComposeWallpaperViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainComposeActivity : BaseComponentActivity(), OnSharedPreferenceChangeListener {

    private val composeWallpaperViewModel: ComposeWallpaperViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferences.init(applicationContext)
        keepScreenOn()
        CrashReport(this).initialize()

        setContent {
            PeristyleTheme {
                Surface(
                        modifier = Modifier.fillMaxSize()
                ) {
                    PeristyleNavigation(this)
                }
            }
        }
    }

    private fun setAutoWallpaperAlarm() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, AutoWallpaperService::class.java)
        val pendingIntent = PendingIntent.getService(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Cancel any existing alarms
        alarmManager.cancel(pendingIntent)

        if (MainPreferences.getAutoWallpaperInterval().toInt() > 0) {
            val interval = MainPreferences.getAutoWallpaperInterval().toInt()
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval.toLong(), pendingIntent)
            Log.d("MainActivity", "Auto wallpaper alarm set for every ${MainPreferences.getAutoWallpaperInterval()} ms")
        } else {
            Log.d("MainActivity", "Auto wallpaper alarm cancelled")
        }
    }

    private fun keepScreenOn() {
        if (BuildConfig.DEBUG) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: android.content.SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.AUTO_WALLPAPER_INTERVAL -> {
                setAutoWallpaperAlarm()
            }
            MainComposePreferences.LOCK_TAG_ID,
            MainComposePreferences.LOCK_FOLDER_ID -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    WallpaperDatabase.wipeLastRandomLockWallpapersDatabase(applicationContext)
                }
            }
            MainComposePreferences.HOME_TAG_ID,
            MainComposePreferences.HOME_FOLDER_ID -> {
                lifecycleScope.launch(Dispatchers.IO) {
                    WallpaperDatabase.wipeLastRandomHomeWallpapersDatabase(applicationContext)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SharedPreferences.registerSharedPreferencesListener(this)
        composeWallpaperViewModel.refresh()
    }

    override fun onPause() {
        super.onPause()
        SharedPreferences.unregisterListener(this)
    }
}
