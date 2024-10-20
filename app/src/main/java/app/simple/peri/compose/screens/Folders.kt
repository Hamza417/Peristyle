package app.simple.peri.compose.screens

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.navigation.NavController
import app.simple.peri.R
import app.simple.peri.compose.commons.COMMON_PADDING
import app.simple.peri.compose.commons.RequestDirectoryPermission
import app.simple.peri.compose.commons.TopHeader
import app.simple.peri.compose.constants.DIALOG_OPTION_FONT_SIZE
import app.simple.peri.compose.constants.DIALOG_TITLE_FONT_SIZE
import app.simple.peri.compose.dialogs.common.ShowWarningDialog
import app.simple.peri.compose.nav.Routes
import app.simple.peri.models.Folder
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.viewmodels.WallpaperViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@Composable
fun Folders(navController: NavController? = null) {
    val wallpaperViewModel: WallpaperViewModel = viewModel()
    val folders by wallpaperViewModel.getFoldersLiveData().observeAsState(emptyList())
    var requestPermission by remember { mutableStateOf(false) }
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }
    val currentLoadingState = wallpaperViewModel.getLoadingImage().collectAsState().value

    if (currentLoadingState.isBlank()) {
        Log.i("Folders", "Loading image is blank")
    }

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

    if (requestPermission) {
        RequestDirectoryPermission(
                onCancel = { requestPermission = false },
                onStorageGranted = { requestPermission = false }
        )
    }

    LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                    top = topPadding,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = bottomPadding
            )
    ) {
        item(span = StaggeredGridItemSpan.FullLine) {
            TopHeader(
                    title = stringResource(R.string.folder),
                    count = folders.size,
                    modifier = Modifier.padding(COMMON_PADDING),
                    navController = navController
            )
        }
        if (currentLoadingState.isBlank().invert()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                Text(
                        text = currentLoadingState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, bottom = 16.dp, end = 16.dp),
                        textAlign = TextAlign.Start,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                )
            }
        }
        items(folders.size) { index ->
            FolderItem(folder = folders[index],
                       navController = navController,
                       wallpaperViewModel = wallpaperViewModel) {
                wallpaperViewModel.deleteFolder(it)
            }
        }
        item {
            ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .aspectRatio(displayDimension.getAspectRatio()),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(
                            defaultElevation = 16.dp,
                            pressedElevation = 0.dp
                    ),
                    onClick = { requestPermission = true }
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

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun FolderItem(folder: Folder, navController: NavController? = null, wallpaperViewModel: WallpaperViewModel, onDelete: (Folder) -> Unit) {
    val hazeState = remember { HazeState() }
    val context = LocalContext.current
    var showFolderMenu by remember { mutableStateOf(false) }
    var showNomediaSuccess by remember { mutableStateOf(false) }
    var showNomediaRemoveSuccess by remember { mutableStateOf(false) }

    if (showFolderMenu) {
        FolderMenu(
                folder = folder,
                onDismiss = { showFolderMenu = false },
                onOptionSelected = {
                    when (it) {
                        context.getString(R.string.delete) -> {
                            onDelete(folder)
                        }

                        context.getString(R.string.add_nomedia) -> {
                            wallpaperViewModel.addNoMediaFile(folder) {
                                showNomediaSuccess = true
                            }
                        }

                        context.getString(R.string.remove_nomedia) -> {
                            Log.d("FolderItem", "Remove nomedia")
                            wallpaperViewModel.removeNoMediaFile(folder) {
                                Log.d("FolderItem", "Remove nomedia success")
                                showNomediaRemoveSuccess = true
                            }
                        }
                    }
                }
        )
    }

    if (showNomediaSuccess) {
        ShowWarningDialog(
                title = stringResource(R.string.nomedia),
                warning = stringResource(R.string.nomedia_success),
                onDismiss = { showNomediaSuccess = false }
        )
    }

    if (showNomediaRemoveSuccess) {
        ShowWarningDialog(
                title = stringResource(R.string.nomedia),
                warning = stringResource(R.string.nomedia_remove_success),
                onDismiss = { showNomediaRemoveSuccess = false }
        )
    }

    ElevatedCard(
            elevation = CardDefaults.cardElevation(
                    defaultElevation = 16.dp,
                    pressedElevation = 0.dp
            ),
            modifier = Modifier
                .padding(8.dp)
                .combinedClickable(
                    onClick = {
                        navController?.navigate(Routes.WALLPAPERS_LIST) {
                            navController.currentBackStackEntry?.savedStateHandle?.set(Routes.FOLDER_ARG, folder)
                        }
                    },
                    onLongClick = {
                        showFolderMenu = true
                    },
                    indication = ripple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() }
                ),
            shape = RoundedCornerShape(16.dp),
    ) {
        Box(
                modifier = Modifier.wrapContentHeight(),
                contentAlignment = Alignment.Center,
        ) {
            GlideImage(
                    model = app.simple.peri.glide.folders.Folder(folder.hashcode, context = LocalContext.current),
                    contentDescription = null,
                    modifier = Modifier.haze(hazeState),
            ) {
                it
                    .transition(withCrossFade())
                    .disallowHardwareConfig()
            }

            Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .hazeChild(
                            state = hazeState,
                            style = HazeDefaults.style(backgroundColor = Color(0x50000000), blurRadius = 5.dp)
                        )
                        .align(Alignment.BottomCenter)
            ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                            text = folder.name ?: "",
                            modifier = Modifier
                                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                                .weight(1f),
                            textAlign = TextAlign.Start,
                            fontSize = 20.sp, // Set the font size
                            fontWeight = FontWeight.Bold, // Make the text bold
                            color = Color.White, // Set the text color
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                    )
                    Icon(
                            imageVector = if (folder.isNomedia) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "No media",
                            modifier = Modifier
                                .padding(start = 8.dp, top = 16.dp, end = 16.dp)
                                .size(14.dp),
                            tint = Color.White
                    )
                }

                Text(
                        text = stringResource(id = R.string.tag_count, folder.count),
                        textAlign = TextAlign.Start,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                        modifier = Modifier.padding(start = 16.dp, bottom = 16.dp),
                )
            }
        }
    }
}

@Composable
fun FolderMenu(folder: Folder? = null, onDismiss: () -> Unit, onOptionSelected: (String) -> Unit) {
    val options = listOf(
            stringResource(R.string.delete),
            when (folder?.isNomedia?.invert()) {
                true -> stringResource(R.string.add_nomedia)
                else -> stringResource(R.string.remove_nomedia)
            },
    )

    AlertDialog(
            title = {
                Text(
                        text = folder?.name ?: "",
                        fontSize = DIALOG_TITLE_FONT_SIZE,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        style = TextStyle.Default,
                )
            },
            onDismissRequest = { onDismiss() },
            text = {
                Column {
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
                                    fontSize = DIALOG_OPTION_FONT_SIZE,
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
