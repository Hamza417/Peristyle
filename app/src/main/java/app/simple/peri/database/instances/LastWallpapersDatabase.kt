package app.simple.peri.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.peri.database.dao.WallpaperDao
import app.simple.peri.database.migrations.WallpaperMigration_9_10
import app.simple.peri.models.Wallpaper
import app.simple.peri.models.WallpaperUsage
import app.simple.peri.utils.ConditionUtils.invert

@Database(entities = [Wallpaper::class, WallpaperUsage::class], version = 10)
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
                        .addMigrations(WallpaperMigration_9_10())
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, LastWallpapersDatabase::class.java, DB_NAME)
                    .addMigrations(WallpaperMigration_9_10())
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
                    .build()
                    .clearAllTables()
            }
        }

        @Synchronized
        fun destroyInstance() {
            kotlin.runCatching {
                instance?.close()
            }.getOrElse {
                // Ignore any exceptions during close
            }

            instance = null
        }
    }
}