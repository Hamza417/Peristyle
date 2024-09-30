package app.simple.peri.compose.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import app.simple.peri.R
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.models.LiveWallpaperInfo
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.viewmodels.LiveWallpapersViewModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@Composable
fun LiveWallpapers(navController: NavHostController) {
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }
    val liveWallpapersViewModel: LiveWallpapersViewModel = viewModel()
    val liveWallpapers = remember { mutableListOf<LiveWallpaperInfo>() }
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

    displayDimension.width = LocalView.current.width
    displayDimension.height = LocalView.current.height

    liveWallpapersViewModel.getLiveWallpapersLiveData().observeAsState().value?.let {
        liveWallpapers.clear()
        liveWallpapers.addAll(it)
    }

    LazyVerticalGrid(
            columns = GridCells.Fixed(MainComposePreferences.getGridSpanCount()),
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                    top = topPadding,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = bottomPadding)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            TopHeader(title = stringResource(R.string.live_wallpapers), count = liveWallpapers.size,
                      modifier = Modifier.padding(COMMON_PADDING),
                      navController = navController)
        }
        items(liveWallpapers.size) { index ->
            val wallpaper = liveWallpapers[index]
            val context = LocalContext.current

            ElevatedCard(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth()
                        .aspectRatio(displayDimension.getAspectRatio()),
                    elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp,
                            pressedElevation = 16.dp
                    ),
                    onClick = {
                        runCatching {
                            val componentName = ComponentName(
                                    wallpaper.resolveInfo.serviceInfo.packageName,
                                    wallpaper.resolveInfo.serviceInfo.name)
                            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName)
                            }
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        }.onFailure {
                            it.printStackTrace()
                            Toast.makeText(context, it.message ?: it.localizedMessage ?: it.stackTraceToString(),
                                           Toast.LENGTH_SHORT).show()
                        }
                    }
            ) {
                val hazeState = remember { HazeState() }

                Box(
                        modifier = Modifier.padding(0.dp),
                ) {
                    Image(
                            painter = rememberDrawablePainter(drawable = wallpaper.icon),
                            contentDescription = wallpaper.name,
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
                                        style = HazeDefaults.style(backgroundColor = Color(0x50000000), blurRadius = 25.dp))
                                .align(Alignment.BottomCenter)
                    ) {
                        Text(
                                text = wallpaper.name,
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
