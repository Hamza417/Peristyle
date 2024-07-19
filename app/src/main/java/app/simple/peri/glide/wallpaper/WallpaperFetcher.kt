package app.simple.peri.glide.wallpaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.simple.peri.constants.Misc
import app.simple.peri.utils.BitmapUtils
import app.simple.peri.utils.FileUtils.toUri
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.FileNotFoundException

class WallpaperFetcher(private val wallpaper: Wallpaper) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        try {
            wallpaper.context.contentResolver.openFileDescriptor(wallpaper.wallpaper.uri.toUri(), "r")?.use {
                val bitmap = BitmapFactory.decodeFileDescriptor(it.fileDescriptor, null, BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inSampleSize = BitmapUtils.calculateInSampleSize(this, Misc.getDisplayWidth(), Misc.getDisplayHeight())
                    inJustDecodeBounds = false
                })

                callback.onDataReady(bitmap)

                /**
                 * Read more about this here:
                 * https://github.com/bumptech/glide/wiki/Resource-re-use-in-Glide#how
                 */
                // bitmap?.recycle()
            }
        } catch (e: FileNotFoundException) {
            callback.onLoadFailed(e)
        } catch (e: IllegalArgumentException) {
            callback.onLoadFailed(e)
        }
    }

    override fun cleanup() {
        // Do nothing
    }

    override fun cancel() {
        // Do nothing
    }

    override fun getDataClass(): Class<Bitmap> {
        return Bitmap::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.DATA_DISK_CACHE
    }

    companion object {
        private const val TAG = "WallpaperFetcher"
        const val IMAGE_MAX_SIZE = 1200000 // 1.2MP
    }
}
