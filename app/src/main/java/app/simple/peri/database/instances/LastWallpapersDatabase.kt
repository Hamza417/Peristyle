package app.simple.peri.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.peri.database.dao.WallpaperDao
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.ConditionUtils.invert

@Database(entities = [Wallpaper::class], version = 9)
abstract class LastWallpapersDatabase : RoomDatabase() {

    abstract fun wallpaperDao(): WallpaperDao

    companion object {
        private const val DB_NAME = "wallpapers_last_random"

        private var instance: LastWallpapersDatabase? = null

        @Synchronized
        fun getInstance(context: Context): LastWallpapersDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, LastWallpapersDatabase::class.java, DB_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, LastWallpapersDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }

        @Synchronized
        fun wipeDatabase(context: Context) {
            kotlin.runCatching {
                instance?.clearAllTables()
            }.getOrElse {
                Room.databaseBuilder(context, LastWallpapersDatabase::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
                    .clearAllTables()
            }
        }

        @Synchronized
        fun destroyInstance() {
            instance = null
        }
    }
}