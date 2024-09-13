package app.simple.peri.compose.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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

    Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Wallpapers(wallpapers, navController, loadingState)

        if (loadingState.isEmpty()) {
            Log.d("List", "Loading state: $loadingState")
            Text(
                    text = loadingState,
                    style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = androidx.compose.ui.graphics.Color.Black
                    )
            )
        }
    }
}
