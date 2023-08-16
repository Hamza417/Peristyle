package app.simple.peri.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import app.simple.peri.R
import app.simple.peri.databinding.ActivityMainBinding
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.preferences.SharedPreferences
import app.simple.peri.ui.MainScreen
import app.simple.peri.utils.ConditionUtils.isNull
import com.google.android.material.color.DynamicColors

class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null

    private val storageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result != null) {
            val uri = result.data?.data
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
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

    override fun attachBaseContext(newBase: Context?) {
        SharedPreferences.init(newBase!!)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This is all you need.
        DynamicColors.applyToActivitiesIfAvailable(application)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        makeAppFullScreen()

        if (MainPreferences.getStorageUri() == null) {
            Log.d("MainActivity", "Storage Uri: no permission")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            storageResult.launch(intent)
        } else {
            Log.d("MainActivity", "Storage Uri: ${MainPreferences.getStorageUri()}")
            if (contentResolver.persistedUriPermissions.isNotEmpty()) {
                if (savedInstanceState.isNull()) {
                    binding?.mainContainer?.id?.let {
                        supportFragmentManager.beginTransaction()
                            .replace(it, MainScreen.newInstance())
                            .commit()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = Color.TRANSPARENT
        }
    }
}
