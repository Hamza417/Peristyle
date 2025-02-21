package app.simple.peri.activities.association

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import app.simple.peri.extensions.BaseComponentActivity
import app.simple.peri.preferences.SharedPreferences
import app.simple.peri.ui.screens.Wallpaper
import app.simple.peri.ui.theme.PeristyleTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import app.simple.peri.models.Wallpaper as ModelWallpaper

class WallpaperAssociationActivity : BaseComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedPreferences.init(applicationContext)

        lifecycleScope.launch(Dispatchers.IO) {
            val wallpaper = ModelWallpaper()
            contentResolver.openInputStream(intent.data!!)?.use { inputStream ->
                val documentFile = DocumentFile.fromSingleUri(this@WallpaperAssociationActivity, intent.data!!)
                wallpaper.filePath = copyFileToCache(inputStream, documentFile!!.name!!).absolutePath
            }

            wallpaper.uri = intent.data.toString()
            wallpaper.name = wallpaper.getFile().name
            wallpaper.dateModified = wallpaper.getFile().lastModified()
            wallpaper.size = wallpaper.getFile().length()

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(wallpaper.filePath, options)
            wallpaper.width = options.outWidth
            wallpaper.height = options.outHeight

            withContext(Dispatchers.Main) {
                setContent {
                    val navController = rememberNavController()
                    PeristyleTheme {
                        Surface(
                                modifier = Modifier.fillMaxSize()
                        ) {
                            Wallpaper(navController, wallpaper)
                        }
                    }
                }
            }
        }
    }

    private fun copyFileToCache(inputStream: InputStream, fileName: String): File {
        val cacheDir = cacheDir
        val cacheFile = File(cacheDir, fileName)
        inputStream.use { input ->
            FileOutputStream(cacheFile).use { output ->
                input.copyTo(output)
            }
        }

        return cacheFile
    }
}
