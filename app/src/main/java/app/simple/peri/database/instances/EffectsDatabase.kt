package app.simple.peri.database.instances

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.simple.peri.database.dao.EffectsDao
import app.simple.peri.models.Effect
import app.simple.peri.utils.ConditionUtils.invert

@Database(entities = [Effect::class], version = 2)
abstract class EffectsDatabase : RoomDatabase() {
    abstract fun effectsDao(): EffectsDao

    companion object {
        private const val DATABASE_NAME = "effects"
        private var instance: EffectsDatabase? = null

        @Synchronized
        fun getInstance(context: Context): EffectsDatabase? {
            kotlin.runCatching {
                if (instance!!.isOpen.invert()) {
                    instance = Room.databaseBuilder(context, EffectsDatabase::class.java, DATABASE_NAME)
                        .build()
                }
            }.getOrElse {
                instance = Room.databaseBuilder(context, EffectsDatabase::class.java, DATABASE_NAME).build()
            }

            return instance
        }

        fun destroy() {
            instance?.close()
            instance = null
        }
    }
}
