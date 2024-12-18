package app.simple.peri.compose.subscreens

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.compose.commons.BottomHeader
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.SelectionMenu
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.commons.WallpaperItem
import app.simple.peri.compose.dialogs.common.PleaseWaitDialog
import app.simple.peri.compose.dialogs.common.PostScalingChangeDialog
import app.simple.peri.compose.nav.Routes
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.PostWallpaperData
import app.simple.peri.models.Tag
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.viewmodels.StateViewModel
import app.simple.peri.viewmodels.TagsViewModel
import app.simple.peri.viewmodels.WallpaperListViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze

@Composable
fun TaggedWallpapers(navController: NavController? = null) {
    val stateViewModel: StateViewModel = viewModel()
    val tag = navController?.previousBackStackEntry?.savedStateHandle?.get<Tag>(Routes.TAG_ARG) ?: stateViewModel.tag ?: return
    stateViewModel.tag = tag

    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory(
                    tag = tag.name
            )
    )

    var wallpapers = remember { mutableStateListOf<Wallpaper>() }

    tagsViewModel.getWallpapers().observe(LocalLifecycleOwner.current) { updatedWallpapers ->
        wallpapers.clear()
        wallpapers = updatedWallpapers.toMutableStateList()
    }

    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }
    val hazeState = remember { HazeState() }
    val wallpaperListViewModel: WallpaperListViewModel =
        viewModel() // We should use a dedicated ViewModel for this
    val isSelectionMode by wallpaperListViewModel.isSelectionMode.collectAsState()
    val selectionCount by wallpaperListViewModel.selectedWallpapers.collectAsState()
    var showPleaseWaitDialog by remember { mutableStateOf(false) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val wallpaperData = remember { mutableStateOf<PostWallpaperData?>(null) }
    var showWallpaperComparisonDialog by remember { mutableStateOf(false) }

    statusBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets
    ).getInsets(WindowInsetsCompat.Type.statusBars()).top
    navigationBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets
    ).getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

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

    if (showWallpaperComparisonDialog) {
        PostScalingChangeDialog(
                onDismiss = {
                    showWallpaperComparisonDialog = false
                },
                postWallpaperData = wallpaperData.value!!
        )
    }

    Box(
            modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(MainComposePreferences.getGridSpanCount(isLandscape)),
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
            if (MainComposePreferences.getBottomHeader().not()) {
                item(span = StaggeredGridItemSpan.FullLine) {
                    TopHeader(
                            title = tag.name, count = wallpapers.size,
                            modifier = Modifier.padding(COMMON_PADDING),
                            navController = navController
                    )
                }
            }
            items(wallpapers.size) { index ->
                WallpaperItem(
                        wallpaper = wallpapers[index],
                        navController = navController,
                        onDelete = { deletedWallpaper ->
                            tagsViewModel.deleteWallpaper(deletedWallpaper)
                        },
                        onCompress = {
                            val postWallpaperData = PostWallpaperData().apply {
                                oldSize = wallpapers[index].size
                                oldWidth = wallpapers[index].width ?: 0
                                oldHeight = wallpapers[index].height ?: 0
                            }
                            showPleaseWaitDialog = true

                            tagsViewModel.compressWallpaper(wallpapers[index]) { result ->
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

                            tagsViewModel.reduceResolution(wallpapers[index]) { result ->
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
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = navigationBarHeightDp),
                    selectedWallpapers = wallpapers,
                    count = selectionCount,
                    hazeState = hazeState,
                    wallpaperListViewModel = wallpaperListViewModel,
                    navigationBarHeight = bottomPadding
            )
        }

        if (MainComposePreferences.getBottomHeader()) {
            val density = LocalDensity.current

            BottomHeader(
                    title = tag.name,
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
