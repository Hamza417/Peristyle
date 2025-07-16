package app.simple.peri.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.simple.peri.models.WallhavenWallpaper
import app.simple.peri.repository.WallhavenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class WallhavenViewModel @Inject constructor(
        private val repository: WallhavenRepository
) : ViewModel() {

    private val _query = MutableStateFlow("nature")
    val query: StateFlow<String> = _query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val wallpapers: StateFlow<PagingData<WallhavenWallpaper>> = _query
        .debounce(300) // avoid flooding API with requests
        .distinctUntilChanged()
        .flatMapLatest { query ->
            repository.getWallpapers(query)
        }
        .cachedIn(viewModelScope)
        .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                PagingData.empty()
        )

    fun search(newQuery: String) {
        _query.value = newQuery
    }
}
