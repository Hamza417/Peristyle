package app.simple.peri.compose.screens

import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.MotionPhotosOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.commons.InitDisplayDimension
import app.simple.peri.compose.nav.Routes
import app.simple.peri.models.DisplayDimension
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.viewmodels.HomeScreenViewModel
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlin.math.absoluteValue

val displayDimension = DisplayDimension(1080, 1920)

@Composable
fun Home(navController: NavController? = null) {
    InitDisplayDimension()

    val pagerState = rememberPagerState(pageCount = {
        3
    })

    val fling = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(10)
    )

    val homeScreenViewModel: HomeScreenViewModel = viewModel()
    var systemWallpapers = remember { mutableStateListOf<Wallpaper>() }

    homeScreenViewModel.getSystemWallpaper().observe(LocalLifecycleOwner.current) {
        systemWallpapers.clear()
        systemWallpapers = it.toMutableStateList()
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(LocalLifecycleOwner.current) {
        val observer = object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                homeScreenViewModel.stopPostingRandomWallpaper()
            }

            override fun onResume(owner: LifecycleOwner) {
                homeScreenViewModel.startPostingRandomWallpaper()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Header(
            title = stringResource(id = R.string.app_name),
            modifier = Modifier.padding(24.dp),
            navController = navController
        )

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 48.dp),
            flingBehavior = fling,
            modifier = Modifier
                .weight(1f)
        ) { page ->
            val wallpaper = systemWallpapers.getOrNull(page)

            WallpaperItem(
                title = when (page) {
                    0 -> stringResource(id = R.string.home_screen)
                    1 -> stringResource(id = R.string.lock_screen)
                    else -> wallpaper?.name ?: ""
                },
                onClick = {
                    if (wallpaper != null) {
                        navController?.navigate(Routes.WALLPAPER) {
                            navController.currentBackStackEntry?.savedStateHandle?.set(Routes.WALLPAPER_ARG, wallpaper)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        // Calculate the absolute offset for the current page from the
                        // scroll position. We use the absolute value which allows us to mirror
                        // any effects for both directions
                        val pageOffset =
                            ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                        val startScale = 0.95f

                        // We animate the alpha, between 50% and 100%
                        // alpha = lerp(
                        //        start = 0.75f,
                        //        stop = 1f,
                        //        fraction = 1f - pageOffset.coerceIn(0f, 1f),
                        //)

                        scaleX = lerp(
                                start = startScale,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f),
                        )

                        scaleY = lerp(
                                start = startScale,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f),
                        )
                    }
                    .padding(8.dp), // Add padding to create space between the cards
                wallpaper = wallpaper

            )
        }

        BottomMenu(
            modifier = Modifier
                .padding(8.dp)
                .height(120.dp),
            navController = navController
        )
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun WallpaperItem(title: String, onClick: () -> Unit, modifier: Modifier = Modifier, wallpaper: Wallpaper?) {
    val currentScale = remember {
        mutableStateOf(ContentScale.Crop)
    }

    val hazeState = remember { HazeState() }

    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        ),
        modifier = modifier
            .graphicsLayer {
                clip = false
            }
            .shadow(
                    12.dp,
                    shape = RoundedCornerShape(32.dp),
                    clip = false,
                    spotColor = Color(wallpaper?.prominentColor ?: Color.DarkGray.toArgb()),
                    ambientColor = Color(wallpaper?.prominentColor ?: Color.DarkGray.toArgb())
            ),
        onClick = onClick,
        shape = RoundedCornerShape(32.dp),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            GlideImage(
                    model = wallpaper?.filePath?.toFile(),
                contentDescription = null,
                transition = CrossFade,
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
                alignment = Alignment.Center,
                contentScale = currentScale.value,
            ) {
                it.addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable, model: Any, target: Target<Drawable>?, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        return false
                    }
                })
                    .disallowHardwareConfig()
                    .fitCenter()
            }

            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .hazeChild(
                            state = hazeState,
                            style = HazeDefaults.style(backgroundColor = Color(0x50000000), blurRadius = 15.dp)
                    )
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = title,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                    textAlign = TextAlign.Start,
                    fontSize = 24.sp, // Set the font size
                    fontWeight = FontWeight.Bold, // Make the text bold
                    color = Color.White, // Set the text color
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val info = when (title) {
                    stringResource(id = R.string.lock_screen) -> {
                        when {
                            Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU -> {
                                buildString {
                                    append((wallpaper?.width ?: 0).toString())
                                    append("x")
                                    append((wallpaper?.height ?: 0).toString())
                                }
                            }

                            else -> {
                                stringResource(id = R.string.lock_screen_wallpaper_info)
                            }
                        }
                    }

                    else -> {
                        buildString {
                            append((wallpaper?.width ?: 0).toString())
                            append("x")
                            append((wallpaper?.height ?: 0).toString())
                        }
                    }
                }

                Text(
                    text = info,
                    modifier = Modifier
                        .padding(start = 16.dp, top = 4.dp, bottom = 16.dp, end = 16.dp),
                    textAlign = TextAlign.Start,
                    fontSize = 16.sp, // Set the font size
                    fontWeight = FontWeight.Light, // Make the text bold
                    color = Color.White, // Set the text color
                )
            }
        }
    }
}

@Composable
fun Header(title: String, modifier: Modifier = Modifier, navController: NavController? = null) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Start,
            fontSize = 32.sp, // Set the font size
            modifier = Modifier.weight(1f), // Set the weight
            fontWeight = FontWeight.Bold, // Make the text bold
        )

        IconButton(
            onClick = {
                navController?.navigate(Routes.SETTINGS)
            },
        ) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomMenu(modifier: Modifier = Modifier, navController: NavController? = null) {
    val height = 60.dp
    val rowPadding = 16.dp
    val context = LocalContext.current

    Row(
        modifier = modifier
            .fillMaxHeight()
            .padding(start = rowPadding, end = rowPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomMenuItem(
            modifier = Modifier
                .weight(0.2F)
                .height(height),
            imageVector = Icons.AutoMirrored.Rounded.Label,
            title = R.string.tags
        ) {
            navController?.navigate(Routes.TAGS)
        }

        BottomMenuItem(
            modifier = Modifier
                .weight(0.2F)
                .height(height),
            imageVector = Icons.Rounded.Schedule,
            title = R.string.auto_wallpaper
        ) {
            navController?.navigate(Routes.AUTO_WALLPAPER)
        }

        BottomMenuItem(
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
            modifier = Modifier
                .padding(8.dp)
                .weight(0.4f)
                .height(height)
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
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Icon(
                imageVector = Icons.Rounded.Folder,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomMenuItem(modifier: Modifier = Modifier, @StringRes title: Int = 0, imageVector: ImageVector = Icons.Rounded.Circle, onClick: () -> Unit = {}) {
    val context = LocalContext.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            ),
            modifier = modifier
                .padding(start = 4.dp, end = 4.dp)
                .aspectRatio(1f)
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
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(14.dp)
            )
        }
    }
}

@Composable
fun ShowTagDialog(title: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = "This is a placeholder")
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = "OK")
            }
        }
    )
}
