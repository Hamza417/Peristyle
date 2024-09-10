package app.simple.peri.glide.tags

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.request.RequestOptions

class TagsFetcher(private val tag: Tag) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        val wallpaperDatabase = WallpaperDatabase.getInstance(tag.context)!!
        val wallpaperList = mutableListOf<Wallpaper>()
        val bitmapList = mutableListOf<Bitmap>()

        tag.tag?.sum?.forEach {
            wallpaperList.add(wallpaperDatabase.wallpaperDao().getWallpaperByMD5(it)!!)
        }

        // Fetch top 6 wallpapers
        val topWallpapers = wallpaperList.take(TAG_COUNT)

        topWallpapers.forEach { wallpaper ->
            val bitmap = Glide.with(tag.context)
                .asBitmap()
                .load(wallpaper.uri)
                .centerCrop()
                .apply(RequestOptions().override(REQUEST_WIDTH, REQUEST_WIDTH))
                .submit()
                .get()

            bitmapList.add(bitmap)
        }

        // Create a grid of 3x2
        val gridHeight = (topWallpapers.size / GRID_WIDTH).plus(
                when {
                    topWallpapers.size % GRID_WIDTH == 0 -> 0
                    else -> 1
                })
        val bitmapWidth = 256
        val bitmapHeight = 256
        val gridBitmap = Bitmap.createBitmap(GRID_WIDTH * bitmapWidth, gridHeight * bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(gridBitmap)
        canvas.drawColor(Color.TRANSPARENT)

        for (i in bitmapList.indices) {
            val x = (i % GRID_WIDTH) * bitmapWidth
            val y = (i / GRID_WIDTH) * bitmapHeight
            canvas.drawBitmap(bitmapList[i], x.toFloat(), y.toFloat(), null)
        }

        callback.onDataReady(gridBitmap)
    }

    override fun cleanup() {
        /* no-op */
    }

    override fun cancel() {
        /* no-op */
    }

    override fun getDataClass(): Class<Bitmap> {
        return Bitmap::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }

    companion object {
        private const val TAG = "TagsFetcher"
        private const val TAG_COUNT = 12
        private const val GRID_WIDTH = 3
        private const val REQUEST_WIDTH = 256
    }
}
