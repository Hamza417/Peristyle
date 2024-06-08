package app.simple.peri.activities

import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import app.simple.peri.R
import app.simple.peri.databinding.ActivityMainBinding
import app.simple.peri.models.Wallpaper
import app.simple.peri.ui.WallpaperScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WallpaperAssociationActivity : AppCompatActivity() {

    private var activityMainBinding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding?.root)
        makeAppFullScreen()

        lifecycleScope.launch(Dispatchers.IO) {
            val wallpaper = Wallpaper()
            val documentFile = DocumentFile.fromSingleUri(this@WallpaperAssociationActivity, intent.data!!)

            wallpaper.uri = intent.data.toString()
            wallpaper.name = documentFile?.name
            wallpaper.dateModified = documentFile?.lastModified()!!
            wallpaper.size = documentFile.length()

            contentResolver.openInputStream(documentFile.uri)?.use { inputStream ->
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(inputStream, null, options)
                wallpaper.width = options.outWidth
                wallpaper.height = options.outHeight
            }

            withContext(Dispatchers.Main) {
                supportFragmentManager.beginTransaction()
                    .add(R.id.mainContainer, WallpaperScreen.newInstance(wallpaper), WallpaperScreen.TAG)
                    .commit()
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
}
