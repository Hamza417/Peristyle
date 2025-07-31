package app.simple.peri.database.converters

import androidx.room.TypeConverter

class IDListConverter {
    @TypeConverter
    fun fromIDList(md5List: HashSet<Int?>?): String {
        return md5List?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toIDList(md5String: String): HashSet<Int?> {
        return if (md5String.isEmpty()) {
            hashSetOf()
        } else {
            md5String.split(",").mapNotNull { it.toIntOrNull() }.toHashSet()
        }
    }
}
