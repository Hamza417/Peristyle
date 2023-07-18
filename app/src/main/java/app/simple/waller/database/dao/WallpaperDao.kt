package app.simple.waller.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import app.simple.waller.models.Wallpaper

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers")
    fun getWallpapers(): List<Wallpaper>

    /**
     * Delete a wallpaper from the database
     */
    @Delete
    fun delete(wallpaper: Wallpaper)

    /**
     * Update a wallpaper from the database
     */
    @Update
    fun update(wallpaper: Wallpaper)

    /**
     * Insert a wallpaper into the database
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(wallpaper: Wallpaper)

    /**
     * Delete the entire table
     */
    @Query("DELETE FROM wallpapers")
    fun nukeTable()
}