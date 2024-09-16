package app.simple.peri.compose.commons

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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.window.Dialog
import androidx.core.app.ShareCompat
import androidx.core.view.WindowInsetsCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.dialogs.AddTagDialog
import app.simple.peri.compose.dialogs.SureDialog
import app.simple.peri.compose.nav.Routes
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.FileUtils.toUri
import app.simple.peri.viewmodels.TagsViewModel
import app.simple.peri.viewmodels.WallpaperListViewModel
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
fun WallpapersList(list: List<Wallpaper>, navController: NavController? = null, title: String = "") {
    var wallpapers by remember { mutableStateOf(emptyList<Wallpaper>()) }
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }
    val hazeState = remember { HazeState() }
    val wallpaperListViewModel: WallpaperListViewModel = viewModel() // We should use a dedicated ViewModel for this
    val isSelectionMode by wallpaperListViewModel.isSelectionMode.collectAsState()
    val selectionCount by wallpaperListViewModel.selectedWallpapers.collectAsState()

    wallpapers = list
    wallpapers = wallpaperListViewModel.wallpapers.collectAsState().value.ifEmpty { list }
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

    Box(
            modifier = Modifier.fillMaxSize()
    ) {
        LazyVerticalGrid(
                columns = GridCells.Fixed(MainComposePreferences.getGridSpanCount()),
                modifier = Modifier
                    .fillMaxSize()
                    .haze(state = hazeState),
                contentPadding = PaddingValues(
                        top = topPadding,
                        start = 8.dp,
                        end = 8.dp,
                        bottom = bottomPadding)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                TopHeader(title = title, count = wallpapers.size,
                          modifier = Modifier.padding(COMMON_PADDING),
                          navController = navController)
            }
            items(wallpapers.size) { index ->
                WallpaperItem(wallpaper = wallpapers[index],
                              navController = navController,
                              onDelete = { deletedWallpaper ->
                                  wallpapers = wallpapers.filter {
                                      it != deletedWallpaper
                                  }
                              },
                              isSelectionMode = isSelectionMode,
                              wallpaperListViewModel = wallpaperListViewModel,
                              list = wallpapers)
            }
        }

        if (isSelectionMode) {
            SelectionMenu(list = wallpapers,
                          count = selectionCount,
                          modifier = Modifier.align(Alignment.BottomCenter),
                          hazeState = hazeState,
                          wallpaperListViewModel = wallpaperListViewModel)
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun WallpaperItem(
        wallpaper: Wallpaper,
        navController: NavController? = null,
        onDelete: (Wallpaper) -> Unit,
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
        mutableFloatStateOf(displayWidth.toFloat() / displayHeight.toFloat())
    }

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
                      })
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

                    WallpaperDimensionsText(wallpaper, displayWidth, displayHeight, isSelected, list)
                }
            }
        }
    }
}

@Composable
fun WallpaperDimensionsText(wallpaper: Wallpaper, displayWidth: Int, displayHeight: Int, isSelected: Boolean, list: List<Wallpaper>) {
    list // We need to use the list to update the selection count
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
                            imageVector = Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                    )
                }

                (wallpaper.width ?: 0) < displayWidth || (wallpaper.height ?: 0) < displayHeight -> {
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

@Composable
fun SelectionMenu(modifier: Modifier = Modifier,
                  list: List<Wallpaper>,
                  count: Int = list.count { it.isSelected },
                  hazeState: HazeState,
                  wallpaperListViewModel: WallpaperListViewModel) {

    val context = LocalContext.current
    val iconSize = 72.dp
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
                .padding(32.dp)
                .clip(RoundedCornerShape(32.dp))
                .hazeChild(
                        state = hazeState,
                        style = HazeDefaults.style(backgroundColor = Color(0x65000000), blurRadius = 15.dp)),
            colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent,
            ),
            shape = RoundedCornerShape(48.dp),
    ) {
        Row {
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
                        tint = Color.White
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
            ) {
                Icon(
                        imageVector = Icons.Rounded.Share,
                        contentDescription = null,
                        tint = Color.White
                )
            }
            VerticalDivider(
                    modifier = Modifier
                        .width(1.dp)
                        .height(iconSize),
                    color = Color.White
            )
            Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(start = 24.dp)
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
                        tint = Color.White
                )
            }
        }
    }
}

@Composable
fun WallpaperMenu(setShowDialog: (Boolean) -> Unit,
                  wallpaper: Wallpaper,
                  onDelete: (Wallpaper) -> Unit,
                  onSelect: () -> Unit = {}) {

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
                                color = MaterialTheme.colorScheme.onSurface,
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
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Button(
                            onClick = {
                                showTagDialog.value = true
                                setShowDialog(false)
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
                                color = MaterialTheme.colorScheme.onSurface,
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
                                setShowDialog(false)
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
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Button(
                            onClick = {
                                onSelect()
                                setShowDialog(false)
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
                                text = context.getString(R.string.select),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
