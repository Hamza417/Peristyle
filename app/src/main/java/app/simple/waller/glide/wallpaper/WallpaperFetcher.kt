package app.simple.waller.glide.wallpaper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import app.simple.waller.utils.FileUtils.toUri
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher

class WallpaperFetcher(private val wallpaper: Wallpaper) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        wallpaper.context.contentResolver.openInputStream(wallpaper.wallpaper.uri?.toUri()!!)?.use {
            callback.onDataReady(BitmapFactory.decodeStream(it))
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