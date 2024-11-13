package app.simple.peri.activities.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.simple.peri.compose.screens.Settings
import app.simple.peri.compose.theme.PeristyleTheme
import app.simple.peri.extensions.BaseComponentActivity
import app.simple.peri.preferences.SharedPreferences

class ComposePreferencesActivity : BaseComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferences.init(applicationContext)

        setContent {
            PeristyleTheme {
                Surface(
                        modifier = Modifier.fillMaxSize()
                ) {
                    Settings()
                }
            }
        }
    }
}
