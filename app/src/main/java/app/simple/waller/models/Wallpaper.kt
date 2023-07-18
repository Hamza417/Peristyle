package app.simple.waller.models

import android.annotation.SuppressLint
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpapers")
class Wallpaper {

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

    constructor(name: String?, uri: String, width: Int?, height: Int?) {
        this.name = name
        this.uri = uri
        this.width = width
        this.height = height
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

        return true
    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + uri.hashCode()
        result = 31 * result + (width ?: 0)
        result = 31 * result + (height ?: 0)
        return result
    }

    fun isNull(): Boolean {
        return name == null && uri == null // && width == null && height == null
    }
}