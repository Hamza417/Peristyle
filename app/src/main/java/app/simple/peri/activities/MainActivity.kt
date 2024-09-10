package app.simple.peri.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import app.simple.peri.compose.nav.PeristyleNavigation
import app.simple.peri.compose.theme.PeristyleTheme
import app.simple.peri.preferences.SharedPreferences

class MainActivity : ComponentActivity() {

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
}
