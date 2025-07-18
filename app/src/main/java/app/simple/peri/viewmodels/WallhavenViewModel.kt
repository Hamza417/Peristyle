package app.simple.peri.viewmodels

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import app.simple.peri.models.WallhavenFilter
import app.simple.peri.models.WallhavenResponse
import app.simple.peri.models.WallhavenTag
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
import kotlinx.coroutines.launch

@HiltViewModel
class WallhavenViewModel @Inject constructor(
        private val repository: WallhavenRepository
) : ViewModel() {

    var lazyGridState: LazyStaggeredGridState by mutableStateOf(LazyStaggeredGridState(0, 0))
    private val _filter = MutableStateFlow(WallhavenFilter(query = "forest"))
    val filter: StateFlow<WallhavenFilter> = _filter.asStateFlow()

    private val _meta = MutableStateFlow<WallhavenResponse.Meta?>(null)
    val meta: StateFlow<WallhavenResponse.Meta?> = _meta.asStateFlow()

    private val _tags = MutableStateFlow<List<WallhavenTag>>(emptyList())
    val tags: StateFlow<List<WallhavenTag>> = _tags.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val wallpapers: StateFlow<PagingData<WallhavenWallpaper>> = _filter
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { filter ->
            repository.getWallpapers(filter) { meta ->
                _meta.value = meta
            }
        }
        .cachedIn(viewModelScope)
        .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                PagingData.empty()
        )

    fun search(newQuery: String) {
        _filter.value = _filter.value.copy(query = newQuery)
    }

    fun updateFilter(modifier: WallhavenFilter.() -> WallhavenFilter) {
        _filter.value = _filter.value.modifier()
    }

    fun fetchWallpaperTags(wallpaperId: String) {
        viewModelScope.launch {
            val response: List<WallhavenTag> = repository.getWallpaperTags(wallpaperId)
            _tags.value = response
        }
    }
}
