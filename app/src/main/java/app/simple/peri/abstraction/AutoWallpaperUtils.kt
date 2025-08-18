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
        // Calculate the aspect ratios
        val aspectRatio = displayWidth.toFloat() / displayHeight.toFloat()
        val bitmapAspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        // Fit the image entirely within the display
        val (cropWidth, cropHeight) = if (bitmapAspectRatio > aspectRatio) {
            // Bitmap is wider: scale height to fit, crop sides
            val width = (bitmap.height * aspectRatio).toInt()
            width to bitmap.height
        } else {
            // Bitmap is taller: scale width to fit, crop top/bottom
            bitmap.width to (bitmap.width / aspectRatio).toInt()
        }

        // Ensure there is no loss of any part of the image by calculating the crop from the center
        val left = (bitmap.width - cropWidth) / 2
        val top = (bitmap.height - cropHeight) / 2
        val right = left + cropWidth
        val bottom = top + cropHeight

        return Rect(left, top, right, bottom)
    }

    fun cropAndScaleToFit(bitmap: Bitmap, displayWidth: Int, displayHeight: Int): Bitmap {
        // Calculate the crop rectangle
        val cropRect = calculateVisibleCropHint(bitmap, displayWidth, displayHeight)

        // Crop the bitmap
        val croppedBitmap = Bitmap.createBitmap(
                bitmap,
                cropRect.left,
                cropRect.top,
                cropRect.width(),
                cropRect.height()
        )

        // Scale the cropped bitmap to fit the display exactly
        return croppedBitmap.scale(displayWidth, displayHeight)
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