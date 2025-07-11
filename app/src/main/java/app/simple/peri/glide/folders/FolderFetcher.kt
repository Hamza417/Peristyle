package app.simple.peri.glide.folders

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.ui.screens.displayDimension
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.signature.ObjectKey
import android.graphics.Bitmapimport androidx.core.graphics.createBitmap

class FolderFetcher(private val contextFolder: ContextFolder) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        val wallpaperDatabase = WallpaperDatabase.getInstance(contextFolder.context)!!
        val wallpaperList = wallpaperDatabase.wallpaperDao().getWallpapersByPathHashcode(contextFolder.folder.hashcode)
        val bitmapList = mutableListOf<FutureTarget<Bitmap>>()
        Log.i(TAG, "Loading wallpapers from folder: ${wallpaperList.size}")

        // Fetch top 6 wallpapers
        val topWallpapers = wallpaperList.shuffled().take(GRID_COUNT)

        topWallpapers.forEach { wallpaper ->
            /**
             * Reference:
             * https://bumptech.github.io/glide/doc/getting-started.html#background-threads
             */
            val bitmap = Glide.with(contextFolder.context)
                .asBitmap()
                .load(wallpaper.filePath)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .signature(ObjectKey(
                        (wallpaper.filePath + displayDimension.getReducedWidth() + displayDimension.getReducedHeight())
                            .hashCode()))
                .submit(displayDimension.getReducedWidth(), displayDimension.getReducedHeight())

            bitmapList.add(bitmap)
        }

        // Create a grid of 3x2
        val gridHeight = (topWallpapers.size / GRID_WIDTH).plus(
                when {
                    topWallpapers.size % GRID_WIDTH == 0 -> 0
                    else -> 1
                })
        val bitmapWidth = displayDimension.getReducedWidth()
        val bitmapHeight = displayDimension.getReducedHeight()
        val gridBitmap = createBitmap(GRID_WIDTH * bitmapWidth, gridHeight * bitmapHeight)
        val canvas = Canvas(gridBitmap)
        canvas.drawColor(Color.TRANSPARENT)

        for (i in bitmapList.indices) {
            val x = (i % GRID_WIDTH) * bitmapWidth
            val y = (i / GRID_WIDTH) * bitmapHeight
            canvas.drawBitmap(bitmapList[i].get(), x.toFloat(), y.toFloat(), null)
        }

        callback.onDataReady(gridBitmap)

        /**
         * Reference:
         * https://bumptech.github.io/glide/doc/getting-started.html#background-threads
         */
        bitmapList.forEach { Glide.with(contextFolder.context).clear(it) }
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
        const val TAG = "FolderFetcher"

        const val GRID_WIDTH = 3
        const val GRID_COUNT = 12
    }
}
