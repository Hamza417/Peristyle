package app.simple.peri.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.peri.viewmodels.TagsViewModel

class TagsViewModelFactory(private val md5: Int = 0,
                           private val tag: String? = null) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
        @Suppress("UNCHECKED_CAST")
        return TagsViewModel(application, md5, tag) as T
    }
}
