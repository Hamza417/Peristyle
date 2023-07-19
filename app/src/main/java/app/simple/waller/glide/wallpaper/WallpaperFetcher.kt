package app.simple.waller.glide.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import app.simple.waller.utils.FileUtils.toUri
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import kotlin.math.pow
import kotlin.math.sqrt

class WallpaperFetcher(private val wallpaper: Wallpaper) : DataFetcher<Bitmap> {
    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        try {
            wallpaper.context.contentResolver.openInputStream(wallpaper.wallpaper.uri.toUri())?.use {
                callback.onDataReady(getBitmap(wallpaper.wallpaper.uri.toUri(), wallpaper.context))
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
        return DataSource.LOCAL
    }

    private fun getBitmap(uri: Uri, context: Context): Bitmap? {
        var inputStream: InputStream?
        return try {
            inputStream = context.contentResolver.openInputStream(uri)

            // Decode image size
            var options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream!!.close()

            var scale = 1
            while (options.outWidth * options.outHeight * (1 / scale.toDouble().pow(2.0)) > IMAGE_MAX_SIZE) {
                scale++
            }
            Log.d(TAG, "scale = " + scale + ", orig-width: " + options.outWidth + ", orig-height: " + options.outHeight)
            var resultBitmap: Bitmap?
            inputStream = context.contentResolver.openInputStream(uri)
            if (scale > 1) {
                scale--
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                options = BitmapFactory.Options()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    options.inPreferredConfig = Bitmap.Config.RGBA_1010102
                } else {
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                options.inSampleSize = scale
                resultBitmap = BitmapFactory.decodeStream(inputStream, null, options)

                // resize to desired dimensions
                val height = resultBitmap!!.height
                val width = resultBitmap.width
                Log.d(TAG, "1th scale operation dimensions - width: $width, height: $height")
                val y = sqrt(IMAGE_MAX_SIZE
                                          / (width.toDouble() / height))
                val x = y / height * width
                val scaledBitmap = Bitmap.createScaledBitmap(resultBitmap, x.toInt(), y.toInt(), true)
                resultBitmap.recycle()
                resultBitmap = scaledBitmap
                System.gc()
            } else {
                resultBitmap = BitmapFactory.decodeStream(inputStream)
            }
            inputStream!!.close()
            Log.d(TAG, "bitmap size - width: " + resultBitmap!!.width + ", height: " + resultBitmap.height)
            resultBitmap
        } catch (e: IOException) {
            Log.e(TAG, e.message, e)
            null
        }
    }

    companion object {
        private const val TAG = "WallpaperFetcher"
        const val IMAGE_MAX_SIZE = 1200000 // 1.2MP
    }
}