package app.simple.peri.database.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.simple.peri.models.Wallpaper
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    @Query("SELECT * FROM wallpapers ORDER BY dateModified DESC")
    fun getWallpapers(): List<Wallpaper>

    @Query("SELECT * FROM wallpapers ORDER BY dateModified DESC")
    fun getWallpapersFlow(): Flow<List<Wallpaper>>

    fun getWallpapersByWidthAndHeight(width: Int, height: Int): List<Wallpaper> {
        return getWallpapers().filter { it.width == width && it.height == height }
    }

    fun getInadequateWallpapers(width: Int, height: Int): List<Wallpaper> {
        return getWallpapers().filter { it.width!! < width || it.height!! < height }
    }

    fun getExcessivelyLargeWallpapers(width: Int, height: Int): List<Wallpaper> {
        return getWallpapers().filter { it.width!! > width || it.height!! > height }
    }

    /**
     * Get wallpaper by MD5
     */
    @Query("SELECT * FROM wallpapers WHERE md5 = :md5")
    fun getWallpaperByMD5(md5: String): Wallpaper?

    /**
     * Get wallpapers by the matching all the MD% in the HashSet
     */
    @Query("SELECT * FROM wallpapers WHERE md5 IN (:md5s)")
    fun getWallpapersByMD5s(md5s: Set<String>): List<Wallpaper>

    /**
     * Get wallpapers by the matching the [Wallpaper.folderUriHashcode]
     * with the specified [uriHashcode]
     */
    @Query("SELECT * FROM wallpapers WHERE uri_hashcode = :uriHashcode")
    fun getWallpapersByUriHashcode(uriHashcode: Int): List<Wallpaper>

    /**
     * Clean any entry that doesn't have any of the
     * specified extension
     *
     * Extensions: .jpg, .jpeg, .webp, .png
     * From: [Wallpaper.name]
     */
    @Query("DELETE FROM wallpapers WHERE name NOT LIKE '%.jpg' AND name NOT LIKE '%.jpeg' AND name NOT LIKE '%.webp' AND name NOT LIKE '%.png'")
    fun sanitizeEntries()

    fun getRandomWallpaper(): Wallpaper {
        return getWallpapers().random()
    }

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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(wallpaper: Wallpaper)

    @Transaction
    fun insertWithConflictHandling(wallpaper: Wallpaper) {
        val existingWallpaper = getWallpaperByMD5(wallpaper.md5)
        if (existingWallpaper != null) {
            wallpaper.md5 += "duplicate"
            Log.i("WallpaperDao", "Duplicate wallpaper found: ${wallpaper.md5}")
        }

        insert(wallpaper)
    }

    /**
     * Delete the entire table
     */
    @Query("DELETE FROM wallpapers")
    fun nukeTable()

    /**
     * Delete wallpaper by the matching the [Wallpaper.folderUriHashcode]
     * with the specified [hashcode]
     */
    @Query("DELETE FROM wallpapers WHERE uri_hashcode = :hashcode")
    fun deleteByUriHashcode(hashcode: Int)
}
