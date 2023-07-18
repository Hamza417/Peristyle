package app.simple.waller.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import app.simple.waller.databinding.ActivityMainBinding
import app.simple.waller.preferences.MainPreferences
import app.simple.waller.preferences.SharedPreferences
import app.simple.waller.ui.MainScreen
import app.simple.waller.utils.ConditionUtils.isNull

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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        makeAppFullScreen()

        if (MainPreferences.getStorageUri() == null) {
            Log.d("MainActivity", "Storage Uri: no permission")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            storageResult.launch(intent)
        } else {
            Log.d("MainActivity", "Storage Uri: ${MainPreferences.getStorageUri()}")
            if (savedInstanceState.isNull()) {
                binding?.mainContainer?.id?.let {
                    supportFragmentManager.beginTransaction()
                        .replace(it, MainScreen.newInstance())
                        .commit()
                }
            }
        }
    }

    private fun makeAppFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = Color.TRANSPARENT
        }
    }
}
