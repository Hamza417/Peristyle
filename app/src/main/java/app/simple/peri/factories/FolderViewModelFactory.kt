package app.simple.peri.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.peri.models.Folder
import app.simple.peri.viewmodels.FolderDataViewModel

class FolderViewModelFactory(private val hashCode: Folder) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
        @Suppress("UNCHECKED_CAST")
        return FolderDataViewModel(application, hashCode) as T
    }
}
