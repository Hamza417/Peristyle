package app.simple.peri.extensions

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.simple.peri.models.Wallpaper
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

abstract class CompressorViewModel(application: Application) : AndroidViewModel(application) {
    fun compressWallpaper(wallpaper: Wallpaper, onSuccess: (Wallpaper) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val uri = Uri.parse(wallpaper.uri)
            val documentFile = DocumentFile.fromSingleUri(context, uri)!!
            val file = File(context.cacheDir, "source_${wallpaper.name}")
            val destinationFile = File(context.cacheDir, "compressed_${wallpaper.name}")

            file.delete()
            destinationFile.delete()

            // Copy the file to the cache directory
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Compress the file
            val compressedImageFile = Compressor.compress(context, file) {
                destination(destinationFile)
                quality(60)
                resolution(wallpaper.width!!, wallpaper.height!!)
                format(getFormat(wallpaper.name!!))
            }

            // Overwrite the original file with the compressed file
            context.contentResolver.openFileDescriptor(uri, "rw")?.use { parcelFileDescriptor ->
                FileOutputStream(parcelFileDescriptor.fileDescriptor).use { outputStream ->
                    // Truncate the file to 0 bytes
                    FileOutputStream(parcelFileDescriptor.fileDescriptor).channel.truncate(0)
                    compressedImageFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            file.delete()
            destinationFile.delete()
            val wallpaper1 = onCompressionDone(wallpaper, documentFile)

            withContext(Dispatchers.Main) {
                onSuccess(wallpaper1)
            }
        }
    }

    fun reduceResolution(wallpaper: Wallpaper, onSuccess: (Wallpaper) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val uri = Uri.parse(wallpaper.uri)
            val documentFile = DocumentFile.fromSingleUri(context, uri)!!
            val file = File(context.cacheDir, "source_${wallpaper.name}")
            val destinationFile = File(context.cacheDir, "compressed_${wallpaper.name}")

            file.delete()
            destinationFile.delete()

            // Copy the file to the cache directory
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                file.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            // Compress the file
            val compressedImageFile = Compressor.compress(context, file) {
                destination(destinationFile)
                quality(100)
                resolution(wallpaper.width!!.div(2), wallpaper.height!!.div(2))
                format(getFormat(wallpaper.name!!))
            }

            // Overwrite the original file with the compressed file
            context.contentResolver.openFileDescriptor(uri, "rw")?.use { parcelFileDescriptor ->
                FileOutputStream(parcelFileDescriptor.fileDescriptor).use { outputStream ->
                    // Truncate the file to 0 bytes
                    FileOutputStream(parcelFileDescriptor.fileDescriptor).channel.truncate(0)
                    compressedImageFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            file.delete()
            destinationFile.delete()
            val wallpaper1 = onCompressionDone(wallpaper, documentFile)

            withContext(Dispatchers.Main) {
                onSuccess(wallpaper1)
            }
        }
    }

    private fun getFormat(name: String): Bitmap.CompressFormat {
        return when (name.substringAfterLast(".")) {
            "jpg" -> Bitmap.CompressFormat.JPEG
            "jpeg" -> Bitmap.CompressFormat.JPEG
            "png" -> Bitmap.CompressFormat.PNG
            else -> Bitmap.CompressFormat.WEBP
        }
    }

    abstract fun onCompressionDone(wallpaper: Wallpaper, documentFile: DocumentFile): Wallpaper
}
