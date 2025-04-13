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
        private var instance: WallpaperDatabase? = null

        @Synchronized
        fun getInstance(context: Context): WallpaperDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, WallpaperDatabase::class.java, DATABASE_NAME)

                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, WallpaperDatabase::class.java, DATABASE_NAME)
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
    }
}
