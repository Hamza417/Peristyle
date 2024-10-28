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
import app.simple.peri.utils.ConditionUtils.invert
import kotlinx.coroutines.flow.Flow
import kotlin.system.measureTimeMillis

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
    @Query("SELECT * FROM wallpapers WHERE id = :id")
    fun getWallpaperByMD5(id: String): Wallpaper?

    /**
     * Get wallpapers by the matching all the MD% in the HashSet
     */
    @Query("SELECT * FROM wallpapers WHERE id IN (:ids)")
    fun getWallpapersByMD5s(ids: Set<String>): List<Wallpaper>

    /**
     * Get wallpapers by the matching the [Wallpaper.folderID]
     * with the specified [hashcode]
     */
    @Query("SELECT * FROM wallpapers WHERE folder_id = :hashcode")
    fun getWallpapersByPathHashcode(hashcode: Int): List<Wallpaper>

    /**
     * Get count of wallpapers of the specified [uriHashcode]
     */
    @Query("SELECT COUNT(*) FROM wallpapers WHERE folder_id = :uriHashcode")
    fun getWallpapersCountByPathHashcode(uriHashcode: Int): Int

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
     * Delete wallpaper by URI
     */
    @Query("DELETE FROM wallpapers WHERE uri = :uri")
    fun deleteByUri(uri: String)

    /**
     * Delete wallpaper by File
     */
    @Query("DELETE FROM wallpapers WHERE file_path = :path")
    fun deleteByFile(path: String)

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
        val existingWallpaper = getWallpaperByMD5(wallpaper.id)
        if (existingWallpaper != null) {
            wallpaper.id += "duplicate"
            Log.i("WallpaperDao", "Duplicate wallpaper found: ${wallpaper.id}")
        }

        insert(wallpaper)
    }

    /**
     * Delete the entire table
     */
    @Query("DELETE FROM wallpapers")
    fun nukeTable()

    /**
     * Delete all wallpapers by the matching the [Wallpaper.folderID]
     * with the specified [hashcode]
     */
    @Query("DELETE FROM wallpapers WHERE folder_id = :hashcode")
    fun deleteByPathHashcode(hashcode: Int)

    fun purgeNonExistingWallpapers() {
        Log.i("WallpaperDao", "Purged non-existing wallpapers in: ${
            measureTimeMillis {
                getWallpapers().forEach {
                    if (it.getFile().exists().invert()) {
                        delete(it)
                    }
                }
            }
        }")
    }
}
