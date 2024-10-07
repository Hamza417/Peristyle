package app.simple.peri.compose.subscreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.SelectionMenu
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.commons.WallpaperItem
import app.simple.peri.compose.nav.Routes
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Tag
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.viewmodels.TagsViewModel
import app.simple.peri.viewmodels.WallpaperListViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun TaggedWallpapers(navController: NavController? = null) {
    val tag = navController?.previousBackStackEntry?.savedStateHandle?.get<Tag>(Routes.TAG_ARG) ?: return

    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory(
                    tag = tag.name
            )
    )

    val wallpapers = tagsViewModel.getWallpapers().observeAsState().value ?: emptyList()
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }
    val hazeState = remember { HazeState() }
    val wallpaperListViewModel: WallpaperListViewModel = viewModel() // We should use a dedicated ViewModel for this
    val isSelectionMode by wallpaperListViewModel.isSelectionMode.collectAsState()
    val selectionCount by wallpaperListViewModel.selectedWallpapers.collectAsState()

    statusBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.statusBars()).top
    navigationBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

    val statusBarHeightPx = statusBarHeight
    val statusBarHeightDp = with(LocalDensity.current) { statusBarHeightPx.toDp() }
    val navigationBarHeightPx = navigationBarHeight
    val navigationBarHeightDp = with(LocalDensity.current) { navigationBarHeightPx.toDp() }

    val topPadding = 8.dp + statusBarHeightDp
    val bottomPadding = 8.dp + navigationBarHeightDp

    Box(
            modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
                columns = GridCells.Fixed(MainComposePreferences.getGridSpanCount()),
                state = wallpaperListViewModel.lazyGridState,
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
                contentPadding = PaddingValues(
                        top = topPadding,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = bottomPadding)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TopHeader(title = tag.name, count = wallpapers.size,
                          modifier = Modifier.padding(COMMON_PADDING),
                          navController = navController)
            }
            items(wallpapers.size) { index ->
                WallpaperItem(wallpaper = wallpapers[index],
                              navController = navController,
                              onDelete = { deletedWallpaper ->
                                  tagsViewModel.deleteWallpaper(deletedWallpaper)
                              },
                              isSelectionMode = isSelectionMode,
                              wallpaperListViewModel = wallpaperListViewModel,
                              list = wallpapers)
            }
        }

        if (isSelectionMode) {
            SelectionMenu(list = wallpapers,
                          count = selectionCount,
                          modifier = Modifier.align(Alignment.BottomCenter),
                          hazeState = hazeState,
                          wallpaperListViewModel = wallpaperListViewModel)
        }
    }
}
