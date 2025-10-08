package app.simple.peri.ui.dialogs.menus

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoveDown
import androidx.compose.material.icons.rounded.PhotoSizeSelectLarge
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import app.simple.peri.R
import app.simple.peri.models.Wallpaper
import app.simple.peri.ui.commons.FolderBrowser
import app.simple.peri.ui.commons.MenuItemWithIcon
import app.simple.peri.ui.dialogs.common.SureDialog
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.viewmodels.ComposeWallpaperViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun WallpaperMenu(
        setShowDialog: (Boolean) -> Unit,
        wallpaper: Wallpaper,
        isAnySelected: Boolean = false,
        onDelete: (Wallpaper) -> Unit,
        onSelect: () -> Unit = {},
        onAddTag: () -> Unit = {},
        onCompress: () -> Unit = {},
        onReduceResolution: () -> Unit = {},
        onSelectToHere: () -> Unit = {},
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var launchDirectoryPicker by remember { mutableStateOf(false) }
    var deleteSure by remember { mutableStateOf(false) }
    val composeWallpaperViewModel: ComposeWallpaperViewModel = viewModel(LocalActivity.current as ComponentActivity)

    if (launchDirectoryPicker) {
        FolderBrowser(
                onCancel = {
                    launchDirectoryPicker = false
                },
                onStorageGranted = {
                    launchDirectoryPicker = false
                    composeWallpaperViewModel.moveWallpapers(listOf(wallpaper), it) {
                        Log.i("WallpaperMenu", "Wallpaper moved: $it")
                        setShowDialog(false)
                    }
                }
        )
    }

    if (deleteSure) {
        SureDialog(
                title = stringResource(R.string.delete),
                message = wallpaper.name ?: "",
                onSure = {
                    coroutineScope.launch(Dispatchers.IO) {
                        if (wallpaper.filePath.toFile().delete()) {
                            withContext(Dispatchers.Main) {
                                composeWallpaperViewModel.removeWallpaper(wallpaper)
                                onDelete(wallpaper)
                                deleteSure = false
                                setShowDialog(false)
                            }
                        }
                    }
                },
                onDismiss = {
                    deleteSure = false
                    setShowDialog(true)
                }
        )
    }

    AlertDialog(
            onDismissRequest = { setShowDialog(false) },
            title = {
                Text(
                        text = wallpaper.name ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                )
            },
            text = {
                LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        // Common Actions Section
                        MenuItemWithIcon(
                                icon = Icons.Rounded.Share,
                                text = context.getString(R.string.send),
                                onClick = {
                                    val uri = FileProvider.getUriForFile(
                                            context,
                                            context.packageName + ".provider",
                                            wallpaper.filePath.toFile()
                                    )

                                    ShareCompat.IntentBuilder(context)
                                        .setType("image/*")
                                        .setChooserTitle("Share Wallpaper")
                                        .setStream(uri)
                                        .startChooser()
                                    setShowDialog(false)
                                }
                        )

                        MenuItemWithIcon(
                                icon = Icons.Rounded.Edit,
                                text = context.getString(R.string.edit),
                                onClick = {
                                    val uri = FileProvider.getUriForFile(
                                            context,
                                            context.packageName + ".provider",
                                            wallpaper.filePath.toFile()
                                    )

                                    val intent = Intent(Intent.ACTION_EDIT)
                                    intent.setDataAndType(uri, "image/*")
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    context.startActivity(
                                            Intent.createChooser(
                                                    intent,
                                                    context.getString(R.string.edit)
                                            )
                                    )
                                    setShowDialog(false)
                                }
                        )

                        MenuItemWithIcon(
                                icon = Icons.AutoMirrored.Rounded.Label,
                                text = context.getString(R.string.add_tag),
                                onClick = {
                                    onAddTag()
                                    setShowDialog(false)
                                }
                        )

                        MenuItemWithIcon(
                                icon = Icons.Rounded.MoveDown,
                                text = context.getString(R.string.move),
                                onClick = {
                                    launchDirectoryPicker = true
                                }
                        )

                        HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Selection Actions Section
                        MenuItemWithIcon(
                                icon = Icons.Rounded.CheckBox,
                                text = context.getString(R.string.select),
                                onClick = {
                                    onSelect()
                                    setShowDialog(false)
                                }
                        )

                        if (isAnySelected) {
                            MenuItemWithIcon(
                                    icon = Icons.Rounded.SelectAll,
                                    text = context.getString(R.string.select_all_from_last),
                                    onClick = {
                                        onSelectToHere()
                                        setShowDialog(false)
                                    }
                            )
                        }

                        HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Image Optimization Section
                        if (wallpaper.isNotCompressible().invert()) {
                            MenuItemWithIcon(
                                    icon = Icons.Rounded.Compress,
                                    text = context.getString(R.string.compress),
                                    onClick = {
                                        onCompress()
                                        setShowDialog(false)
                                    }
                            )
                        }

                        MenuItemWithIcon(
                                icon = Icons.Rounded.PhotoSizeSelectLarge,
                                text = context.getString(R.string.reduce_resolution),
                                onClick = {
                                    onReduceResolution()
                                    setShowDialog(false)
                                }
                        )

                        MenuItemWithIcon(
                                icon = Icons.Rounded.Refresh,
                                text = context.getString(R.string.reload_metadata),
                                onClick = {
                                    composeWallpaperViewModel.reloadMetadata(wallpaper) {
                                        Log.i("WallpaperMenu", "Metadata reloaded: $it")
                                        Log.i("WallpaperMenu", "for wallpaper: $wallpaper")
                                        setShowDialog(false)
                                    }
                                }
                        )

                        HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Destructive Action Section
                        MenuItemWithIcon(
                                icon = Icons.Rounded.Delete,
                                text = context.getString(R.string.delete),
                                onClick = {
                                    deleteSure = true
                                },
                                isDestructive = true
                        )
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
