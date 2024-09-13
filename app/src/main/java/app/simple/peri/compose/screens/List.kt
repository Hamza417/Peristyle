package app.simple.peri.compose.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.compose.commons.Wallpapers
import app.simple.peri.models.Wallpaper
import app.simple.peri.viewmodels.WallpaperViewModel

@Composable
fun List(navController: NavController? = null) {
    val wallpaperViewModel: WallpaperViewModel = viewModel()
    var wallpapers by remember { mutableStateOf(emptyList<Wallpaper>()) }
    var loadingState by remember { mutableStateOf("") }

    wallpaperViewModel.getWallpapersLiveData().observeAsState().value?.let {
        wallpapers = it
    }

    wallpaperViewModel.getLoadingStatusLiveData().observeAsState().value?.let {
        loadingState = it
    }

    Wallpapers(wallpapers, navController, loadingState)
}
