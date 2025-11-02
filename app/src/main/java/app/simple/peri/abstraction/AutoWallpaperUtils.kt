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
            Log.d(TAG, "Image path : $path}")
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

    fun calculateVisibleCropHint(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Rect {
        val targetAspect = targetWidth.toFloat() / targetHeight
        val bitmapAspect = bitmap.width.toFloat() / bitmap.height

        val (cropWidth, cropHeight) = if (bitmapAspect > targetAspect) {
            val height = bitmap.height
            val width = (height * targetAspect).toInt().coerceAtMost(bitmap.width)
            width to height
        } else {
            val width = bitmap.width
            val height = (width / targetAspect).toInt().coerceAtMost(bitmap.height)
            width to height
        }

        val left = ((bitmap.width - cropWidth) / 2).coerceAtLeast(0)
        val top = ((bitmap.height - cropHeight) / 2).coerceAtLeast(0)
        return Rect(left, top, left + cropWidth, top + cropHeight)
    }

    fun cropAndScaleToFit(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        // Always crop to aspect ratio before scaling to avoid distortion
        val cropRect = calculateVisibleCropHint(bitmap, targetWidth, targetHeight)
        val cropped = Bitmap.createBitmap(
                bitmap,
                cropRect.left,
                cropRect.top,
                cropRect.width(),
                cropRect.height()
        )
        val scaled = cropped.scale(targetWidth, targetHeight)
        if (cropped != bitmap) cropped.recycle()
        return scaled
    }

    fun decodeBitmap(byteArray: ByteArray, targetWidth: Int, targetHeight: Int): Bitmap {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, options)

        val decodeOptions = BitmapFactory.Options().apply {
            inPreferredConfig = MainComposePreferences.getWallpaperColorSpace()
            inMutable = true
            inSampleSize = BitmapUtils.calculateInSampleSize(options, targetWidth, targetHeight)
            inJustDecodeBounds = false
        }

        Log.d(TAG, "Expected bitmap size: $targetWidth x $targetHeight")
        Log.d(TAG, "Bitmap decoded with sample size: ${decodeOptions.inSampleSize}")

        return BitmapFactory.decodeStream(ByteArrayInputStream(byteArray), null, decodeOptions)!!
    }

    fun bitmapToInputStream(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 50): InputStream {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(format, quality, outputStream)
        return ByteArrayInputStream(outputStream.toByteArray())
    }
}