package app.simple.peri.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.simple.peri.models.Effect

@Dao
interface EffectsDao {
    @Query("SELECT * FROM effects")
    fun getAllEffects(): List<Effect>

    @Query("SELECT * FROM effects WHERE id = :id")
    fun getEffectById(id: Int): Effect

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEffect(effect: Effect)

    @Delete
    fun deleteEffect(effect: Effect)

    @Query("DELETE FROM effects")
    fun deleteAllEffects()
}
