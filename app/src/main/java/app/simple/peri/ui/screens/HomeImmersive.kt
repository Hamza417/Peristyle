package app.simple.peri.ui.screens

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MotionPhotosOn
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.data.Page
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.ui.commons.CircularCountdownProgress
import app.simple.peri.ui.commons.CircularIconButton
import app.simple.peri.ui.commons.InitDisplayDimension
import app.simple.peri.ui.dialogs.autowallpaper.AutoWallpaperPageSelectionDialog
import app.simple.peri.ui.dialogs.common.SureDialog
import app.simple.peri.ui.dialogs.wallhaven.WallhavenSearchDialog
import app.simple.peri.ui.nav.Routes
import app.simple.peri.ui.theme.LocalBarsSize
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.utils.ServiceUtils
import app.simple.peri.viewmodels.HomeScreenViewModel
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

const val RANDOM_WALLPAPER_POSITION_FS = 0
const val HOME_SCREEN_POSITION_FS = 1
const val LOCK_SCREEN_POSITION_FS = 2
const val LIVE_AUTO_WALLPAPER_POSITION_FS = 3

@Composable
fun ImmersiveHome(navController: NavController? = null) {
    InitDisplayDimension()

    val applicationContext = LocalContext.current.applicationContext
    val isLiveWallpaperRunning = remember {
        mutableStateOf(ServiceUtils.isWallpaperServiceRunning(applicationContext))
    }

    val pages = listOf(
            Page(RANDOM_WALLPAPER_POSITION_FS, true),
            Page(HOME_SCREEN_POSITION_FS, true),
            Page(LOCK_SCREEN_POSITION_FS, MainComposePreferences.getShowLockScreenWallpaper()),
            Page(LIVE_AUTO_WALLPAPER_POSITION_FS, isLiveWallpaperRunning.value)
    )

    val pagerState = rememberPagerState(pageCount = {
        pages.count { it.isVisible }
    })

    val fling = PagerDefaults.flingBehavior(
            state = pagerState,
            pagerSnapDistance = PagerSnapDistance.atMost(10)
    )

    val homeScreenViewModel: HomeScreenViewModel = viewModel(LocalActivity.current as ComponentActivity)

    val systemWallpaper = homeScreenViewModel.getSystemWallpaper().observeAsState().value
    val lockWallpaper = homeScreenViewModel.getLockWallpaper().observeAsState().value
    val randomWallpaper = homeScreenViewModel.getRandomWallpaper().observeAsState().value
    val lastLiveWallpaper = homeScreenViewModel.getLastLiveWallpaper().observeAsState().value
    val hazeState = remember { HazeState() }

    DisposableEffect(ProcessLifecycleOwner.get()) {
        val observer = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                homeScreenViewModel.stopCountDownFlow()
                Log.i("HomeScreen", "onPause")
            }

            override fun onResume(owner: LifecycleOwner) {
                if (!homeScreenViewModel.isCountdownPaused.value) {
                    homeScreenViewModel.resumeCountDownFlow()
                }
                isLiveWallpaperRunning.value = ServiceUtils.isWallpaperServiceRunning(applicationContext)
                Log.i("HomeScreen", "onResume")
            }

            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                Log.i("HomeScreen", "onDestroy")
            }
        }

        val lifecycle = ProcessLifecycleOwner.get().lifecycle
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val visiblePages = pages.filter { it.isVisible }

        HorizontalPager(
                state = pagerState,
                flingBehavior = fling,
                modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val currentPage = visiblePages[pageIndex]
            val wallpaper = when (currentPage.id) {
                HOME_SCREEN_POSITION_FS -> systemWallpaper
                LOCK_SCREEN_POSITION_FS -> lockWallpaper
                LIVE_AUTO_WALLPAPER_POSITION_FS -> lastLiveWallpaper
                else -> randomWallpaper
            }

            ImmersiveWallpaperItem(
                    position = currentPage.id,
                    title = when (currentPage.id) {
                        HOME_SCREEN_POSITION_FS -> stringResource(id = R.string.home_screen)
                        LOCK_SCREEN_POSITION_FS -> stringResource(id = R.string.lock_screen)
                        LIVE_AUTO_WALLPAPER_POSITION_FS -> stringResource(id = R.string.live_auto_wallpaper)
                        else -> wallpaper?.name ?: ""
                    },
                    hazeState = hazeState,
                    onClick = {
                        if (wallpaper != null) {
                            navController?.navigate(Routes.WALLPAPER) {
                                navController.currentBackStackEntry?.savedStateHandle?.set(Routes.WALLPAPER_ARG, wallpaper)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize(),
                    wallpaper = wallpaper,
                    onNextWallpaper = {
                        homeScreenViewModel.nextRandomWallpaper()
                    },
                    onDeleteWallpaper = {
                        homeScreenViewModel.deleteWallpaper(wallpaper) {
                            homeScreenViewModel.nextRandomWallpaper()
                        }
                    }
            )
        }

        // Overlay Header on Top
        ImmersiveHeader(
                title = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                            top = LocalBarsSize.current.statusBarHeight + 16.dp,
                            start = 24.dp,
                            end = 24.dp
                    ),
                navController = navController
        )

        // Overlay Bottom Menu on Bottom exactly as originally styled
        ImmersiveBottomMenu(
                hazeState = hazeState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                            start = 8.dp,
                            end = 8.dp,
                            bottom = LocalBarsSize.current.navigationBarHeight)
                    .height(120.dp),
                navController = navController
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ImmersiveWallpaperItem(
        title: String,
        position: Int,
        hazeState: HazeState,
        onClick: () -> Unit,
        onNextWallpaper: () -> Unit,
        onDeleteWallpaper: () -> Unit,
        modifier: Modifier = Modifier,
        wallpaper: Wallpaper?) {

    val currentScale = remember {
        mutableStateOf(ContentScale.Crop)
    }

    val showDeleteDialog = remember { mutableStateOf(false) }

    val homeScreenViewModel: HomeScreenViewModel = viewModel(LocalActivity.current as ComponentActivity)
    val isCountdownPaused = homeScreenViewModel.isCountdownPaused.collectAsState().value

    if (showDeleteDialog.value) {
        SureDialog(
                message = wallpaper?.name ?: wallpaper?.filePath ?: "",
                onSure = {
                    onDeleteWallpaper()
                    showDeleteDialog.value = false
                },
                onDismiss = {
                    showDeleteDialog.value = false
                }
        )
    }

    Box(
            modifier = modifier
                .fillMaxSize()
    ) {
        GlideImage(
                model = wallpaper?.filePath?.toFile(),
                contentDescription = null,
                transition = CrossFade,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                alignment = Alignment.Center,
                failure = placeholder(painter = painterResource(id = R.drawable.no_image_placeholder)),
                contentScale = currentScale.value,
        ) {
            it.addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean): Boolean {
                    return false
                }

                override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean): Boolean {
                    return false
                }
            })
                .disallowHardwareConfig()
                .fitCenter()
        }

        // Dark gradient/overlay for readability of text and icons
        Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
        )
        
        // Valid click region restricted to the middle of the screen
        Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                            top = LocalBarsSize.current.statusBarHeight + 140.dp, // Clears header and top controls
                            bottom = LocalBarsSize.current.navigationBarHeight + 220.dp // Clears bottom menu and title
                    )
                    .combinedClickable(
                            onClick = onClick,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                    )
        )

        if (position == RANDOM_WALLPAPER_POSITION_FS) {
            Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .padding(
                                top = LocalBarsSize.current.statusBarHeight + 80.dp,
                                start = 24.dp,
                                end = 24.dp
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                CircularCountdownProgress()

                Spacer(modifier = Modifier.weight(1f))

                CircularIconButton(
                        onClick = { homeScreenViewModel.toggleCountdownPause() },
                        imageVector = if (isCountdownPaused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause
                )

                CircularIconButton(
                        onClick = { onNextWallpaper() },
                        imageVector = Icons.Rounded.FastForward
                )

                CircularIconButton(
                        onClick = { showDeleteDialog.value = true },
                        imageVector = Icons.Rounded.Delete
                )
            }
        }

        Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    // Horizontal margin and push above bottom menu
                    .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = LocalBarsSize.current.navigationBarHeight + 120.dp
                    )
                    .clip(RoundedCornerShape(24.dp)) // Rounds the background
                    .hazeEffect(
                            state = hazeState,
                            style = HazeDefaults.style(backgroundColor = Color(0x30000000), blurRadius = 20.dp)
                    )
        ) {
            Text(
                    text = title,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 16.dp, end = 24.dp),
                    textAlign = TextAlign.Start,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
            )

            Text(
                    text = buildString {
                        append((wallpaper?.width ?: 0).toString())
                        append("x")
                        append((wallpaper?.height ?: 0).toString())
                    },
                    modifier = Modifier
                        .padding(start = 24.dp, top = 4.dp, bottom = 16.dp, end = 24.dp),
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun ImmersiveHeader(title: String, modifier: Modifier = Modifier, navController: NavController? = null) {
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
                fontSize = 24.sp,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
        )

        IconButton(
                onClick = {
                    autoWallpaperScreenSelection.value = true
                },
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
        ) {
            Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null
            )
        }

        IconButton(
                onClick = {
                    navController?.navigate(Routes.SETTINGS)
                },
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
        ) {
            Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null
            )
        }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun ImmersiveBottomMenu(
        hazeState: HazeState,
        modifier: Modifier = Modifier,
        navController: NavController? = null) {

    val height = 60.dp
    val rowPadding = 16.dp
    val context = LocalContext.current
    val wallhavenSearchParametersDialog = remember { mutableStateOf(false) }

    if (wallhavenSearchParametersDialog.value) {
        WallhavenSearchDialog(
                onDismiss = {
                    wallhavenSearchParametersDialog.value = false
                },
                onSearch = { filter ->
                    navController?.navigate(Routes.WALLHAVEN) {
                        navController.currentBackStackEntry?.savedStateHandle?.set(Routes.WALLHAVEN_ARG, filter)
                    }
                }
        )
    }

    Row(
            modifier = modifier
                .fillMaxHeight()
                .padding(start = rowPadding, end = rowPadding),
            verticalAlignment = Alignment.CenterVertically
    ) {
        ImmersiveBottomMenuItem(
                hazeState = hazeState,
                modifier = Modifier
                    .weight(0.2F)
                    .height(height),
                imageVector = Icons.AutoMirrored.Rounded.Label,
                title = R.string.tags
        ) {
            navController?.navigate(Routes.TAGS)
        }

        ImmersiveBottomMenuItem(
                hazeState = hazeState,
                modifier = Modifier
                    .weight(0.2F)
                    .height(height),
                imageVector = Icons.Rounded.Search,
                title = R.string.wallhaven
        ) {
            wallhavenSearchParametersDialog.value = true
        }

        ImmersiveBottomMenuItem(
                hazeState = hazeState,
                modifier = Modifier
                    .weight(0.2F)
                    .height(height),
                imageVector = Icons.Rounded.MotionPhotosOn,
                title = R.string.live_wallpapers
        ) {
            navController?.navigate(Routes.LIVE_WALLPAPERS)
        }

        Card(
                elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                ),
                colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(0.4f)
                    .height(height)
                    .clip(RoundedCornerShape(32.dp))
                    .hazeEffect(
                            state = hazeState,
                            style = HazeDefaults.style(backgroundColor = Color(0x30000000), blurRadius = 20.dp)
                    )
                    .combinedClickable(
                            onClick = {
                                navController?.navigate(Routes.FOLDERS)
                            },
                            onLongClick = {
                                Toast
                                    .makeText(
                                            context,
                                            context.getString(R.string.folder),
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                            },
                            indication = ripple(bounded = true, radius = 32.dp),
                            interactionSource = remember { MutableInteractionSource() }
                    ),
                shape = RoundedCornerShape(32.dp),
        ) {
            Icon(
                    imageVector = Icons.Rounded.Folder,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(16.dp)
                        .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun ImmersiveBottomMenuItem(
        hazeState: HazeState,
        modifier: Modifier = Modifier,
        @StringRes title: Int = 0,
        imageVector: ImageVector = Icons.Rounded.Circle,
        onClick: () -> Unit = {}) {

    val context = LocalContext.current

    Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
                elevation = CardDefaults.cardElevation(
                        defaultElevation = 0.dp
                ),
                colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent,
                ),
                modifier = modifier
                    .padding(start = 4.dp, end = 4.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(32.dp))
                    .hazeEffect(
                            state = hazeState,
                            style = HazeDefaults.style(backgroundColor = Color(0x66000000), blurRadius = 20.dp)
                    )
                    .combinedClickable(
                            onClick = onClick,
                            onLongClick = {
                                Toast
                                    .makeText(
                                            context,
                                            context.getString(title),
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                            },
                            indication = ripple(bounded = true, radius = 32.dp),
                            interactionSource = remember { MutableInteractionSource() }
                    ),
                shape = RoundedCornerShape(32.dp),
        ) {
            Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                        .padding(14.dp)
            )
        }
    }
}