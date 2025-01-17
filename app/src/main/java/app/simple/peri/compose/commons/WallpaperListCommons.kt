package app.simple.peri.compose.commons

import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoveUp
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.dialogs.common.AddTagDialog
import app.simple.peri.compose.dialogs.menus.WallpaperMenu
import app.simple.peri.compose.dialogs.settings.SureDialog
import app.simple.peri.compose.nav.Routes
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.viewmodels.ComposeWallpaperViewModel
import app.simple.peri.viewmodels.WallpaperListViewModel
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
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun WallpaperItem(
        wallpaper: Wallpaper,
        navController: NavController? = null,
        onDelete: (Wallpaper) -> Unit,
        onCompress: () -> Unit,
        onReduceResolution: () -> Unit,
        isSelectionMode: Boolean,
        wallpaperListViewModel: WallpaperListViewModel,
        list: List<Wallpaper>
) {
    val hazeState = remember { HazeState() }
    var showDialog by remember { mutableStateOf(false) }
    var isSelected by remember { mutableStateOf(wallpaper.isSelected) }
    val showTagDialog = remember { mutableStateOf(false) }

    var displayWidth by remember { mutableIntStateOf(0) }
    var displayHeight by remember { mutableIntStateOf(0) }

    displayWidth = LocalView.current.width
    displayHeight = LocalView.current.height

    val aspectRatio by remember {
        mutableFloatStateOf(
                if (MainComposePreferences.isOriginalAspectRatio()) {
                    wallpaper.width?.toFloat()?.div(wallpaper.height!!) ?: (16f / 9f)
                } else {
                    if (!(displayWidth.toFloat() / displayHeight.toFloat()).isNaN()) {
                        displayWidth.toFloat() / displayHeight.toFloat()
                    } else {
                        16f / 9f
                    }
                }
        )
    }

    if (showDialog) {
        WallpaperMenu(
                { showDialog = it },
                wallpaper,
                isAnySelected = list.any { it.isSelected },
                onDelete = onDelete,
                onSelect = {
                    wallpaper.isSelected = wallpaper.isSelected.not()
                    isSelected = wallpaper.isSelected
                    wallpaperListViewModel.setSelectionMode(list.any { it.isSelected })
                    wallpaperListViewModel.setSelectedWallpapers(list.count { it.isSelected })
                },
                onAddTag = {
                    showTagDialog.value = true
                },
                onCompress = {
                    onCompress()
                },
                onReduceResolution = {
                    onReduceResolution()
                },
                onSelectToHere = {
                    wallpaperListViewModel.selectFromLastToCurrent(wallpaper, list)
                }
        )
    }

    if (showTagDialog.value) {
        AddTagDialog(
                wallpapers = listOf(wallpaper),
                onDismiss = { showTagDialog.value = false }
        )
    }

    Box {
        val imageShadow = remember {
            MainComposePreferences.getShowImageShadow()
                    && MainComposePreferences.getMarginBetween()
        }

        Log.d("WallpaperItem", "ImageShadow: $imageShadow")

        if (imageShadow) {
            GlideImage(
                    model = wallpaper.filePath.toFile(),
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
                    .aspectRatio(aspectRatio)
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
                            spotColor = Color(wallpaper.prominentColor),
                            ambientColor = Color(wallpaper.prominentColor)
                    )
                    .combinedClickable(
                            onClick = {
                                if (isSelectionMode) {
                                    wallpaper.isSelected = wallpaper.isSelected.not()
                                    isSelected = wallpaper.isSelected
                                    wallpaperListViewModel.setSelectionMode(list.any { it.isSelected })
                                    wallpaperListViewModel.setSelectedWallpapers(list.count { it.isSelected })
                                } else {
                                    navController?.navigate(Routes.WALLPAPER) {
                                        navController.currentBackStackEntry
                                            ?.savedStateHandle?.set(Routes.WALLPAPER_ARG, wallpaper)
                                    }
                                }
                            },
                            onLongClick = {
                                showDialog = true
                            }
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
                        model = wallpaper.filePath.toFile(),
                        contentDescription = null,
                        transition = CrossFade,
                        modifier = Modifier
                            .haze(hazeState)
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
                                .hazeChild(
                                        state = hazeState,
                                        style = HazeDefaults.style(
                                                backgroundColor = Color(0x50000000),
                                                blurRadius = 5.dp
                                        )
                                )
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

                        WallpaperDimensionsText(wallpaper, displayWidth, displayHeight, isSelected)
                    }
                }
            }
        }
    }
}

@Composable
fun WallpaperDimensionsText(
        wallpaper: Wallpaper,
        displayWidth: Int,
        displayHeight: Int,
        isSelected: Boolean
) {
    val showWarningIndicator = remember { MainComposePreferences.getShowWarningIndicator() }

    Row(
            modifier = Modifier
                .padding(start = 16.dp, top = 4.dp, bottom = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
                text = "${wallpaper.width ?: 0}x${wallpaper.height ?: 0}, ${wallpaper.size.toSize()}",
                textAlign = TextAlign.Start,
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                modifier = Modifier.weight(1f)
        )

        if (showWarningIndicator) {
            when {
                (wallpaper.width ?: 0) > displayWidth || (wallpaper.height
                    ?: 0) > displayHeight -> {
                    Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                    )
                }

                (wallpaper.width ?: 0) < displayWidth || (wallpaper.height
                    ?: 0) < displayHeight -> {
                    Icon(
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                    )
                }

                else -> {

                }
            }
        }

        if (isSelected) {
            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp),
            )
        }
    }
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun SelectionMenu(
        modifier: Modifier = Modifier,
        selectedWallpapers: List<Wallpaper>,
        count: Int = selectedWallpapers.count { it.isSelected },
        hazeState: HazeState,
        wallpaperListViewModel: WallpaperListViewModel,
        navigationBarHeight: Dp
) {

    val context = LocalContext.current
    val iconSize = 56.dp
    var showDeleteSureDialog by remember { mutableStateOf(false) }
    var launchDirectoryPicker by remember { mutableStateOf(false) }
    val showTagDialog = remember { mutableStateOf(false) }

    val composeWallpaperViewModel: ComposeWallpaperViewModel = viewModel(LocalActivity.current as ComponentActivity)

    if (showDeleteSureDialog) {
        SureDialog(title = stringResource(R.string.delete),
                   text = stringResource(R.string.delete_message, selectedWallpapers.count { it.isSelected }),
                   onConfirm = {
                       // Delete the selected wallpapers
                       showDeleteSureDialog = false
                       wallpaperListViewModel.deleteSelectedWallpapers(selectedWallpapers.toMutableList())
                   },
                   onDismiss = {
                       showDeleteSureDialog = false
                   }
        )
    }

    if (launchDirectoryPicker) {
        FolderBrowser(
                onCancel = {
                    launchDirectoryPicker = false
                },
                onStorageGranted = { path ->
                    Log.d("WallpaperMenu", "Path: $path Selected: ${selectedWallpapers.size}")
                    launchDirectoryPicker = false
                    composeWallpaperViewModel.moveWallpapers(selectedWallpapers, path) {
                        Log.i("WallpaperMenu", "Wallpaper moved: $path")
                        wallpaperListViewModel.resetSelectedWallpapersState()
                    }
                }
        )
    }

    if (showTagDialog.value) {
        AddTagDialog(
                wallpapers = selectedWallpapers,
                onDismiss = {
                    showTagDialog.value = false
                    selectedWallpapers.forEach {
                        it.isSelected = false
                    }
                    wallpaperListViewModel.resetSelectedWallpapersState()
                }
        )
    }

    Card(
            elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp,
            ),
            modifier = modifier
                .wrapContentHeight()
                .padding(16.dp)
                .padding(bottom = navigationBarHeight)
                .clip(RoundedCornerShape(16.dp))
                .hazeChild(
                        state = hazeState,
                        style = HazeMaterials.thin()
                ),
            colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(48.dp),
    ) {
        Row(
                horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            IconButton(
                    onClick = {
                        launchDirectoryPicker = true
                    },
                    modifier = Modifier
                        .size(iconSize)
            ) {
                Icon(
                        imageVector = Icons.Rounded.MoveUp,
                        contentDescription = null,
                )
            }
            IconButton(
                    onClick = {
                        showTagDialog.value = true
                    },
                    modifier = Modifier
                        .size(iconSize)
            ) {
                Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Label,
                        contentDescription = null,
                )
            }
            IconButton(
                    onClick = {
                        showDeleteSureDialog = true
                    },
                    modifier = Modifier
                        .size(iconSize)
            ) {
                Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = null,
                )
            }
            IconButton(
                    onClick = {
                        val files = selectedWallpapers.filter { it.isSelected }.map { it.filePath.toFile() }
                        val filesUri = files.map {
                            FileProvider.getUriForFile(
                                    context,
                                    context.packageName + ".provider",
                                    it
                            )
                        }
                        val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
                        intent.type = "image/*"
                        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(filesUri))
                        context.startActivity(Intent.createChooser(intent, "Share Wallpapers"))
                    },
                    modifier = Modifier
                        .size(iconSize)
                        .padding(end = COMMON_PADDING)
            ) {
                Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                )
            }
            VerticalDivider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(iconSize),
            )
            Text(
                    text = count.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 24.dp, end = 8.dp)
                        .align(Alignment.CenterVertically)
            )
            IconButton(
                    onClick = {
                        selectedWallpapers.forEach {
                            it.isSelected = false
                        }
                        wallpaperListViewModel.setSelectionMode(false)
                        wallpaperListViewModel.setSelectedWallpapers(0)
                    },
                    modifier = Modifier
                        .size(iconSize)
            ) {
                Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = null,
                )
            }
        }
    }
}
