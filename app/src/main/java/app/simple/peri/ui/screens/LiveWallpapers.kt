package app.simple.peri.ui.screens

import android.app.Activity.RESULT_OK
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import app.simple.peri.R
import app.simple.peri.models.LiveWallpaperInfo
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.ui.commons.BottomHeader
import app.simple.peri.ui.commons.COMMON_PADDING
import app.simple.peri.ui.commons.TopHeader
import app.simple.peri.ui.dialogs.livewallpapers.LiveWallpapersMenu
import app.simple.peri.ui.theme.LocalBarsSize
import app.simple.peri.viewmodels.LiveWallpapersViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiveWallpapers(navController: NavHostController) {
    val liveWallpapersViewModel: LiveWallpapersViewModel = viewModel()
    val liveWallpapers = remember { mutableStateListOf<LiveWallpaperInfo>() }
    var packageNameToUninstall by remember { mutableStateOf<String?>(null) }
    val hazeState = remember { HazeState() }
    val listState = rememberLazyGridState()

    var bottomHeaderHeight by remember { mutableStateOf(0.dp) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val topPadding = 8.dp + LocalBarsSize.current.statusBarHeight
    val bottomPadding = 8.dp + if (MainComposePreferences.getBottomHeader()) {
        bottomHeaderHeight
    } else {
        LocalBarsSize.current.navigationBarHeight
    }

    liveWallpapersViewModel.getLiveWallpapersLiveData().observeAsState().value?.let {
        liveWallpapers.clear()
        liveWallpapers.addAll(it)
    }

    val uninstallLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            RESULT_OK -> {
                // Handle successful uninstallation
                packageNameToUninstall?.let { packageName ->
                    liveWallpapers.removeIf { it.resolveInfo.serviceInfo.packageName == packageName }
                    Log.d("LiveWallpapers", "Uninstalled successfully $packageName")
                }
            }

            else -> {
                // Handle unsuccessful uninstallation
                Log.d("LiveWallpapers", "Uninstallation failed for $packageNameToUninstall")
            }
        }
    }

    Box {
        LazyVerticalGrid(
                state = listState,
                columns = GridCells.Fixed(MainComposePreferences.getGridSpanCount(isLandscape)),
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                contentPadding = PaddingValues(
                        top = topPadding,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = bottomPadding
                )
        ) {
            if (MainComposePreferences.getBottomHeader().not()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    TopHeader(
                            title = stringResource(R.string.live_wallpapers),
                            count = liveWallpapers.size,
                            modifier = Modifier.padding(COMMON_PADDING),
                            navController = navController
                    )
                }
            }
            items(liveWallpapers.size) { index ->
                val liveWallpaperInfo = liveWallpapers[index]
                val context = LocalContext.current
                var showWallpaperMenu by remember { mutableStateOf(false) }
                val aspectRatio = if (isLandscape()) 16F / 9F else 9F / 16F

                if (showWallpaperMenu) {
                    LiveWallpapersMenu(
                            liveWallpaperInfo = liveWallpaperInfo,
                            onDismiss = { showWallpaperMenu = false },
                            onOptionSelected = { option ->
                                when (option) {
                                    context.getString(R.string.delete) -> {
                                        packageNameToUninstall = liveWallpaperInfo.resolveInfo.serviceInfo.packageName
                                        @Suppress("DEPRECATION")
                                        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                                            putExtra(Intent.EXTRA_RETURN_RESULT, true)
                                            data = "package:$packageNameToUninstall".toUri()
                                        }
                                        uninstallLauncher.launch(intent)
                                    }
                                }
                            }
                    )
                }

                ElevatedCard(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                            .aspectRatio(aspectRatio)
                            .combinedClickable(
                                    onClick = {
                                        runCatching {
                                            val componentName = ComponentName(
                                                    liveWallpaperInfo.resolveInfo.serviceInfo.packageName,
                                                    liveWallpaperInfo.resolveInfo.serviceInfo.name
                                            )
                                            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                                                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName)
                                            }
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(intent)
                                        }.onFailure {
                                            it.printStackTrace()
                                            Toast
                                                .makeText(
                                                        context,
                                                        it.message ?: it.localizedMessage ?: it.stackTraceToString(),
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        }
                                    },
                                    onLongClick = {
                                        showWallpaperMenu = true
                                    },
                            ),
                        elevation = CardDefaults.cardElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 16.dp
                        ),
                        shape = RoundedCornerShape(16.dp)
                ) {
                    val localHazeState = remember { HazeState() }

                    Box(modifier = Modifier.padding(0.dp)) {
                        Image(
                                painter = rememberDrawablePainter(drawable = liveWallpaperInfo.icon),
                                contentDescription = liveWallpaperInfo.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .hazeSource(localHazeState),
                                contentScale = ContentScale.Crop
                        )
                        Column(
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .fillMaxWidth()
                                    .hazeEffect(state = localHazeState,
                                                style = HazeDefaults.style(backgroundColor = Color(0x50000000), blurRadius = 25.dp)
                                    )
                                    .align(Alignment.BottomCenter)
                        ) {
                            Text(
                                    text = liveWallpaperInfo.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(COMMON_PADDING)
                            )
                        }
                    }
                }
            }
        }

        if (MainComposePreferences.getBottomHeader()) {
            val density = LocalDensity.current

            BottomHeader(
                    title = stringResource(R.string.live_wallpapers),
                    count = liveWallpapers.size,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .onGloballyPositioned {
                            bottomHeaderHeight = with(density) { it.size.height.toDp() }
                        },
                    navController = navController,
                    hazeState = hazeState,
                    navigationBarHeight = LocalBarsSize.current.navigationBarHeight
            )
        }
    }
}

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}
