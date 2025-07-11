package app.simple.peri.coil.impl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import app.simple.peri.coil.models.ContextFolder
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.ui.screens.displayDimension
import coil3.asImage
import coil3.decode.DataSource
import coil3.fetch.FetchResult
import coil3.fetch.Fetcher
import coil3.fetch.ImageFetchResult
import okio.Buffer

class FolderFetcherImpl(private val data: ContextFolder) : Fetcher {
    override suspend fun fetch(): FetchResult? {
        val context = data.context
        val wallpaperDatabase = WallpaperDatabase.getInstance(context)!!
        val wallpaperList = wallpaperDatabase.wallpaperDao().getWallpapersByPathHashcode(data.folder.hashcode)
        val topWallpapers = wallpaperList.shuffled().take(GRID_COUNT)
        val bitmapWidth = displayDimension.getReducedWidth()
        val bitmapHeight = displayDimension.getReducedHeight()
        val gridHeight = (topWallpapers.size / GRID_WIDTH) + if (topWallpapers.size % GRID_WIDTH == 0) 0 else 1
        val gridBitmap = createBitmap(GRID_WIDTH * bitmapWidth, gridHeight * bitmapHeight)
        val canvas = Canvas(gridBitmap)

        canvas.drawColor(Color.TRANSPARENT)

        topWallpapers.forEachIndexed { i, wallpaper ->
            val bmp = android.graphics.BitmapFactory.decodeFile(wallpaper.filePath)
            val x = (i % GRID_WIDTH) * bitmapWidth
            val y = (i / GRID_WIDTH) * bitmapHeight
            bmp?.let {
                val scaled = it.scale(bitmapWidth, bitmapHeight)
                canvas.drawBitmap(scaled, x.toFloat(), y.toFloat(), null)
                if (scaled != it) scaled.recycle()
            }
        }

        val buffer = Buffer()
        gridBitmap.compress(Bitmap.CompressFormat.PNG, 100, buffer.outputStream())

        return ImageFetchResult(
                image = gridBitmap.asImage(),
                isSampled = false,
                dataSource = DataSource.NETWORK
        )
    }

    companion object {
        const val GRID_WIDTH = 3
        const val GRID_COUNT = 12
    }
}