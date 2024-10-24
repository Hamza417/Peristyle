package app.simple.peri.compose.subscreens

import android.util.Log
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.commons.BottomHeader
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.SelectionMenu
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.commons.WallpaperItem
import app.simple.peri.compose.dialogs.common.PleaseWaitDialog
import app.simple.peri.compose.nav.Routes
import app.simple.peri.factories.FolderViewModelFactory
import app.simple.peri.models.Folder
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.viewmodels.FolderDataViewModel
import app.simple.peri.viewmodels.WallpaperListViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun WallpaperList(navController: NavController? = null) {
    val folder =
        navController?.previousBackStackEntry?.savedStateHandle?.get<Folder>(Routes.FOLDER_ARG)

    if (folder != null) {
        val folderDataViewModel: FolderDataViewModel = viewModel(
                factory = FolderViewModelFactory(hashCode = folder)
        )

        var wallpapers = remember { mutableStateListOf<Wallpaper>() }
        val updatedWallpapers by folderDataViewModel.getWallpapers().collectAsState()

        // Update the wallpapers list
        wallpapers.clear()
        wallpapers = updatedWallpapers.toMutableStateList()
        Log.d("WallpaperList", "Updated wallpapers: ${updatedWallpapers.size}")

        if (wallpapers.isEmpty()) { // TODO = list doesn't update with this
            Log.e("WallpaperList", "Wallpapers list is empty")
            return
        }

        val wallpaperListViewModel: WallpaperListViewModel = viewModel() // We should use a dedicated ViewModel for this
        val isSelectionMode by wallpaperListViewModel.isSelectionMode.collectAsState()
        val selectionCount by wallpaperListViewModel.selectedWallpapers.collectAsState()
        var statusBarHeight by remember { mutableIntStateOf(0) }
        var navigationBarHeight by remember { mutableIntStateOf(0) }
        var showPleaseWaitDialog by remember { mutableStateOf(false) }
        val hazeState = remember { HazeState() }

        statusBarHeight = WindowInsetsCompat.toWindowInsetsCompat(LocalView.current.rootWindowInsets)
            .getInsets(WindowInsetsCompat.Type.statusBars()).top
        navigationBarHeight = WindowInsetsCompat.toWindowInsetsCompat(LocalView.current.rootWindowInsets)
            .getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

        val statusBarHeightPx = statusBarHeight
        val statusBarHeightDp = with(LocalDensity.current) { statusBarHeightPx.toDp() }
        val navigationBarHeightPx = navigationBarHeight
        val navigationBarHeightDp = with(LocalDensity.current) { navigationBarHeightPx.toDp() }
        var bottomHeaderHeight by remember { mutableStateOf(0.dp) }

        val topPadding = 8.dp + statusBarHeightDp
        val bottomPadding = 8.dp + if (MainComposePreferences.getBottomHeader()) {
            bottomHeaderHeight
        } else {
            navigationBarHeightDp
        }

        if (showPleaseWaitDialog) {
            PleaseWaitDialog {
                Log.i("WallpaperList", "Please wait dialog dismissed")
            }
        }

        Box(
                modifier = Modifier.fillMaxSize()
                // Uncomment this line if blurring is needed
                // .then(if (showPleaseWaitDialog) Modifier.blur(8.dp) else Modifier)
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
                            bottom = bottomPadding
                    )
            ) {
                if (MainComposePreferences.getBottomHeader().invert()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        TopHeader(
                                title = folder.name ?: stringResource(R.string.unknown),
                                count = wallpapers.size,
                                modifier = Modifier.padding(COMMON_PADDING),
                                navController = navController
                        )
                    }
                }
                items(wallpapers.size, key = { wallpapers[it].hashCode() }) { index ->
                    WallpaperItem(
                            wallpaper = wallpapers[index],
                            navController = navController,
                            onDelete = { deletedWallpaper ->
                                folderDataViewModel.deleteWallpaper(deletedWallpaper)
                            },
                            onCompress = {
                                showPleaseWaitDialog = true
                                folderDataViewModel.compressWallpaper(wallpapers[index]) {
                                    showPleaseWaitDialog = false
                                    Log.d("WallpaperList", "Compressed wallpaper: ${wallpapers[index].size.toSize()} -> ${it.size.toSize()}")
                                }
                            },
                            onReduceResolution = {
                                showPleaseWaitDialog = true
                                folderDataViewModel.reduceResolution(wallpapers[index]) {
                                    showPleaseWaitDialog = false
                                    Log.d("WallpaperList", "Reduced wallpaper: ${it.size.toSize()}, ${it.height}x${it.width}")
                                }
                            },
                            isSelectionMode = isSelectionMode,
                            wallpaperListViewModel = wallpaperListViewModel,
                            list = wallpapers
                    )
                }
            }

            if (isSelectionMode) {
                SelectionMenu(
                        list = wallpapers,
                        count = selectionCount,
                        modifier = Modifier
                            .align(Alignment.BottomCenter),
                        hazeState = hazeState,
                        wallpaperListViewModel = wallpaperListViewModel,
                        navigationBarHeight = bottomPadding
                )
            }

            if (MainComposePreferences.getBottomHeader()) {
                val density = LocalDensity.current

                BottomHeader(
                        title = folder.name ?: stringResource(R.string.unknown),
                        count = wallpapers.size,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .onGloballyPositioned {
                                bottomHeaderHeight = with(density) { it.size.height.toDp() }
                            },
                        navController = navController,
                        hazeState = hazeState,
                        navigationBarHeight = navigationBarHeightDp,
                        statusBarHeight = statusBarHeightDp
                )
            }
        }
    } else {
        Log.e("WallpaperList", "Folder is null")
    }
}
