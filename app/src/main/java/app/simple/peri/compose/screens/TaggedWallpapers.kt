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
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Wallpaper
import app.simple.peri.viewmodels.TagsViewModel

@Composable
fun TaggedWallpapers(navController: NavController? = null, tag: String?) {
    var wallpapers by remember { mutableStateOf(emptyList<Wallpaper>()) }
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory(
                    tag = tag
            )
    )

    tagsViewModel.getWallpapers().observeAsState().value?.let {
        wallpapers = it
    }

    Wallpapers(list = wallpapers, navController = navController)
}
