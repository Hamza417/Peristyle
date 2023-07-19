package app.simple.waller.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.waller.database.dao.WallpaperDao
import app.simple.waller.models.Wallpaper
import app.simple.waller.utils.ConditionUtils.invert

@Database(entities = [Wallpaper::class], version = 3)
abstract class WallpaperDatabase : RoomDatabase() {
    abstract fun wallpaperDao(): WallpaperDao

    companion object {
        private const val DATABASE_NAME = "wallpapers"
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
    }
}