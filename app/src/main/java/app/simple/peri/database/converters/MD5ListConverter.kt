package app.simple.peri.database.converters

import androidx.room.TypeConverter

class MD5ListConverter {
    @TypeConverter
    fun fromMd5List(md5List: HashSet<String>): String {
        return md5List.joinToString(",")
    }

    @TypeConverter
    fun toMd5List(md5String: String): HashSet<String> {
        return md5String.split(",").toHashSet()
    }
}
