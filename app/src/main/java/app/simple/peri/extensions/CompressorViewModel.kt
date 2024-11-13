package app.simple.peri.extensions

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.FileUtils.toFile
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

abstract class CompressorViewModel(application: Application) : AndroidViewModel(application) {
    fun compressWallpaper(wallpaper: Wallpaper, onSuccess: (Wallpaper) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = wallpaper.filePath.toFile()

            // Compress the file
            Compressor.compress(getApplication(), file) {
                destination(file)
                quality(60)
                resolution(wallpaper.width!!, wallpaper.height!!)
                format(getFormat(wallpaper.name!!))
            }

            val wallpaper1 = onCompressionDone(wallpaper, file)

            withContext(Dispatchers.Main) {
                onSuccess(wallpaper1)
            }

            clearResidue()
        }
    }

    fun reduceResolution(wallpaper: Wallpaper, onSuccess: (Wallpaper) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val file = wallpaper.filePath.toFile()

            // Compress the file
            Compressor.compress(context, file) {
                destination(file)
                quality(100)
                resolution(wallpaper.width!!.div(2), wallpaper.height!!.div(2))
                format(getFormat(wallpaper.name!!))
            }

            val wallpaper1 = onCompressionDone(wallpaper, file)

            withContext(Dispatchers.Main) {
                onSuccess(wallpaper1)
            }

            clearResidue()
        }
    }

    private fun getFormat(name: String): Bitmap.CompressFormat {
        @Suppress("DEPRECATION")
        return when (name.lowercase().substringAfterLast(".")) {
            "jpg" -> Bitmap.CompressFormat.JPEG
            "jpeg" -> Bitmap.CompressFormat.JPEG
            "png" -> Bitmap.CompressFormat.PNG
            else -> Bitmap.CompressFormat.WEBP
        }
    }

    private fun clearResidue() {
        val cacheDir = getApplication<Application>().cacheDir
        cacheDir.listFiles()?.forEach {
            if (it.absolutePath.contains("/compressor/")) {
                it.delete()
            }
        }
    }

    abstract fun onCompressionDone(wallpaper: Wallpaper, file: File): Wallpaper
}
