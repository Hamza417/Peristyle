package app.simple.peri.abstraction

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.util.Log
import androidx.core.graphics.scale
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.BitmapUtils
import app.simple.peri.utils.FileUtils.toFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

object AutoWallpaperUtils {

    private const val TAG = "AutoWallpaperUtils"

    fun getBitmapFromFile(path: String, expectedWidth: Int, expectedHeight: Int, crop: Boolean = true, recycle: Boolean = true, onBitmap: (Bitmap) -> Unit) {
        path.toFile().inputStream().use { stream ->
            val byteArray = stream.readBytes()
            var bitmap = decodeBitmap(byteArray, expectedWidth, expectedHeight)

            // Correct orientation of the bitmap if faulty due to EXIF data
            bitmap = BitmapUtils.correctOrientation(bitmap, ByteArrayInputStream(byteArray))

            if (crop) {
                bitmap = cropAndScaleToFit(bitmap, expectedWidth, expectedHeight)
            }

            onBitmap(bitmap)

            if (recycle) {
                bitmap.recycle()
            }
        }
    }

    fun calculateVisibleCropHint(bitmap: Bitmap, displayWidth: Int, displayHeight: Int): Rect {
        val aspectRatio = displayWidth.toFloat() / displayHeight.toFloat()
        val bitmapAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        val cropWidth: Int
        val cropHeight: Int

        if (bitmapAspectRatio > aspectRatio) {
            cropHeight = bitmap.height
            cropWidth = (bitmap.height * aspectRatio).toInt().coerceAtMost(bitmap.width)
        } else {
            cropWidth = bitmap.width
            cropHeight = (bitmap.width / aspectRatio).toInt().coerceAtMost(bitmap.height)
        }

        val left = ((bitmap.width - cropWidth) / 2).coerceAtLeast(0)
        val top = ((bitmap.height - cropHeight) / 2).coerceAtLeast(0)
        val right = (left + cropWidth).coerceAtMost(bitmap.width)
        val bottom = (top + cropHeight).coerceAtMost(bitmap.height)

        return Rect(left, top, right, bottom)
    }

    fun cropAndScaleToFit(bitmap: Bitmap, displayWidth: Int, displayHeight: Int): Bitmap {
        if (bitmap.width < displayWidth || bitmap.height < displayHeight) {
            // If the bitmap is smaller than the target, scale directly
            return bitmap.scale(displayWidth, displayHeight)
        }

        val cropRect = calculateVisibleCropHint(bitmap, displayWidth, displayHeight)

        // Ensure cropRect is within bitmap bounds
        val left = cropRect.left.coerceAtLeast(0)
        val top = cropRect.top.coerceAtLeast(0)
        val width = cropRect.width().coerceAtMost(bitmap.width - left)
        val height = cropRect.height().coerceAtMost(bitmap.height - top)

        val croppedBitmap = Bitmap.createBitmap(bitmap, left, top, width, height)
        val scaledBitmap = croppedBitmap.scale(displayWidth, displayHeight)
        if (croppedBitmap != bitmap) {
            croppedBitmap.recycle()
        }
        return scaledBitmap
    }

    fun decodeBitmap(byteArray: ByteArray, displayWidth: Int, displayHeight: Int): Bitmap {
        val bitmapOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, bitmapOptions)

        return BitmapFactory.decodeStream(
                ByteArrayInputStream(byteArray), null, BitmapFactory.Options().apply {
            inPreferredConfig = MainComposePreferences.getWallpaperColorSpace()
            inMutable = true

            Log.d(TAG, "Expected bitmap size: $displayWidth x $displayHeight")
            inSampleSize =
                BitmapUtils.calculateInSampleSize(bitmapOptions, displayWidth, displayHeight)
            inJustDecodeBounds = false
            Log.d(TAG, "Bitmap decoded with sample size: ${this.inSampleSize}")
        })!!
    }

    fun bitmapToInputStream(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 50): InputStream {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, outputStream)
        return ByteArrayInputStream(outputStream.toByteArray())
    }
}