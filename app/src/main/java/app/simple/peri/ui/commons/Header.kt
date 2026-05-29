package app.simple.peri.ui.commons

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SdCard
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.ui.dialogs.autowallpaper.AutoWallpaperPageSelectionDialog
import app.simple.peri.ui.nav.Routes
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

val COMMON_PADDING = 16.dp

/**
 * Tracks whether the user is scrolling toward the top of a staggered grid.
 * Returns true when scrolling back up, false when scrolling deeper into the list.
 * Starts as true so the header is visible when the screen first opens.
 */
@Composable
fun LazyStaggeredGridState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    var isScrollingUp by remember(this) { mutableStateOf(true) }

    LaunchedEffect(firstVisibleItemIndex, firstVisibleItemScrollOffset) {
        when {
            previousIndex != firstVisibleItemIndex -> isScrollingUp = previousIndex > firstVisibleItemIndex
            previousOffset != firstVisibleItemScrollOffset -> isScrollingUp = previousOffset > firstVisibleItemScrollOffset
        }
        previousIndex = firstVisibleItemIndex
        previousOffset = firstVisibleItemScrollOffset
    }

    return isScrollingUp
}

/**
 * Same scroll direction tracking but for regular (non-staggered) grids.
 */
@SuppressLint("FrequentlyChangingValue")
@Composable
fun LazyGridState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    var isScrollingUp by remember(this) { mutableStateOf(true) }

    LaunchedEffect(firstVisibleItemIndex, firstVisibleItemScrollOffset) {
        when {
            previousIndex != firstVisibleItemIndex -> isScrollingUp = previousIndex > firstVisibleItemIndex
            previousOffset != firstVisibleItemScrollOffset -> isScrollingUp = previousOffset > firstVisibleItemScrollOffset
        }
        previousIndex = firstVisibleItemIndex
        previousOffset = firstVisibleItemScrollOffset
    }

    return isScrollingUp
}

/**
 * A header that floats above the grid content and slides away when the user scrolls deeper
 * into the list, then reappears when they scroll back up. It works in both top and bottom
 * positions depending on the user's preference setting.
 */
@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun AnchoredHeader(
        title: String,
        modifier: Modifier = Modifier,
        count: Int = 0,
        navController: NavController? = null,
        isHideSettings: Boolean = false,
        isHideAutoWallpaper: Boolean = false,
        isShowSearch: Boolean = false,
        onSearch: (() -> Unit)? = null,
        hazeState: HazeState,
        statusBarHeight: Dp = 0.dp,
        navigationBarHeight: Dp = 0.dp,
        isVisible: Boolean = true,
) {
    val isBottom = MainComposePreferences.getBottomHeader()
    val context = LocalContext.current
    val autoWallpaperScreenSelection = remember { mutableStateOf(false) }

    if (autoWallpaperScreenSelection.value) {
        AutoWallpaperPageSelectionDialog(
                onDismiss = { autoWallpaperScreenSelection.value = false },
                onOptionSelected = { option ->
                    when (option) {
                        context.getString(R.string.wallpaper_manager) -> navController?.navigate(Routes.AUTO_WALLPAPER)
                        context.getString(R.string.live_auto_wallpaper) -> navController?.navigate(Routes.LIVE_AUTO_WALLPAPER)
                    }
                }
        )
    }

    val enterAnimation = if (isBottom) slideInVertically { it } else slideInVertically { -it }
    val exitAnimation = if (isBottom) slideOutVertically { it } else slideOutVertically { -it }

    val topBarHeight = if (statusBarHeight == 0.dp) COMMON_PADDING else statusBarHeight
    val navBarHeight = if (navigationBarHeight == 0.dp) COMMON_PADDING else navigationBarHeight

    AnimatedVisibility(
            visible = isVisible,
            enter = enterAnimation,
            exit = exitAnimation,
            modifier = modifier,
    ) {
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(
                            elevation = 24.dp,
                            spotColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ambientColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    .hazeEffect(state = hazeState, style = HazeMaterials.regular())
        ) {
            if (isBottom) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }

            Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                            top = if (isBottom) COMMON_PADDING else topBarHeight,
                            bottom = if (isBottom) navBarHeight else COMMON_PADDING,
                            start = COMMON_PADDING,
                            end = COMMON_PADDING
                    )
            ) {
                Text(
                        text = title,
                        textAlign = TextAlign.Start,
                        fontSize = 32.sp,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = COMMON_PADDING, end = 8.dp),
                        fontWeight = FontWeight.Bold,
                        lineHeight = 36.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                )

                if (count > 0) {
                    Text(
                            text = count.toString(),
                            textAlign = TextAlign.End,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(end = 8.dp),
                            fontWeight = FontWeight.Thin,
                    )
                }

                if (isHideAutoWallpaper.not()) {
                    IconButton(onClick = { autoWallpaperScreenSelection.value = true }) {
                        Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null)
                    }
                }

                if (isHideSettings.not()) {
                    IconButton(onClick = { navController?.navigate(Routes.SETTINGS) }) {
                        Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = stringResource(id = R.string.settings),
                        )
                    }
                }

                if (isShowSearch) {
                    IconButton(
                            onClick = { onSearch?.invoke() },
                            modifier = Modifier.padding(end = COMMON_PADDING),
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = stringResource(id = R.string.settings),
                        )
                    }
                }
            }

            if (!isBottom) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            }
        }
    }
}

@Composable
fun TopHeader(title: String,
              modifier: Modifier = Modifier,
              count: Int = 0,
              navController: NavController? = null,
              isHideSettings: Boolean = false,
              isShowSearch: Boolean = false,
              isHideAutoWallpaper: Boolean = false,
              isShowAdd: Boolean = false,
              isShowSdcard: Boolean = false,
              onSearch: (() -> Unit)? = null,
              onAdd: (() -> Unit)? = null,
              onSdcard: (() -> Unit)? = null) {

    val context = LocalContext.current
    val autoWallpaperScreenSelection = remember { mutableStateOf(false) }

    if (autoWallpaperScreenSelection.value) {
        AutoWallpaperPageSelectionDialog(
                onDismiss = {
                    autoWallpaperScreenSelection.value = false
                },
                onOptionSelected = { option ->
                    when (option) {
                        context.getString(R.string.wallpaper_manager) -> {
                            navController?.navigate(Routes.AUTO_WALLPAPER)
                        }

                        context.getString(R.string.live_auto_wallpaper) -> {
                            navController?.navigate(Routes.LIVE_AUTO_WALLPAPER)
                        }
                    }
                }
        )
    }

    Row(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = title,
                textAlign = TextAlign.Start,
                fontSize = 32.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
        )

        if (count > 0) {
            Text(
                    text = count.toString(),
                    textAlign = TextAlign.End,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(end = 8.dp),
                    fontWeight = FontWeight.Thin,
            )
        }

        if (isHideAutoWallpaper.not()) {
            IconButton(
                    onClick = {
                        autoWallpaperScreenSelection.value = true
                    },
            ) {
                Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null
                )
            }
        }

        if (isHideSettings.not()) {
            IconButton(
                    onClick = {
                        navController?.navigate(Routes.SETTINGS)
                    },
            ) {
                Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = stringResource(id = R.string.settings),
                )
            }
        }

        if (isShowSearch) {
            IconButton(
                    onClick = {
                        onSearch?.invoke()
                    },
            ) {
                Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(id = R.string.settings),
                )
            }
        }

        if (isShowSdcard) {
            IconButton(
                    onClick = {
                        onSdcard?.invoke()
                    },
            ) {
                Icon(
                        imageVector = Icons.Rounded.SdCard,
                        contentDescription = "",
                )
            }
        }

        if (isShowAdd) {
            IconButton(
                    onClick = {
                        onAdd?.invoke()
                    },
            ) {
                Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(id = R.string.create_new_folder),
                )
            }
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun BottomHeader(title: String,
                 modifier: Modifier = Modifier,
                 count: Int = 0,
                 navController: NavController? = null,
                 isSettings: Boolean = false,
                 isSearch: Boolean = false,
                 isAutoWallpaper: Boolean = false,
                 onSearch: (() -> Unit)? = null,
                 hazeState: HazeState,
                 navigationBarHeight: Dp
) {

    val context = LocalContext.current
    val autoWallpaperScreenSelection = remember { mutableStateOf(false) }

    if (autoWallpaperScreenSelection.value) {
        AutoWallpaperPageSelectionDialog(
                onDismiss = {
                    autoWallpaperScreenSelection.value = false
                },
                onOptionSelected = { option ->
                    when (option) {
                        context.getString(R.string.wallpaper_manager) -> {
                            navController?.navigate(Routes.AUTO_WALLPAPER)
                        }

                        context.getString(R.string.live_auto_wallpaper) -> {
                            navController?.navigate(Routes.LIVE_AUTO_WALLPAPER)
                        }
                    }
                }
        )
    }

    val navHeight = if (navigationBarHeight == 0.dp) {
        COMMON_PADDING
    } else {
        navigationBarHeight
    }

    Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(elevation = 24.dp,
                        spotColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ambientColor = MaterialTheme.colorScheme.surfaceVariant)
                .hazeEffect(
                        state = hazeState,
                        style = HazeMaterials.regular()
                ),
    ) {
        HorizontalDivider(
                color = MaterialTheme.colorScheme.surfaceVariant,
        )

        Row(
                verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                    text = title,
                    textAlign = TextAlign.Start,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp, bottom = navHeight, start = COMMON_PADDING, top = COMMON_PADDING),
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
            )

            if (count > 0) {
                Text(
                        text = count.toString(),
                        textAlign = TextAlign.End,
                        fontSize = 24.sp,
                        modifier = Modifier
                            .wrapContentWidth()
                            .padding(end = 8.dp, bottom = navHeight, top = COMMON_PADDING),
                        fontWeight = FontWeight.Thin,
                )
            }

            if (isAutoWallpaper.not()) {
                IconButton(
                        onClick = {
                            autoWallpaperScreenSelection.value = true
                        },
                        modifier = Modifier.padding(end = 4.dp, bottom = navHeight, top = COMMON_PADDING),
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null
                    )
                }
            }

            if (isSettings.not()) {
                IconButton(
                        onClick = {
                            navController?.navigate(Routes.SETTINGS)
                        },
                        modifier = Modifier.padding(
                                end = if (isSearch) 4.dp else COMMON_PADDING,
                                bottom = navHeight,
                                top = COMMON_PADDING),
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(id = R.string.settings),
                    )
                }
            }

            if (isSearch) {
                IconButton(
                        onClick = {
                            onSearch?.invoke()
                        },
                        modifier = Modifier.padding(end = COMMON_PADDING, bottom = navHeight, top = COMMON_PADDING),
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = stringResource(id = R.string.settings),
                    )
                }
            }
        }
    }
}
