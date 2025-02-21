package app.simple.peri.extensions

import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity

abstract class BaseComponentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                                   .detectAll()
                                   .penaltyLog()
                                   .build())
    }
}
