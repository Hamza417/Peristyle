package app.simple.peri.compose.screens

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.models.Folder
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
fun Folders(navController: NavController? = null) {
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }
    var count by remember { mutableIntStateOf(0) }
    val wallpaperViewModel: WallpaperViewModel = viewModel()
    var folders by remember { mutableStateOf(emptyList<Folder>()) }

    statusBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.statusBars()).top
    navigationBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    count = LocalContext.current.contentResolver.persistedUriPermissions.size

    wallpaperViewModel.getFoldersLiveData().observeAsState().value?.let {
        folders = it
    }

    val statusBarHeightPx = statusBarHeight
    val statusBarHeightDp = with(LocalDensity.current) { statusBarHeightPx.toDp() }
    val navigationBarHeightPx = navigationBarHeight
    val navigationBarHeightDp = with(LocalDensity.current) { navigationBarHeightPx.toDp() }

    val topPadding = 8.dp + statusBarHeightDp
    val bottomPadding = 8.dp + navigationBarHeightDp

    LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                    top = topPadding,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = bottomPadding)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            TopHeader(title = "Wallpapers", count = count,
                      modifier = Modifier.padding(COMMON_PADDING),
                      navController = navController)
        }
        items(folders.size) { index ->
            FolderItem(folder = folders[index], navController = navController) {

            }
        }
        item {
            ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(COMMON_PADDING)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(
                            defaultElevation = 16.dp,
                    ),
                    onClick = {
                        // TODO add folder
                    }
            ) {
                Box(
                        modifier = Modifier
                            .padding(COMMON_PADDING)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                ) {
                    Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add folder",
                            modifier = Modifier.fillMaxSize(),
                            tint = MaterialTheme.colorScheme.surfaceDim
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun FolderItem(folder: Folder, navController: NavController? = null, onDelete: (Folder) -> Unit) {
    val hazeState = remember { HazeState() }

    var displayWidth by remember { mutableIntStateOf(0) }
    var displayHeight by remember { mutableIntStateOf(0) }

    displayWidth = LocalView.current.width
    displayHeight = LocalView.current.height

    val aspectRatio by remember {
        mutableFloatStateOf(displayWidth.toFloat() / displayHeight.toFloat())
    }

    Box {
        ElevatedCard(
                elevation = CardDefaults.cardElevation(
                        defaultElevation = 16.dp,
                ),
                modifier = Modifier
                    .aspectRatio(aspectRatio)
                    .padding(COMMON_PADDING)
                    .combinedClickable(
                            onClick = {

                            },
                            onLongClick = {

                            }
                    ),
                shape = RoundedCornerShape(16.dp),
        ) {
            Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
            ) {
                GlideImage(
                        model = folder.uri.toUri(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .haze(hazeState),
                        alignment = Alignment.Center,
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
                            text = folder.name ?: "",
                            modifier = Modifier
                                .padding(16.dp),
                            textAlign = TextAlign.Start,
                            fontSize = 18.sp, // Set the font size
                            fontWeight = FontWeight.Bold, // Make the text bold
                            color = Color.White, // Set the text color
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                    )
                }
            }
        }
    }
}
