package app.simple.peri.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.TagsDatabase
import app.simple.peri.models.Tag
import app.simple.peri.models.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TagsViewModel(application: Application) : AndroidViewModel(application) {

    private val tags: MutableLiveData<List<Tag>> by lazy {
        MutableLiveData<List<Tag>>().also {
            loadTags()
        }
    }

    fun getTags(): LiveData<List<Tag>> {
        return tags
    }

    private fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(getApplication())
            val tagsDao = database?.tagsDao()
            val tags = tagsDao?.getAllTags()
            this@TagsViewModel.tags.postValue(tags)
        }
    }

    fun addTag(tagName: String, wallpaper: Wallpaper) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(getApplication())
            val tagsDao = database?.tagsDao()

            if (tagsDao?.isTagExists(tagName.trim())!!) {
                val tag = tagsDao.getTagById(tagName.trim())
                tag.addSum(wallpaper.md5)
                tagsDao.insertTag(tag)
            } else {
                val tag = Tag(tagName.trim(), hashSetOf(wallpaper.md5))
                tagsDao.insertTag(tag)
            }

            loadTags()
        }
    }
}
