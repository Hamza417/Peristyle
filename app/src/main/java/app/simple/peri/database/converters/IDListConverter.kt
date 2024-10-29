package app.simple.peri.database.converters

import androidx.room.TypeConverter

class IDListConverter {
    @TypeConverter
    fun fromIDList(md5List: HashSet<String>): String {
        return md5List.joinToString(",")
    }

    @TypeConverter
    fun toIDList(md5String: String): HashSet<String> {
        return md5String.split(",").toHashSet()
    }
}
