package app.simple.waller.glide.wallpaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Build
import app.simple.waller.utils.FileUtils.toUri
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher

class WallpaperFetcher(private val wallpaper: Wallpaper) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        wallpaper.context.contentResolver.openInputStream(wallpaper.wallpaper.uri.toUri())?.use {
            val bitmap = BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply {
                inPreferredConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Bitmap.Config.RGBA_1010102
                } else {
                    Bitmap.Config.ARGB_8888
                }
                outWidth = wallpaper.wallpaper.width?.div(4)!!
                outHeight = wallpaper.wallpaper.height?.div(4)!!
            })

            callback.onDataReady(bitmap)
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
        return DataSource.LOCAL
    }
}