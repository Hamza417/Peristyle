package app.simple.peri.compose.commons

import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.constants.DIALOG_OPTION_FONT_SIZE
import app.simple.peri.compose.constants.DIALOG_TITLE_FONT_SIZE
import app.simple.peri.compose.dialogs.common.AddTagDialog
import app.simple.peri.compose.dialogs.settings.SureDialog
import app.simple.peri.compose.nav.Routes
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.utils.FileUtils.toUri
import app.simple.peri.viewmodels.TagsViewModel
import app.simple.peri.viewmodels.WallpaperListViewModel
import app.simple.peri.viewmodels.WallpaperViewModel
import com.bumptech.glide.integration.compose.CrossFade
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
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    Log.i("WallpaperItem", "Aspect ratio: $aspectRatio for ${wallpaper.name}")

    if (showDialog) {
        WallpaperMenu({
                          showDialog = it
                      }, wallpaper,
                      onDelete = onDelete,
                      onSelect = {
                          wallpaper.isSelected = wallpaper.isSelected.not()
                          isSelected = wallpaper.isSelected
                          wallpaperListViewModel.setSelectionMode(list.any { it.isSelected })
                          wallpaperListViewModel.setSelectedWallpapers(list.count { it.isSelected })
                      },
                      onCompress = {
                          onCompress()
                      },
                      onReduceResolution = {
                          onReduceResolution()
                      })
    }

    Box {
        val imageShadow = remember { MainComposePreferences.getShowImageShadow() }

        if (imageShadow) {
            GlideImage(
                    model = wallpaper.uri.toUri(),
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
                            start = 8.dp,
                            bottom = if (imageShadow) 35.dp else 8.dp,
                            end = 8.dp,
                            top = 8.dp
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
                shape = RoundedCornerShape(16.dp),
        ) {
            Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
            ) {
                GlideImage(
                        model = wallpaper.uri.toUri(),
                        contentDescription = null,
                        transition = CrossFade,
                        modifier = Modifier
                            .haze(hazeState),
                        alignment = Alignment.Center,
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
                        .centerCrop()
                        .disallowHardwareConfig()
                }

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
        list: List<Wallpaper>,
        count: Int = list.count { it.isSelected },
        hazeState: HazeState,
        wallpaperListViewModel: WallpaperListViewModel,
        navigationBarHeight: Dp
) {

    val context = LocalContext.current
    val iconSize = 56.dp
    var showDeleteSureDialog by remember { mutableStateOf(false) }

    if (showDeleteSureDialog) {
        SureDialog(title = stringResource(R.string.delete),
                   text = stringResource(R.string.delete_message, list.count { it.isSelected }),
                   onConfirm = {
                       // Delete the selected wallpapers
                       showDeleteSureDialog = false
                       wallpaperListViewModel.deleteSelectedWallpapers(list.toMutableList())
                   },
                   onDismiss = {
                       showDeleteSureDialog = false
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
                        val filesUri = list.filter { it.isSelected }.map { it.uri.toUri() }
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
                        list.forEach {
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

@Composable
fun WallpaperMenu(
        setShowDialog: (Boolean) -> Unit,
        wallpaper: Wallpaper,
        onDelete: (Wallpaper) -> Unit,
        onSelect: () -> Unit = {},
        onAddTag: () -> Unit = {},
        onCompress: () -> Unit = {},
        onReduceResolution: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory()
    )
    val wallpaperViewModel: WallpaperViewModel = viewModel()
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

    AlertDialog(
            onDismissRequest = { setShowDialog(false) },
            title = {
                Text(
                        text = wallpaper.name ?: "",
                        style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = DIALOG_TITLE_FONT_SIZE
                        )
                )
            },
            text = {
                Box(
                        contentAlignment = Alignment.Center
                ) {
                    Column {
                        Button(
                                onClick = {
                                    ShareCompat.IntentBuilder(context)
                                        .setType("image/*")
                                        .setChooserTitle("Share Wallpaper")
                                        .setStream(wallpaper.uri.toUri())
                                        .startChooser()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                        ) {
                            Text(
                                    text = context.getString(R.string.send),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = DIALOG_OPTION_FONT_SIZE,
                                    fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Button(
                                onClick = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        if (DocumentFile.fromSingleUri(context, wallpaper.uri.toUri())
                                                    ?.delete() == true
                                        ) {
                                            withContext(Dispatchers.Main) {
                                                wallpaperViewModel.removeWallpaper(wallpaper)
                                                onDelete(wallpaper)
                                                setShowDialog(false)
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                        ) {
                            Text(
                                    text = context.getString(R.string.delete),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = DIALOG_OPTION_FONT_SIZE,
                                    fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Button(
                                onClick = {
                                    showTagDialog.value = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                        ) {
                            Text(
                                    text = context.getString(R.string.add_tag),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = DIALOG_OPTION_FONT_SIZE,
                                    fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_EDIT)
                                    intent.setDataAndType(wallpaper.uri.toUri(), "image/*")
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    context.startActivity(
                                            Intent.createChooser(
                                                    intent,
                                                    context.getString(R.string.edit)
                                            )
                                    )
                                    setShowDialog(false)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                        ) {
                            Text(
                                    text = context.getString(R.string.edit),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = DIALOG_OPTION_FONT_SIZE,
                                    fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Button(
                                onClick = {
                                    onSelect()
                                    setShowDialog(false)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth(),
                        ) {
                            Text(
                                    text = context.getString(R.string.select),
                                    fontSize = DIALOG_OPTION_FONT_SIZE,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        HorizontalDivider(
                                modifier = Modifier
                                    .padding(top = 5.dp, bottom = 5.dp)
                                    .fillMaxWidth()
                        )

                        Button(
                                onClick = {
                                    onCompress()
                                    setShowDialog(false)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth(),
                        ) {
                            Text(
                                    text = context.getString(R.string.compress),
                                    fontSize = DIALOG_OPTION_FONT_SIZE,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Button(
                                onClick = {
                                    onReduceResolution()
                                    setShowDialog(false)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth(),
                        ) {
                            Text(
                                    text = context.getString(R.string.reduce_resolution),
                                    fontSize = DIALOG_OPTION_FONT_SIZE,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { setShowDialog(false) },
                ) {
                    Text(
                            text = stringResource(R.string.close),
                    )
                }
            },
    )
}
