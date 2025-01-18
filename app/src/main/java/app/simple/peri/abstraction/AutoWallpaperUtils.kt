package app.simple.peri.abstraction

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Build
import android.util.Log
import app.simple.peri.abstraction.AbstractComposeAutoWallpaperService.Companion.TAG
import app.simple.peri.utils.BitmapUtils
import app.simple.peri.utils.BitmapUtils.cropBitmap
import app.simple.peri.utils.FileUtils.toFile
import java.io.ByteArrayInputStream

object AutoWallpaperUtils {

    fun getBitmapFromFile(path: String, expectedWidth: Int, expectedHeight: Int, crop: Boolean = true, recycle: Boolean = true, onBitmap: (Bitmap) -> Unit) {
        path.toFile().inputStream().use { stream ->
            val byteArray = stream.readBytes()
            var bitmap = decodeBitmap(byteArray, expectedWidth, expectedHeight)

            // Correct orientation of the bitmap if faulty due to EXIF data
            bitmap = BitmapUtils.correctOrientation(bitmap, ByteArrayInputStream(byteArray))

            val visibleCropHint = calculateVisibleCropHint(bitmap, expectedWidth, expectedHeight)

            if (crop) {
                bitmap = bitmap.cropBitmap(visibleCropHint)
            }

            onBitmap(bitmap)

            if (recycle) {
                bitmap.recycle()
            }
        }
    }

    fun calculateVisibleCropHint(bitmap: Bitmap, displayWidth: Int, displayHeight: Int): Rect {
        // Calculate the aspect ratio of the display
        val aspectRatio = displayWidth.toFloat() / displayHeight.toFloat()
        // Calculate the aspect ratio of the bitmap
        val bitmapAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        // Determine the crop width and height based on the aspect ratios
        val (cropWidth, cropHeight) = if (bitmapAspectRatio > aspectRatio) {
            // If the bitmap is wider than the desired aspect ratio
            val width = (bitmap.height * aspectRatio).toInt()
            width to bitmap.height
        } else {
            // If the bitmap is taller than the desired aspect ratio
            val height = (bitmap.width / aspectRatio).toInt()
            bitmap.width to height
        }

        // Calculate the left, top, right, and bottom coordinates for the crop rectangle
        val left = (bitmap.width - cropWidth) / 2
        val top = (bitmap.height - cropHeight) / 2
        val right = left + cropWidth
        val bottom = top + cropHeight

        // Return the calculated crop rectangle
        return Rect(left, top, right, bottom)
    }

    fun decodeBitmap(byteArray: ByteArray, displayWidth: Int, displayHeight: Int): Bitmap {
        val bitmapOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, bitmapOptions)

        return BitmapFactory.decodeStream(
                ByteArrayInputStream(byteArray), null, BitmapFactory.Options().apply {
            inPreferredConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Bitmap.Config.RGBA_1010102
            } else {
                Bitmap.Config.ARGB_8888
            }

            inMutable = true

            Log.d(TAG, "Expected bitmap size: $displayWidth x $displayHeight")
            inSampleSize =
                BitmapUtils.calculateInSampleSize(bitmapOptions, displayWidth, displayHeight)
            inJustDecodeBounds = false
            Log.d(TAG, "Bitmap decoded with sample size: ${this.inSampleSize}")
        })!!
    }
}