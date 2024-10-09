package app.simple.peri.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Folder
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.preferences.SharedPreferences.registerSharedPreferenceChangeListener
import app.simple.peri.preferences.SharedPreferences.unregisterSharedPreferenceChangeListener
import app.simple.peri.utils.WallpaperSort.getSortedList
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.destination
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class FolderDataViewModel(application: Application, private val folder: Folder) :
    AndroidViewModel(application), OnSharedPreferenceChangeListener {

    init {
        registerSharedPreferenceChangeListener()
    }

    private val wallpapersData: MutableLiveData<ArrayList<Wallpaper>> by lazy {
        MutableLiveData<ArrayList<Wallpaper>>().also {
            loadWallpaperDatabase()
        }
    }

    fun getWallpapers(): MutableLiveData<ArrayList<Wallpaper>> {
        return wallpapersData
    }

    private fun loadWallpaperDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.sanitizeEntries() // Sanitize the database
            val wallpaperList =
                wallpaperDao?.getWallpapersByUriHashcode(folder.hashcode)?.toMutableList()
                    ?: throw NullPointerException("Wallpaper list is null")

            (wallpaperList as ArrayList<Wallpaper>).getSortedList()

            for (i in wallpaperList.indices) {
                wallpaperList[i].isSelected = false
            }

            @Suppress("UNCHECKED_CAST")
            wallpapersData.postValue(wallpaperList.clone() as ArrayList<Wallpaper>)
        }
    }

    fun compressWallpaper(wallpaper: Wallpaper, onSuccess: (Wallpaper) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val uri = Uri.parse(wallpaper.uri)
            val documentFile = DocumentFile.fromSingleUri(context, uri)
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

            val format = when (wallpaper.name?.substringAfterLast(".")) {
                "jpg" -> Bitmap.CompressFormat.JPEG
                "jpeg" -> Bitmap.CompressFormat.JPEG
                "png" -> Bitmap.CompressFormat.PNG
                else -> Bitmap.CompressFormat.WEBP
            }

            // Compress the file
            val compressedImageFile = Compressor.compress(context, file) {
                destination(destinationFile)
                quality(60)
                format(format)
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
            val wallpaper1 = documentFile?.let { postNewWallpaper(it, wallpaper) }!!

            withContext(Dispatchers.Main) {
                onSuccess(wallpaper1)
            }
        }
    }

    fun reduceResolution(wallpaper: Wallpaper, onSuccess: (Wallpaper) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val uri = Uri.parse(wallpaper.uri)
                val documentFile = DocumentFile.fromSingleUri(context, uri)

                if (documentFile != null && documentFile.exists()) {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        val reducedBitmap = Bitmap.createScaledBitmap(
                            bitmap,
                            bitmap.width / 2,
                            bitmap.height / 2,
                            true
                        )

                        val reducedFile = File(context.cacheDir, "reduced_${documentFile.name}")

                        reducedFile.outputStream().use { outputStream ->
                            reducedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                        }

                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            reducedFile.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }

                        reducedFile.delete()
                        val wallpaper1 = postNewWallpaper(documentFile, wallpaper)

                        withContext(Dispatchers.Main) {
                            onSuccess(wallpaper1)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun postNewWallpaper(documentFile: DocumentFile, previousWallpaper: Wallpaper): Wallpaper {
        val wallpaper = Wallpaper().createFromUri(documentFile.uri.toString(), getApplication())
        wallpaper.md5 = previousWallpaper.md5
        wallpaper.uriHashcode = previousWallpaper.uriHashcode
        wallpaper.dateModified = previousWallpaper.dateModified
        val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
        val wallpaperDao = wallpaperDatabase?.wallpaperDao()
        wallpaperDao?.insert(wallpaper)
        loadWallpaperDatabase()
        return wallpaper
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MainPreferences.SORT -> {
                wallpapersData.value?.getSortedList()
                wallpapersData.postValue(wallpapersData.value)
            }

            MainPreferences.ORDER -> {
                wallpapersData.value?.getSortedList()
                wallpapersData.postValue(wallpapersData.value)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        this.unregisterSharedPreferenceChangeListener()
    }

    fun deleteWallpaper(deletedWallpaper: Wallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()
            wallpaperDao?.delete(deletedWallpaper)
            loadWallpaperDatabase()
        }
    }
}
