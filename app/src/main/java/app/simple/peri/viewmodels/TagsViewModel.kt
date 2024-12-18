package app.simple.peri.viewmodels

import android.app.Application
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.TagsDatabase
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.extensions.CompressorViewModel
import app.simple.peri.models.Tag
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.FileUtils.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class TagsViewModel(application: Application, private val id: String? = null, private val tag: String? = null) :
    CompressorViewModel(application) {

    private val tags: MutableLiveData<List<Tag>> by lazy {
        MutableLiveData<List<Tag>>().also {
            loadTags()
        }
    }

    private val wallpaperTags: MutableLiveData<List<String>> by lazy {
        MutableLiveData<List<String>>().also {
            if (id != null) {
                loadWallpaperTags(id)
            }
        }
    }

    private val wallpapers: MutableLiveData<List<Wallpaper>> by lazy {
        MutableLiveData<List<Wallpaper>>().also {
            if (tag != null) {
                loadWallpapers(tag)
            }
        }
    }

    fun getTags(): LiveData<List<Tag>> {
        return tags
    }

    fun getWallpaperTags(): LiveData<List<String>> {
        return wallpaperTags
    }

    fun getWallpapers(): LiveData<List<Wallpaper>> {
        return wallpapers
    }

    private fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(getApplication())
            val tagsDao = database?.tagsDao()
            val tags = tagsDao?.getAllTags()
            this@TagsViewModel.tags.postValue(tags?.sortedBy {
                it.name
            })
        }
    }

    private fun loadWallpaperTags(md5: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(getApplication())
            val tagsDao = database?.tagsDao()
            val tags = tagsDao?.getTagNamesByID(md5)
            wallpaperTags.postValue(tags)
        }
    }

    private fun loadWallpapers(factoryTag: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tagsDatabase = TagsDatabase.getInstance(getApplication())
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val tagsDao = tagsDatabase?.tagsDao()
            val tag = tagsDao?.getTagByID(factoryTag)
            val wallpapers = wallpaperDatabase?.wallpaperDao()?.getWallpapersByMD5s(tag?.sum!!)
            this@TagsViewModel.wallpapers.postValue(wallpapers)
        }
    }

    fun addTag(tagName: String, wallpapers: List<Wallpaper>, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(getApplication())
            val tagsDao = database?.tagsDao()

            wallpapers.forEach { wallpaper ->
                if (tagsDao?.isTagExists(tagName.trim())!!) {
                    val tag = tagsDao.getTagByID(tagName.trim())
                    tag.addSum(wallpaper.id)
                    tagsDao.insertTag(tag)
                } else {
                    val tag = Tag(tagName.trim(), hashSetOf(wallpaper.id))
                    tagsDao.insertTag(tag)
                }
            }

            loadTags()

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(getApplication())
            val tagsDao = database?.tagsDao()
            tagsDao?.deleteTag(tag)
            loadTags()
        }
    }

    private fun createRandomTagsForTesting() {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(getApplication())
            val tagsDao = database?.tagsDao()
            val wallpapers = WallpaperDatabase.getInstance(getApplication())?.wallpaperDao()?.getWallpapers()

            val randomTags = listOf("Red", "Grey", "Blue", "Black", "White")
            val random = java.util.Random()

            randomTags.forEach { tagName ->
                val randomWallpapers = wallpapers?.shuffled()?.take(random.nextInt(10) + 6) ?: emptyList()
                val tag = Tag(tagName, randomWallpapers.map { it.id }.toHashSet())
                tagsDao?.insertTag(tag)
            }

            loadTags()
        }
    }

    fun deleteWallpaper(deletedWallpaper: Wallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(getApplication())
            val tagsDao = database?.tagsDao()
            val tags = tagsDao?.getAllTags()

            tags?.forEach { tag ->
                if (tag.sum.contains(deletedWallpaper.id)) {
                    tag.sum.remove(deletedWallpaper.id)
                    tagsDao.insertTag(tag)
                }
            }

            DocumentFile.fromSingleUri(getApplication(), deletedWallpaper.uri.toUri())?.delete()

            if (tag != null) {
                loadWallpapers(tag)
                loadTags()
            }
        }
    }

    override fun onCompressionDone(wallpaper: Wallpaper, file: File): Wallpaper {
        return postNewWallpaper(file, wallpaper)
    }

    private fun postNewWallpaper(file: File, previousWallpaper: Wallpaper): Wallpaper {
        val wallpaper = Wallpaper.createFromFile(file)
        wallpaper.id = previousWallpaper.id
        wallpaper.folderID = previousWallpaper.folderID
        val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
        val wallpaperDao = wallpaperDatabase?.wallpaperDao()
        wallpaperDao?.insert(wallpaper)
        loadWallpapers(tag!!)
        return wallpaper
    }
}
