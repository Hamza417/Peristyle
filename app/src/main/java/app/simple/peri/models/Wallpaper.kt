package app.simple.peri.models

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpapers")
class Wallpaper : Parcelable {

    @ColumnInfo(name = "name")
    var name: String? = null

    @SuppressLint("KotlinNullnessAnnotation")
    @PrimaryKey
    @ColumnInfo(name = "uri")
    @NonNull
    var uri: String = ""

    @ColumnInfo(name = "width")
    var width: Int? = null

    @ColumnInfo(name = "height")
    var height: Int? = null

    @ColumnInfo(name = "dateModified")
    var dateModified: Long = 0

    @ColumnInfo(name = "size")
    var size: Long = 0

    @ColumnInfo
    var isSelected: Boolean = false

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()
        uri = parcel.readString().toString()
        width = parcel.readValue(Int::class.java.classLoader) as? Int
        height = parcel.readValue(Int::class.java.classLoader) as? Int
        dateModified = parcel.readLong()
        size = parcel.readLong()
        isSelected = parcel.readByte() != 0.toByte()
    }

    constructor(name: String?, uri: String, width: Int?, height: Int?, dateModified: Long, isSelected: Boolean) {
        this.name = name
        this.uri = uri
        this.width = width
        this.height = height
        this.dateModified = dateModified
        this.isSelected = isSelected
    }

    constructor()

    override fun toString(): String {
        return "Wallpaper(name=$name, uri=$uri, width=$width, height=$height)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Wallpaper) return false

        if (name != other.name) return false
        if (uri != other.uri) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (dateModified != other.dateModified) return false
        if (size != other.size) return false
        if (isSelected != other.isSelected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + uri.hashCode()
        result = 31 * result + (width ?: 0)
        result = 31 * result + (height ?: 0)
        result = 31 * result + dateModified.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + isSelected.hashCode()
        return result
    }

    fun isNull(): Boolean {
        return name == null || uri.isEmpty() || width == null || height == null
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(uri)
        parcel.writeValue(width)
        parcel.writeValue(height)
        parcel.writeLong(dateModified)
        parcel.writeLong(size)
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
    }
}