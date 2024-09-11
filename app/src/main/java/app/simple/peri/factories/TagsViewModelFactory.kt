package app.simple.peri.factories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.peri.viewmodels.TagsViewModel

class TagsViewModelFactory(private val application: Application, private val md5: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TagsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TagsViewModel(application, md5) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
