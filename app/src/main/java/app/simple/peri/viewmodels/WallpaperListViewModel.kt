package app.simple.peri.viewmodels

import android.app.Application
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.simple.peri.database.instances.WallpaperDatabase
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.FileUtils.toFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WallpaperListViewModel(application: Application) : AndroidViewModel(application) {
    var lazyGridState: LazyStaggeredGridState by mutableStateOf(LazyStaggeredGridState(0, 0))

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> get() = _isSelectionMode

    fun setSelectionMode(isSelectionMode: Boolean) {
        _isSelectionMode.value = isSelectionMode
    }

    private val _selectedWallpapers = MutableStateFlow(0)
    val selectedWallpapers: StateFlow<Int> get() = _selectedWallpapers

    fun setSelectedWallpapers(selectedWallpapers: Int) {
        _selectedWallpapers.value = selectedWallpapers
    }

    private val _wallpapers = MutableStateFlow(mutableListOf<Wallpaper>())
    val wallpapers: StateFlow<MutableList<Wallpaper>> get() = _wallpapers

    fun addWallpapers(wallpapers: List<Wallpaper>) {
        _wallpapers.value.addAll(wallpapers)
    }

    fun deleteSelectedWallpapers(list: MutableList<Wallpaper>) {
        viewModelScope.launch(Dispatchers.IO) {
            val wallpaperDatabase = WallpaperDatabase.getInstance(getApplication())
            val wallpaperDao = wallpaperDatabase?.wallpaperDao()

            list.forEach {
                if (it.isSelected) {
                    if (it.filePath.toFile().delete()) {
                        _selectedWallpapers.value--
                        wallpaperDao?.delete(it)
                    }
                }
            }

            list.removeAll { it.isSelected }
            _selectedWallpapers.value = 0
            _isSelectionMode.value = false
            _wallpapers.value = list
        }
    }

    fun resetSelectedWallpapersState() {
        _wallpapers.value.forEach { it.isSelected = false }
        _selectedWallpapers.value = 0
        _isSelectionMode.value = false
    }

    fun selectFromLastToCurrent(wallpaper: Wallpaper, wallpapers: List<Wallpaper>) {
        val currentIndex = wallpapers.indexOf(wallpaper)
        val lastSelectedIndex = wallpapers.indexOfLast { it.isSelected }
        val firstSelectedIndex = wallpapers.indexOfFirst { it.isSelected }

        when {
            currentIndex > lastSelectedIndex -> {
                for (i in lastSelectedIndex..currentIndex) {
                    wallpapers[i].isSelected = true
                }
            }
            currentIndex < firstSelectedIndex -> {
                for (i in currentIndex..firstSelectedIndex) {
                    wallpapers[i].isSelected = true
                }
            }
            else -> { // I am not sure why it's here
                for (i in currentIndex..lastSelectedIndex) {
                    wallpapers[i].isSelected = true
                }
            }
        }

        _selectedWallpapers.value = wallpapers.count { it.isSelected }
    }
}
