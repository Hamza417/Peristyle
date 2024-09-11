package app.simple.peri.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.peri.database.dao.TagsDao
import app.simple.peri.models.Tag
import app.simple.peri.utils.ConditionUtils.invert

@Database(entities = [Tag::class], version = 1)
abstract class TagsDatabase : RoomDatabase() {
    abstract fun tagsDao(): TagsDao

    companion object {
        private const val DATABASE_NAME = "tags"
        private var instance: TagsDatabase? = null

        @Synchronized
        fun getInstance(context: Context): TagsDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, TagsDatabase::class.java, DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, TagsDatabase::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }
    }
}
