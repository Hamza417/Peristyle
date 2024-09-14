package app.simple.peri.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.simple.peri.compose.nav.PeristyleNavigation
import app.simple.peri.compose.theme.PeristyleTheme
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.preferences.SharedPreferences
import app.simple.peri.services.AutoWallpaperService

class MainComposeActivity : ComponentActivity(), OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferences.init(applicationContext)

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

    override fun onSharedPreferenceChanged(sharedPreferences: android.content.SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.AUTO_WALLPAPER_INTERVAL -> {
                setAutoWallpaperAlarm()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SharedPreferences.registerSharedPreferencesListener(this)
    }

    override fun onPause() {
        super.onPause()
        SharedPreferences.unregisterListener(this)
    }
}
