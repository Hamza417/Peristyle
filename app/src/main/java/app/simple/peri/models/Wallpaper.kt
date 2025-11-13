package app.simple.peri.models

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.documentfile.provider.DocumentFile
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.MainPreferences
import app.simple.peri.utils.BitmapUtils.generatePalette
import app.simple.peri.utils.ConditionUtils.isNotNull
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.utils.FileUtils.toUri
import app.simple.peri.utils.WallpaperSort
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.Serializable

@Entity(
        tableName = "wallpapers",
        indices = [
            Index(value = ["dateModified"]),
            Index(value = ["id"], unique = true),
            Index(value = ["folder_id"])
        ]
)
class Wallpaper() : Comparable<Wallpaper>, Serializable, Parcelable {

    @ColumnInfo(name = "name")
    var name: String? = null

    @SuppressLint("KotlinNullnessAnnotation")
    @ColumnInfo(name = "uri")
    @NonNull
    var uri: String = ""

    @ColumnInfo(name = "file_path")
    var filePath: String = ""

    @PrimaryKey
    @ColumnInfo(name = "id")
    var id: Int = 0

    @ColumnInfo(name = "prominentColor")
    var prominentColor: Int = Color.TRANSPARENT

    @ColumnInfo(name = "width")
    var width: Int? = null

    @ColumnInfo(name = "height")
    var height: Int? = null

    @ColumnInfo(name = "dateModified")
    var dateModified: Long = 0

    @ColumnInfo(name = "size")
    var size: Long = 0

    @ColumnInfo(name = "folder_id")
    var folderID: Int = 0

    @ColumnInfo
    var isSelected: Boolean = false

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()
        uri = parcel.readString() ?: ""
        filePath = parcel.readString() ?: ""
        id = parcel.readInt()
        prominentColor = parcel.readInt()
        width = parcel.readValue(Int::class.java.classLoader) as? Int
        height = parcel.readValue(Int::class.java.classLoader) as? Int
        dateModified = parcel.readLong()
        size = parcel.readLong()
        folderID = parcel.readInt()
        isSelected = parcel.readByte() != 0.toByte()
    }

    fun getFile(): File {
        return filePath.toFile()
    }

    override fun toString(): String {
        return "Wallpaper(name=$name, uri=$uri, width=$width, height=$height)"
    }

    override fun compareTo(other: Wallpaper): Int {
        return when (MainPreferences.getSort()) {
            WallpaperSort.NAME -> name!!.compareTo(other.name!!)
            WallpaperSort.DATE -> dateModified.compareTo(other.dateModified)
            WallpaperSort.SIZE -> size.compareTo(other.size)
            WallpaperSort.WIDTH -> width!!.compareTo(other.width!!)
            WallpaperSort.HEIGHT -> height!!.compareTo(other.height!!)
            else -> 0
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Wallpaper) return false

        return id == other.id
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + uri.hashCode()
        result = 31 * result + (width ?: 0)
        result = 31 * result + (height ?: 0)
        result = 31 * result + dateModified.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + isSelected.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + prominentColor
        result = 31 * result + folderID
        return result
    }

    fun isNull(): Boolean {
        return name == null || uri.isEmpty() || width == null || height == null
    }

    /**
     * Checks if the wallpaper is in compressible format.
     */
    fun isNotCompressible(): Boolean {
        return filePath.isNotEmpty() && filePath.endsWith(".png")
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(uri)
        parcel.writeString(filePath)
        parcel.writeInt(id)
        parcel.writeInt(prominentColor)
        parcel.writeValue(width)
        parcel.writeValue(height)
        parcel.writeLong(dateModified)
        parcel.writeLong(size)
        parcel.writeInt(folderID)
        parcel.writeByte(if (isSelected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Wallpaper> {
        override fun createFromParcel(parcel: Parcel): Wallpaper {
            return Wallpaper(parcel)
        }

        override fun newArray(size: Int): Array<Wallpaper?> {
            return arrayOfNulls(size)
        }

        private const val TAG = "Wallpaper"

        /**
         * Use this method to create a Wallpaper object from a URI only for files
         * that doesn't exist in the database and are not required to be associated
         * with a specific folder.
         *
         * [folderID] will return null
         *
         * @param uri The URI of the file
         */
        fun createFromUri(uri: String, context: Context): Wallpaper {
            val wallpaper = Wallpaper()
            wallpaper.uri = uri
            val documentFile = DocumentFile.fromSingleUri(context, Uri.parse(uri))
            wallpaper.name = documentFile?.name
            wallpaper.size = documentFile?.length() ?: 0
            wallpaper.dateModified = documentFile?.lastModified() ?: 0

            context.contentResolver.openInputStream(uri.toUri())?.use { inputStream ->
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = false
                val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
                wallpaper.width = options.outWidth
                wallpaper.height = options.outHeight
                wallpaper.prominentColor = bitmap?.generatePalette()?.vibrantSwatch?.rgb ?: 0
                wallpaper.id = uri.hashCode()
            }

            return wallpaper
        }

        fun createWallpaperFromFile(file: DocumentFile, context: Context): Wallpaper {
            return createFromUri(file.uri.toString(), context)
        }

        @Throws(IOException::class)
        fun createFromFile(file: File, context: Context): Wallpaper {
            val wallpaper = Wallpaper()
            wallpaper.filePath = file.absolutePath
            wallpaper.name = file.name
            wallpaper.size = file.length()
            wallpaper.dateModified = file.lastModified()

            // First pass: bounds only
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            file.inputStream().use { BitmapFactory.decodeStream(it, null, bounds) }
            wallpaper.width = bounds.outWidth
            wallpaper.height = bounds.outHeight

            if (MainComposePreferences.skipPalette()) {
                wallpaper.prominentColor = Color.DKGRAY
            } else if (context.isNotNull()) {
                // Minimal resource palette extraction
                try {
                    val targetSize = 32 // small side target
                    val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
                    val sampleSize = if (maxDim > targetSize) {
                        // Highest power of two >= maxDim / targetSize
                        Integer.highestOneBit((maxDim - 1) / targetSize + 1)
                    } else 1

                    val decodeOptions = BitmapFactory.Options().apply {
                        inJustDecodeBounds = false
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.RGB_565 // lower memory footprint
                    }

                    FileInputStream(file).use { input ->
                        val bmp = BitmapFactory.decodeStream(input, null, decodeOptions)
                        wallpaper.prominentColor = bmp?.generatePalette()?.vibrantSwatch?.rgb ?: Color.DKGRAY
                        bmp?.recycle()
                    }
                } catch (_: OutOfMemoryError) {
                    wallpaper.prominentColor = Color.DKGRAY
                } catch (_: Exception) {
                    wallpaper.prominentColor = Color.DKGRAY
                }
            }

            wallpaper.id = file.hashCode()
            return wallpaper
        }
    }
}
