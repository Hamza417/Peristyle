package app.simple.peri.ui.screens

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import app.simple.peri.R
import app.simple.peri.models.WallhavenWallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.ui.commons.BottomHeader
import app.simple.peri.ui.commons.COMMON_PADDING
import app.simple.peri.ui.commons.TopHeader
import app.simple.peri.ui.nav.Routes
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.viewmodels.WallhavenViewModel
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

@Composable
fun WallhavenScreen(navController: NavController? = null) {

    val viewModel: WallhavenViewModel = hiltViewModel()
    val wallpapers = viewModel.wallpapers.collectAsLazyPagingItems()

    val hazeState = remember { HazeState() }
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }
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

    Box(
            modifier = Modifier.fillMaxSize()
            // Uncomment this line if blurring is needed
            // .then(if (showPleaseWaitDialog) Modifier.blur(8.dp) else Modifier)
    ) {
        LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(MainComposePreferences.getGridSpanCount(isLandscape)),
                state = viewModel.lazyGridState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
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
                            title = stringResource(R.string.wallhaven),
                            modifier = Modifier.padding(COMMON_PADDING),
                            navController = navController
                    )
                }
            }

            items(wallpapers.itemCount) { index ->
                wallpapers[index]?.let { wallpaper ->
                    ImageCard(wallpaper, navController)
                }
            }
        }

        if (MainComposePreferences.getBottomHeader()) {
            val density = LocalDensity.current

            BottomHeader(
                    title = stringResource(R.string.wallhaven),
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

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageCard(wallpaper: WallhavenWallpaper, navController: NavController? = null) {
    Card(
            modifier = Modifier
                .aspectRatio(wallpaper.resolution.let { it ->
                    val (width, height) = it.split("x").map { it.toFloat() }
                    width / height
                })
                .padding(8.dp),
            onClick = {
                navController?.navigate(Routes.WALLPAPER) {
                    navController.currentBackStackEntry
                        ?.savedStateHandle?.set(Routes.WALLPAPER_ARG, wallpaper)
                }
            }
    ) {
        GlideImage(
                model = wallpaper.thumbnailUrl,
                contentDescription = null,
                transition = CrossFade,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
        ) {
            it.addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
                .disallowHardwareConfig()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        }
    }
}