package app.simple.peri.compose.screens

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.compose.commons.Wallpapers
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Wallpaper
import app.simple.peri.viewmodels.TagsViewModel

@Composable
fun TaggedWallpapers(navController: NavController? = null, tag: String?) {
    Log.i("TaggedWallpapers", "Tag: $tag")
    var wallpapers by remember { mutableStateOf(emptyList<Wallpaper>()) }
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory(
                    application = requireNotNull(LocalContext.current.applicationContext as Application),
                    tag = tag
            )
    )

    tagsViewModel.getWallpapers().observeAsState().value?.let {
        wallpapers = it
    }

    Wallpapers(list = wallpapers, navController = navController)
}
