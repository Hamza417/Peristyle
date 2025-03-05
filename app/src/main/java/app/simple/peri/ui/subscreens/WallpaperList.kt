package app.simple.peri.ui.subscreens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.factories.FolderViewModelFactory
import app.simple.peri.models.Folder
import app.simple.peri.models.PostWallpaperData
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.ui.commons.BottomHeader
import app.simple.peri.ui.commons.COMMON_PADDING
import app.simple.peri.ui.commons.SelectionMenu
import app.simple.peri.ui.commons.TextWithIcon
import app.simple.peri.ui.commons.TopHeader
import app.simple.peri.ui.commons.WallpaperItem
import app.simple.peri.ui.dialogs.common.PleaseWaitDialog
import app.simple.peri.ui.dialogs.common.PostScalingChangeDialog
import app.simple.peri.ui.nav.Routes
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.viewmodels.FolderDataViewModel
import app.simple.peri.viewmodels.StateViewModel
import app.simple.peri.viewmodels.WallpaperListViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun WallpaperList(navController: NavController? = null) {
    val stateViewModel: StateViewModel = viewModel()
    val folder = navController?.previousBackStackEntry?.savedStateHandle?.get<Folder>(Routes.FOLDER_ARG)
        ?: stateViewModel.folder
        ?: return
    stateViewModel.folder = folder // Save the state

    val folderDataViewModel: FolderDataViewModel = viewModel(
            factory = FolderViewModelFactory(hashCode = folder)
    )

    var wallpapers = remember { mutableStateListOf<Wallpaper>() }
    val updatedWallpapers by folderDataViewModel.getWallpapers().collectAsState()

    // Update the wallpapers list
    wallpapers.clear()
    wallpapers = updatedWallpapers.toMutableStateList()

    val wallpaperData = remember { mutableStateOf<PostWallpaperData?>(null) }
    var showWallpaperComparisonDialog by remember { mutableStateOf(false) }

    if (showWallpaperComparisonDialog) {
        Log.i("WallpaperList", "Show wallpaper comparison dialog")
        PostScalingChangeDialog(
                onDismiss = {
                    showWallpaperComparisonDialog = false
                },
                postWallpaperData = wallpaperData.value!!
        )
    }

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
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

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
        LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(MainComposePreferences.getGridSpanCount(isLandscape)),
                state = wallpaperListViewModel.lazyGridState,
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
                contentPadding = PaddingValues(
                        top = if (MainComposePreferences.getBottomHeader()) {
                            if (MainComposePreferences.getMarginBetween()) {
                                topPadding
                            } else {
                                0.dp
                            }
                        } else {
                            topPadding
                        },
                        start = if (MainComposePreferences.getMarginBetween()) 8.dp else 0.dp,
                        end = if (MainComposePreferences.getMarginBetween()) 8.dp else 0.dp,
                        bottom = if (MainComposePreferences.getMarginBetween()) {
                            bottomPadding
                        } else {
                            bottomPadding - 8.dp
                        }
                )
        ) {
            if (MainComposePreferences.getBottomHeader().invert()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    TopHeader(
                            title = folder.name ?: stringResource(R.string.unknown),
                            count = wallpapers.size,
                            modifier = Modifier.padding(COMMON_PADDING),
                            navController = navController
                    )
                }
            }

            if (MainComposePreferences.getShowWarningIndicator() && MainComposePreferences.getWallpaperDetails()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    Column {
                        TextWithIcon(
                                imageVector = Icons.Rounded.Warning,
                                tint = Color.Red,
                                text = stringResource(R.string.insufficient_wallpaper),
                                modifier = Modifier.padding(start = COMMON_PADDING, end = COMMON_PADDING)
                        )

                        TextWithIcon(
                                imageVector = Icons.Rounded.Warning,
                                tint = Color.LightGray,
                                text = stringResource(R.string.excessive_wallpaper),
                                modifier = Modifier.padding(start = COMMON_PADDING, end = COMMON_PADDING, top = 8.dp, bottom = COMMON_PADDING)
                        )
                    }
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
                            val postWallpaperData = PostWallpaperData().apply {
                                oldSize = wallpapers[index].size
                                oldWidth = wallpapers[index].width ?: 0
                                oldHeight = wallpapers[index].height ?: 0
                            }
                            showPleaseWaitDialog = true

                            folderDataViewModel.compressWallpaper(wallpapers[index]) { result ->
                                showPleaseWaitDialog = false
                                result.let {
                                    postWallpaperData.apply {
                                        newSize = it.size
                                        newWidth = it.width ?: 0
                                        newHeight = it.height ?: 0
                                        path = it.filePath
                                    }
                                    wallpaperData.value = postWallpaperData
                                    showWallpaperComparisonDialog = true
                                }
                            }
                        },
                        onReduceResolution = {
                            val postWallpaperData = PostWallpaperData().apply {
                                oldSize = wallpapers[index].size
                                oldWidth = wallpapers[index].width ?: 0
                                oldHeight = wallpapers[index].height ?: 0
                            }
                            showPleaseWaitDialog = true

                            folderDataViewModel.reduceResolution(wallpapers[index]) { result ->
                                showPleaseWaitDialog = false
                                result.let {
                                    postWallpaperData.apply {
                                        newSize = it.size
                                        newWidth = it.width ?: 0
                                        newHeight = it.height ?: 0
                                        path = it.filePath
                                    }
                                    wallpaperData.value = postWallpaperData
                                    showWallpaperComparisonDialog = true
                                }
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
                    selectedWallpapers = wallpapers.filter { it.isSelected },
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
}
