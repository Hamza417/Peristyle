package app.simple.peri.ui.screens

import android.content.res.Configuration
import android.graphics.drawable.Drawable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import app.simple.peri.R
import app.simple.peri.activities.main.LocalDisplaySize
import app.simple.peri.models.WallhavenFilter
import app.simple.peri.models.WallhavenResponse
import app.simple.peri.models.WallhavenWallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.ui.commons.BottomHeader
import app.simple.peri.ui.commons.COMMON_PADDING
import app.simple.peri.ui.commons.TopHeader
import app.simple.peri.ui.commons.WallpaperDimensionsText
import app.simple.peri.ui.dialogs.wallhaven.WallhavenSearchDialog
import app.simple.peri.ui.nav.Routes
import app.simple.peri.ui.theme.LocalBarsSize
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.viewmodels.WallhavenViewModel
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@Composable
fun WallhavenScreen(navController: NavController? = null) {

    val viewModel: WallhavenViewModel = hiltViewModel()
    val wallpapers = viewModel.wallpapers.collectAsLazyPagingItems()
    val savedStateHandle = navController?.previousBackStackEntry?.savedStateHandle
    val meta: WallhavenResponse.Meta? by viewModel.meta.collectAsStateWithLifecycle()
    var presetFilter by remember { mutableStateOf(savedStateHandle?.get<WallhavenFilter>(Routes.WALLHAVEN_ARG)) }

    LaunchedEffect(presetFilter) {
        viewModel.updateFilter {
            copy(
                    atleast = presetFilter!!.atleast,
                    ratios = presetFilter!!.ratios,
                    resolution = presetFilter!!.resolution,
                    purity = presetFilter!!.purity,
                    categories = presetFilter!!.categories,
                    query = presetFilter!!.query,
                    sorting = presetFilter!!.sorting,
                    order = presetFilter!!.order
            )
        }
    }

    val hazeState = remember { HazeState() }
    var searchDialog by remember { mutableStateOf(false) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var bottomHeaderHeight by remember { mutableStateOf(0.dp) }

    val topPadding = 8.dp + LocalBarsSize.current.statusBarHeight
    val bottomPadding = 8.dp + if (MainComposePreferences.getBottomHeader()) {
        bottomHeaderHeight
    } else {
        LocalBarsSize.current.navigationBarHeight
    }

    if (searchDialog) {
        WallhavenSearchDialog(
                filter = presetFilter,
                onDismiss = { searchDialog = false },
                onSearch = {
                    viewModel.updateFilter {
                        copy(
                                query = it.query,
                                categories = it.categories,
                                purity = it.purity,
                                atleast = it.atleast,
                                resolution = it.resolution,
                                ratios = it.ratios,
                                sorting = it.sorting,
                                order = it.order
                        )
                    }

                    presetFilter = it
                    savedStateHandle?.set(Routes.WALLHAVEN_ARG, it)
                }
        )
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
                            navController = navController,
                            isSearch = true,
                            onSearch = {
                                searchDialog = true
                            },
                            count = meta?.total ?: 0,
                    )
                }
            }

            // Show current filter params
            item(span = StaggeredGridItemSpan.FullLine) {
                if (presetFilter != null) {
                    val filterParams = buildList {
                        add("${stringResource(R.string.query)}: ${presetFilter?.query ?: "All"}")
                        add("${stringResource(R.string.purity)}: ${presetFilter?.purity}")
                        add("${stringResource(R.string.category)}: ${presetFilter?.categories}")
                        add("${stringResource(R.string.ratios)}: ${presetFilter?.ratios}")
                        add("${stringResource(R.string.resolution)}: ${presetFilter?.resolution ?: "Any"}")
                        add("${stringResource(R.string.at_least)}: ${presetFilter?.atleast ?: "Any"}")
                        add("${stringResource(R.string.sort)}: ${presetFilter?.sorting ?: "Default"}")
                        add("${stringResource(R.string.order)}: ${presetFilter?.order ?: "Default"}")
                    }

                    LazyRow {
                        items(filterParams.size) { pos ->
                            AssistChip(
                                    onClick = {}, // non-clickable
                                    label = { Text(filterParams[pos]) },
                                    enabled = false,
                                    modifier = Modifier
                                        .padding(
                                                start = if (pos == 0) 8.dp else 4.dp,
                                                end = if (pos == filterParams.size - 1) 8.dp else 4.dp
                                        ),
                                    colors = AssistChipDefaults.assistChipColors(
                                            containerColor = Color.White.copy(alpha = 0.15f),
                                            labelColor = Color.Gray
                                    )
                            )
                        }
                    }
                }
            }

            items(wallpapers.itemCount) { index ->
                wallpapers[index]?.let { wallpaper ->
                    ImageCard(wallpaper, navController, presetFilter)
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
                            bottomHeaderHeight = with(density) {
                                it.size.height.toDp()
                            }
                        },
                    navController = navController,
                    hazeState = hazeState,
                    navigationBarHeight = LocalBarsSize.current.navigationBarHeight,
                    isSearch = true,
                    onSearch = {
                        searchDialog = true
                    },
                    count = meta?.total ?: 0,
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImageCard(wallpaper: WallhavenWallpaper, navController: NavController? = null, presetFilter: WallhavenFilter? = null) {
    val hazeState = remember { HazeState() }

    Box {
        val imageShadow = remember {
            MainComposePreferences.getShowImageShadow()
                    && MainComposePreferences.getMarginBetween()
        }

        if (imageShadow) {
            GlideImage(
                    model = wallpaper.originalUrl,
                    contentDescription = null,
                    transition = CrossFade,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(16.dp)
                        .blur(30.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                        .alpha(0.5f)
                        .graphicsLayer {
                            clip = false
                        }
                        .align(Alignment.BottomCenter),
                    alignment = Alignment.BottomCenter,
            ) {
                it.override(512, 512)
                    .transition(withCrossFade())
                    .disallowHardwareConfig()
                    .centerCrop()
            }
        }

        ElevatedCard(
                elevation = CardDefaults.cardElevation(
                        defaultElevation = if (imageShadow) 24.dp else 0.dp,
                ),
                modifier = Modifier
                    .aspectRatio(wallpaper.aspectRatio.toFloat())
                    .padding(
                            start = if (MainComposePreferences.getMarginBetween()) 8.dp else 0.dp,
                            bottom = if (imageShadow) 35.dp else if (MainComposePreferences.getMarginBetween()) 8.dp else 0.dp,
                            end = if (MainComposePreferences.getMarginBetween()) 8.dp else 0.dp,
                            top = if (MainComposePreferences.getMarginBetween()) 8.dp else 0.dp
                    )
                    .shadow(
                            if (imageShadow) 0.dp else 24.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false,
                            spotColor = try {
                                Color(wallpaper.colors[0].toColorInt())
                            } catch (_: IndexOutOfBoundsException) {
                                Color(0xFF444444)
                            },
                            ambientColor = try {
                                Color(wallpaper.colors[1].toColorInt())
                            } catch (_: IndexOutOfBoundsException) {
                                Color(0xFF444444)
                            }
                    )
                    .combinedClickable(
                            onClick = {
                                navController?.navigate(Routes.WALLPAPER) {
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle?.set(Routes.WALLPAPER_ARG, wallpaper)
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle?.set(Routes.WALLHAVEN_FILTER, presetFilter)
                                }
                            },
                    ),
                shape = if (MainComposePreferences.getMarginBetween()) {
                    RoundedCornerShape(16.dp)
                } else {
                    RoundedCornerShape(0.dp)
                },
        ) {
            Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
            ) {
                GlideImage(
                        model = wallpaper.originalUrl,
                        contentDescription = null,
                        transition = CrossFade,
                        modifier = Modifier
                            .hazeSource(hazeState)
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

                if (MainComposePreferences.getWallpaperDetails()) {
                    Column(
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .hazeEffect(
                                        state = hazeState,
                                        style = HazeDefaults.style(
                                                backgroundColor = Color(0x50000000),
                                                blurRadius = 5.dp
                                        )
                                )
                                .align(Alignment.BottomCenter)
                    ) {
                        Text(
                                text = wallpaper.path.substringAfterLast("/"),
                                modifier = Modifier
                                    .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                                textAlign = TextAlign.Start,
                                fontSize = 18.sp, // Set the font size
                                fontWeight = FontWeight.Bold, // Make the text bold
                                color = Color.White, // Set the text color
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                softWrap = false,
                        )

                        WallpaperDimensionsText(wallpaper, LocalDisplaySize.current.width, LocalDisplaySize.current.height)
                    }
                }
            }
        }
    }
}