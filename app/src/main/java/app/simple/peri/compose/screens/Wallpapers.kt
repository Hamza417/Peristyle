package app.simple.peri.compose.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.compose.nav.Routes
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.FileUtils.toUri
import app.simple.peri.viewmodels.WallpaperViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@Composable
fun Wallpapers(navController: NavController? = null) {
    val wallpaperViewModel: WallpaperViewModel = viewModel()
    val wallpapers = wallpaperViewModel.getWallpapersLiveData().observeAsState(initial = emptyList())

    LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(8.dp)
    ) {
        items(wallpapers.value.size) { index ->
            WallpaperItem(wallpapers.value[index], navController)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun WallpaperItem(wallpaper: Wallpaper, navController: NavController? = null) {
    val hazeState = remember { HazeState() }

    ElevatedCard(
            elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
            ),
            modifier = Modifier
                .aspectRatio(9F / 16F)
                .padding(8.dp),
            onClick = {
                navController?.navigate(Routes.WALLPAPER) {
                    navController.currentBackStackEntry?.savedStateHandle?.set(Routes.WALLPAPER_ARG, wallpaper)
                }
            },
            shape = RoundedCornerShape(16.dp)
    ) {
        Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
        ) {
            GlideImage(
                    model = wallpaper.uri.toUri(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(hazeState),
                    alignment = Alignment.Center,
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
                    .transition(withCrossFade())
                    .disallowHardwareConfig()
                    .centerCrop()
            }

            Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                        .hazeChild(
                                state = hazeState,
                                style = HazeDefaults.style(backgroundColor = Color(0x50000000), blurRadius = 5.dp))
                        .align(Alignment.BottomCenter)
            ) {
                Text(
                        text = wallpaper.name ?: "",
                        modifier = Modifier
                            .padding(start = 16.dp, top = 16.dp, end = 16.dp),
                        textAlign = TextAlign.Start,
                        fontSize = 18.sp, // Set the font size
                        fontWeight = FontWeight.Bold, // Make the text bold
                        color = Color.White, // Set the text color
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                )

                Text(
                        text = (wallpaper.width ?: 0).toString() + "x" + (wallpaper.height ?: 0).toString(),
                        modifier = Modifier
                            .padding(start = 16.dp, top = 4.dp, bottom = 16.dp),
                        textAlign = TextAlign.End,
                        fontSize = 14.sp, // Set the font size
                        fontWeight = FontWeight.Light, // Make the text bold
                        color = Color.White, // Set the text color
                )
            }
        }
    }
}
