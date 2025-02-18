package app.simple.peri.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.peri.database.dao.WallpaperDao
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.ConditionUtils.invert

@Database(entities = [Wallpaper::class], version = 9)
abstract class WallpaperDatabase : RoomDatabase() {
    abstract fun wallpaperDao(): WallpaperDao

    companion object {
        private const val DATABASE_NAME = "wallpapers"
        private const val LAST_RANDOM_DATABASE_NAME = "wallpapers_last_random"
        private const val LAST_RANDOM_HOME_DATABASE_NAME = "wallpapers_last_random_home"
        private const val LAST_RANDOM_LOCK_DATABASE_NAME = "wallpapers_last_random_lock"

        private var instance: WallpaperDatabase? = null

        @Synchronized
        fun getInstance(context: Context): WallpaperDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, WallpaperDatabase::class.java, DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, WallpaperDatabase::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }

        @Synchronized
        fun getLastRandomWallpapersDatabaseInstance(context: Context): WallpaperDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, WallpaperDatabase::class.java, LAST_RANDOM_DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, WallpaperDatabase::class.java, LAST_RANDOM_DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }

        @Synchronized
        fun getLastRandomHomeWallpapersDatabaseInstance(context: Context): WallpaperDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, WallpaperDatabase::class.java, LAST_RANDOM_HOME_DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, WallpaperDatabase::class.java, LAST_RANDOM_HOME_DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }

        @Synchronized
        fun getLastRandomLockWallpapersDatabaseInstance(context: Context): WallpaperDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, WallpaperDatabase::class.java, LAST_RANDOM_LOCK_DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, WallpaperDatabase::class.java, LAST_RANDOM_LOCK_DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance
        }

        fun destroyInstance() {
            if (instance?.isOpen == true) {
                instance?.close()
            }
            instance = null
        }

        fun destroyLastRandomWallpapersDatabaseInstance() {
            if (instance?.isOpen == true) {
                instance?.close()
            }
            instance = null
        }

        fun wipeLastRandomWallpapersDatabase(context: Context) {
            Room.databaseBuilder(context, WallpaperDatabase::class.java, LAST_RANDOM_DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
                .wallpaperDao()
                .nukeTable()
        }

        fun wipeLastRandomHomeWallpapersDatabase(context: Context) {
            Room.databaseBuilder(context, WallpaperDatabase::class.java, LAST_RANDOM_HOME_DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
                .wallpaperDao()
                .nukeTable()
        }

        fun wipeLastRandomLockWallpapersDatabase(context: Context) {
            Room.databaseBuilder(context, WallpaperDatabase::class.java, LAST_RANDOM_LOCK_DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
                .wallpaperDao()
                .nukeTable()
        }
    }
}
