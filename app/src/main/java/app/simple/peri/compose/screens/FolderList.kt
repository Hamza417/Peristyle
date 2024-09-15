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
import app.simple.peri.compose.nav.Routes
import app.simple.peri.factories.FolderViewModelFactory
import app.simple.peri.models.Folder
import app.simple.peri.models.Wallpaper
import app.simple.peri.viewmodels.FolderDataViewModel

@Composable
fun FolderList(navController: NavController? = null) {
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

        Wallpapers(wallpapers, navController, title = it.name)
    }
}
