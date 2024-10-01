package app.simple.peri.compose.screens

import android.app.Activity.RESULT_OK
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import app.simple.peri.R
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.constants.DIALOG_OPTION_FONT_SIZE
import app.simple.peri.compose.constants.DIALOG_TITLE_FONT_SIZE
import app.simple.peri.models.LiveWallpaperInfo
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.viewmodels.LiveWallpapersViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiveWallpapers(navController: NavHostController) {
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }
    val liveWallpapersViewModel: LiveWallpapersViewModel = viewModel()
    val liveWallpapers = remember { mutableStateListOf<LiveWallpaperInfo>() }
    var packageNameToUninstall by remember { mutableStateOf<String?>(null) }

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

    LazyVerticalGrid(
            columns = GridCells.Fixed(MainComposePreferences.getGridSpanCount()),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                    top = topPadding,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = bottomPadding
            )
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            TopHeader(
                    title = stringResource(R.string.live_wallpapers),
                    count = liveWallpapers.size,
                    modifier = Modifier.padding(COMMON_PADDING),
                    navController = navController
            )
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
                        wallpaper = liveWallpaperInfo,
                        onOptionSelected = { option ->
                            when (option) {
                                context.getString(R.string.delete) -> {
                                    packageNameToUninstall = liveWallpaperInfo.resolveInfo.serviceInfo.packageName
                                    val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                                        putExtra(Intent.EXTRA_RETURN_RESULT, true)
                                        data = Uri.parse("package:$packageNameToUninstall")
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
            ) {
                val hazeState = remember { HazeState() }

                Box(modifier = Modifier.padding(0.dp)) {
                    Image(
                            painter = rememberDrawablePainter(drawable = liveWallpaperInfo.icon),
                            contentDescription = liveWallpaperInfo.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .haze(hazeState),
                            contentScale = ContentScale.Crop
                    )
                    Column(
                            modifier = Modifier
                                .wrapContentHeight()
                                .fillMaxWidth()
                                .hazeChild(
                                        state = hazeState,
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
}

@Composable
fun LiveWallpapersMenu(liveWallpaperInfo: LiveWallpaperInfo? = null, onDismiss: () -> Unit, onOptionSelected: (String) -> Unit, wallpaper: LiveWallpaperInfo) {
    val options = listOf(
            stringResource(R.string.delete)
    )

    AlertDialog(
            onDismissRequest = { onDismiss() },
            text = {
                Column {
                    Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp, end = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = liveWallpaperInfo?.name ?: "",
                                style = TextStyle(
                                        fontSize = DIALOG_TITLE_FONT_SIZE,
                                        fontFamily = FontFamily.Default,
                                        fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                        )
                        Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "",
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(30.dp)
                                    .clickable { onDismiss() }
                        )
                    }
                    options.forEach { option ->
                        Button(
                                onClick = {
                                    onOptionSelected(option)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                    text = option,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = DIALOG_OPTION_FONT_SIZE
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            onDismiss()
                        }
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}

@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}
