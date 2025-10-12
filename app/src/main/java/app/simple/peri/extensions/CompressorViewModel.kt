package app.simple.peri.extensions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.utils.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

abstract class CompressorViewModel(application: Application) : AndroidViewModel(application) {
    fun compressWallpaper(wallpaper: Wallpaper, onSuccess: (Wallpaper) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = wallpaper.filePath.toFile()

            // Compress the file using optimized custom compressor
            ImageCompressor.compressImage(
                    sourceFile = file,
                    destinationFile = file,
                    quality = 60,
                    targetWidth = wallpaper.width!!,
                    targetHeight = wallpaper.height!!
            )

            val wallpaper1 = onCompressionDone(wallpaper, file)

            withContext(Dispatchers.Main) {
                onSuccess(wallpaper1)
            }
        }
    }

    fun reduceResolution(wallpaper: Wallpaper, onSuccess: (Wallpaper) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = wallpaper.filePath.toFile()

            // Reduce resolution using optimized custom reducer
            ImageCompressor.reduceResolution(
                    sourceFile = file,
                    destinationFile = file,
                    scaleFactor = 0.5f,
                    quality = 100
            )

            val wallpaper1 = onCompressionDone(wallpaper, file)

            withContext(Dispatchers.Main) {
                onSuccess(wallpaper1)
            }
        }
    }

    abstract suspend fun onCompressionDone(wallpaper: Wallpaper, file: File): Wallpaper
}
