package app.simple.peri.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {

    /**
     * Compress image while maintaining original dimensions but reducing file size
     * Optimized for speed by using inSampleSize and efficient bitmap operations
     */
    suspend fun compressImage(
            sourceFile: File,
            destinationFile: File,
            quality: Int = 60,
            targetWidth: Int? = null,
            targetHeight: Int? = null
    ): File = withContext(Dispatchers.IO) {
        // Read original image dimensions without loading full bitmap
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(sourceFile.absolutePath, options)

        val originalWidth = options.outWidth
        val originalHeight = options.outHeight
        val finalWidth = targetWidth ?: originalWidth
        val finalHeight = targetHeight ?: originalHeight

        // Calculate optimal inSampleSize for faster decoding
        options.inSampleSize = calculateInSampleSize(
                originalWidth, originalHeight,
                finalWidth, finalHeight
        )

        // Decode with inSampleSize for faster loading
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inMutable = false

        var bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, options)
            ?: throw IllegalStateException("Failed to decode image")

        // Scale to exact dimensions if needed (only if inSampleSize wasn't exact)
        if (bitmap.width != finalWidth || bitmap.height != finalHeight) {
            val scaledBitmap = bitmap.scale(finalWidth, finalHeight)
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
                bitmap = scaledBitmap
            }
        }

        // Fix orientation if needed
        bitmap = fixOrientation(sourceFile, bitmap)

        // Determine output format
        val format = getCompressFormat(sourceFile.name)

        // Compress and save
        FileOutputStream(destinationFile).use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }

        bitmap.recycle()
        destinationFile
    }

    /**
     * Reduce image resolution (half or custom factor) while maintaining quality
     * Optimized for speed using inSampleSize
     */
    suspend fun reduceResolution(
            sourceFile: File,
            destinationFile: File,
            scaleFactor: Float = 0.5f,
            quality: Int = 100
    ): File = withContext(Dispatchers.IO) {
        // Read original dimensions
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(sourceFile.absolutePath, options)

        val originalWidth = options.outWidth
        val originalHeight = options.outHeight
        val targetWidth = (originalWidth * scaleFactor).toInt()
        val targetHeight = (originalHeight * scaleFactor).toInt()

        // Calculate optimal inSampleSize (power of 2 for best performance)
        options.inSampleSize = calculateInSampleSize(
                originalWidth, originalHeight,
                targetWidth, targetHeight
        )

        // Decode with inSampleSize
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        options.inMutable = false

        var bitmap = BitmapFactory.decodeFile(sourceFile.absolutePath, options)
            ?: throw IllegalStateException("Failed to decode image")

        // Fine-tune scaling if inSampleSize didn't give exact size
        if (bitmap.width != targetWidth || bitmap.height != targetHeight) {
            val scaledBitmap = bitmap.scale(targetWidth, targetHeight)
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
                bitmap = scaledBitmap
            }
        }

        // Fix orientation
        bitmap = fixOrientation(sourceFile, bitmap)

        // Determine format
        val format = getCompressFormat(sourceFile.name)

        // Save with high quality
        FileOutputStream(destinationFile).use { out ->
            bitmap.compress(format, quality, out)
            out.flush()
        }

        bitmap.recycle()
        destinationFile
    }

    /**
     * Calculate optimal inSampleSize for efficient bitmap loading
     * Returns power of 2 for best performance
     */
    private fun calculateInSampleSize(
            srcWidth: Int,
            srcHeight: Int,
            reqWidth: Int,
            reqHeight: Int
    ): Int {
        var inSampleSize = 1

        if (srcHeight > reqHeight || srcWidth > reqWidth) {
            val halfHeight = srcHeight / 2
            val halfWidth = srcWidth / 2

            // Calculate the largest inSampleSize value that is a power of 2
            // and keeps both height and width larger than requested
            while ((halfHeight / inSampleSize) >= reqHeight &&
                    (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Fix image orientation based on EXIF data
     */
    private fun fixOrientation(file: File, bitmap: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(file.absolutePath)
            val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
            )

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> flipBitmap(bitmap, horizontal = true)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> flipBitmap(bitmap, horizontal = false)
                else -> bitmap
            }
        } catch (e: Exception) {
            bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated != bitmap) {
            bitmap.recycle()
        }
        return rotated
    }

    private fun flipBitmap(bitmap: Bitmap, horizontal: Boolean): Bitmap {
        val matrix = Matrix().apply {
            if (horizontal) {
                postScale(-1f, 1f)
            } else {
                postScale(1f, -1f)
            }
        }
        val flipped = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (flipped != bitmap) {
            bitmap.recycle()
        }
        return flipped
    }

    /**
     * Determine compression format based on file extension
     */
    private fun getCompressFormat(fileName: String): Bitmap.CompressFormat {
        return when (fileName.lowercase().substringAfterLast(".")) {
            "jpg", "jpeg" -> Bitmap.CompressFormat.JPEG
            "png" -> Bitmap.CompressFormat.PNG
            "webp" -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }
            else -> Bitmap.CompressFormat.JPEG
        }
    }
}

