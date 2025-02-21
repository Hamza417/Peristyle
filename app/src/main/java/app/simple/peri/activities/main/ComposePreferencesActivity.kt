package app.simple.peri.activities.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.simple.peri.extensions.BaseComponentActivity
import app.simple.peri.preferences.SharedPreferences
import app.simple.peri.ui.screens.Settings
import app.simple.peri.ui.theme.PeristyleTheme

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
