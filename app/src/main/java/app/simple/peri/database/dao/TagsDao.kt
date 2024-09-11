package app.simple.peri.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.simple.peri.models.Tag

@Dao
interface TagsDao {
    @Query("SELECT * FROM tags")
    fun getAllTags(): List<Tag>

    @Query("SELECT * FROM tags WHERE name = :id")
    fun getTagById(id: String): Tag

    /**
     * Get all tag names whose sum contains the given md5
     *
     * @param md5 The md5 to search for
     */
    @Query("SELECT name FROM tags WHERE sum LIKE '%' || :md5 || '%'")
    fun getTagNamesByMD5(md5: String): List<String>

    @Query("SELECT EXISTS(SELECT 1 FROM tags WHERE name = :id)")
    fun isTagExists(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTag(tag: Tag)

    @Delete
    fun deleteTag(tag: Tag)

    @Query("DELETE FROM tags")
    fun deleteAllTags()
}
