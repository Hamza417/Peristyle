package app.simple.peri.compose.commons

import android.app.Application
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ShareCompat
import androidx.core.view.WindowInsetsCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.dialogs.AddTagDialog
import app.simple.peri.compose.nav.Routes
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.FileUtils.toUri
import app.simple.peri.viewmodels.TagsViewModel
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun Wallpapers(list: List<Wallpaper>, navController: NavController? = null, loadingState: String = "") {
    var wallpapers by remember { mutableStateOf(emptyList<Wallpaper>()) }
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }

    wallpapers = list
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
            TopHeader(title = "Wallpapers", count = wallpapers.size,
                      modifier = Modifier.padding(COMMON_PADDING))
        }
        items(wallpapers.size) { index ->
            WallpaperItem(wallpapers[index], navController) { deletedWallpaper ->
                wallpapers = wallpapers.filter { it != deletedWallpaper }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun WallpaperItem(wallpaper: Wallpaper, navController: NavController? = null, onDelete: (Wallpaper) -> Unit) {
    val hazeState = remember { HazeState() }
    var showDialog by remember { mutableStateOf(false) }

    var displayWidth by remember { mutableIntStateOf(0) }
    var displayHeight by remember { mutableIntStateOf(0) }

    displayWidth = LocalView.current.width
    displayHeight = LocalView.current.height

    val aspectRatio by remember {
        mutableFloatStateOf(displayWidth.toFloat() / displayHeight.toFloat())
    }

    if (showDialog) {
        WallpaperMenu({
                          showDialog = it
                      }, wallpaper, onDelete)
    }

    Box {
        val imageShadow = remember { MainComposePreferences.getShowImageShadow() }

        if (imageShadow) {
            GlideImage(
                    model = wallpaper.uri.toUri(),
                    contentDescription = null,
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
                    .aspectRatio(aspectRatio)
                    .padding(start = 8.dp,
                             bottom = if (imageShadow) 35.dp else 8.dp,
                             end = 8.dp,
                             top = 8.dp)
                    .shadow(
                            if (imageShadow) 0.dp else 24.dp,
                            shape = RoundedCornerShape(16.dp),
                            clip = false,
                            spotColor = Color(wallpaper.prominentColor),
                            ambientColor = Color(wallpaper.prominentColor))
                    .combinedClickable(
                            onClick = {
                                navController?.navigate(Routes.WALLPAPER) {
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle?.set(Routes.WALLPAPER_ARG, wallpaper)
                                }
                            },
                            onLongClick = {
                                showDialog = true
                            }
                    ),
                shape = RoundedCornerShape(16.dp),
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
                            text = wallpaper.name ?: "",
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

                    WallpaperDimensionsText(wallpaper, displayWidth, displayHeight)
                }
            }
        }
    }
}

@Composable
fun WallpaperMenu(setShowDialog: (Boolean) -> Unit,
                  wallpaper: Wallpaper,
                  onDelete: (Wallpaper) -> Unit,
                  wallpaperViewModel: WallpaperViewModel = viewModel()) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory(context.applicationContext as Application)
    )
    val showTagDialog = remember { mutableStateOf(false) }

    if (showTagDialog.value) {
        AddTagDialog(
                onDismiss = { showTagDialog.value = false },
                onAdd = { tagName ->
                    tagsViewModel.addTag(tagName, wallpaper)
                    showTagDialog.value = false
                }
        )
    }

    Dialog(onDismissRequest = { setShowDialog(false) }) {
        Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White
        ) {
            Box(
                    contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = wallpaper.name ?: "",
                                style = TextStyle(
                                        fontSize = 24.sp,
                                        fontFamily = FontFamily.Default,
                                        fontWeight = FontWeight.Bold
                                )
                        )
                        Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "",
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(30.dp)
                                    .clickable { setShowDialog(false) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                            onClick = {
                                ShareCompat.IntentBuilder(context)
                                    .setType("image/*")
                                    .setChooserTitle("Share Wallpaper")
                                    .setStream(wallpaper.uri.toUri())
                                    .startChooser()
                            },
                            colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                    ) {
                        Text(
                                text = context.getString(R.string.send),
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Button(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    if (DocumentFile.fromSingleUri(context, wallpaper.uri.toUri())?.delete() == true) {
                                        withContext(Dispatchers.Main) {
                                            wallpaperViewModel.removeWallpaper(wallpaper)
                                            onDelete(wallpaper)
                                            setShowDialog(false)
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                    ) {
                        Text(
                                text = context.getString(R.string.delete),
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Button(
                            onClick = {
                                showTagDialog.value = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                    ) {
                        Text(
                                text = context.getString(R.string.add_tag),
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_EDIT)
                                intent.setDataAndType(wallpaper.uri.toUri(), "image/*")
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                context.startActivity(Intent.createChooser(intent, context.getString(R.string.edit)))
                            },
                            colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                    ) {
                        Text(
                                text = context.getString(R.string.edit),
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WallpaperDimensionsText(wallpaper: Wallpaper, displayWidth: Int, displayHeight: Int) {
    val showWarningIndicator = remember { MainComposePreferences.getShowWarningIndicator() }

    Row(
            modifier = Modifier
                .padding(start = 16.dp, top = 4.dp, bottom = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = "${wallpaper.width ?: 0}x${wallpaper.height ?: 0}",
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                modifier = Modifier.weight(1f)
        )

        if (showWarningIndicator) {
            when {
                (wallpaper.width ?: 0) > displayWidth || (wallpaper.height ?: 0) > displayHeight -> {
                    Icon(
                            imageVector = Icons.Rounded.Warning, // Use an appropriate icon
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                    )
                }

                (wallpaper.width ?: 0) < displayWidth || (wallpaper.height ?: 0) < displayHeight -> {
                    Icon(
                            imageVector = Icons.Rounded.Warning, // Use an appropriate icon
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                    )
                }

                else -> {

                }
            }
        }
    }
}
