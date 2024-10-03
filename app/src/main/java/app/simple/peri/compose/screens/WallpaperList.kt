package app.simple.peri.compose.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.compose.commons.WallpapersList
import app.simple.peri.compose.nav.Routes
import app.simple.peri.factories.FolderViewModelFactory
import app.simple.peri.models.Folder
import app.simple.peri.models.Wallpaper
import app.simple.peri.viewmodels.FolderDataViewModel

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WallpaperList(navController: NavController? = null,
                  sharedTransitionScope: SharedTransitionScope,
                  animatedContentScope: AnimatedContentScope) {

    val folder = navController?.previousBackStackEntry?.savedStateHandle?.get<Folder>(Routes.FOLDER_ARG)

    // Ensure folder is not null before proceeding
    folder?.let {
        val folderDataViewModel: FolderDataViewModel = viewModel(
                factory = FolderViewModelFactory(hashCode = it)
        )
        var wallpapers by remember { mutableStateOf(emptyList<Wallpaper>()) }

        folderDataViewModel.getWallpapers().observeAsState().value?.let { newWallpapers ->
            wallpapers = newWallpapers
        }

        WallpapersList(wallpapers,
                       navController,
                       title = it.name,
                       folder = it,
                       sharedTransitionScope = sharedTransitionScope,
                       animatedContentScope = animatedContentScope)
    }
}
