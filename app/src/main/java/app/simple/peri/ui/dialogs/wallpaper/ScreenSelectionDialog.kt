package app.simple.peri.ui.dialogs.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ScreenLockPortrait
import androidx.compose.material.icons.rounded.Screenshot
import androidx.compose.material.icons.rounded.Splitscreen
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import app.simple.peri.R
import app.simple.peri.models.Wallpaper
import app.simple.peri.ui.commons.MenuItemWithIcon
import app.simple.peri.ui.dialogs.common.ShowWarningDialog
import app.simple.peri.utils.FileUtils.withDelete
import java.io.File

@Composable
fun ScreenSelectionDialog(setShowDialog: (Boolean) -> Unit, context: Context, bitmap: Bitmap, wallpaper: Wallpaper) {
    val shouldExport = remember { mutableStateOf(false) }
    val showDoneDialog = remember { mutableStateOf(false) }

    if (shouldExport.value) {
        ExportWallpaper(context, bitmap, wallpaper) {
            shouldExport.value = false
            showDoneDialog.value = true
        }
    }

    if (showDoneDialog.value) {
        ShowWarningDialog(
                title = context.getString(R.string.exported),
                warning = wallpaper.name!!.substringBeforeLast(".") + "_edited.png",
                onDismiss = {
                    setShowDialog(false)
                    showDoneDialog.value = false
                }
        )
    }

    AlertDialog(
            onDismissRequest = { setShowDialog(false) },
            title = {
                Text(
                        text = stringResource(id = R.string.set_as_wallpaper),
                )
            },
            text = {
                LazyColumn {
                    item {
                        MenuItemWithIcon(
                                icon = Icons.Rounded.ScreenLockPortrait,
                                text = stringResource(R.string.lock_screen),
                                onClick = {
                                    setWallpaper(context, WallpaperManager.FLAG_LOCK, bitmap)
                                    setShowDialog(false)
                                },
                        )

                        MenuItemWithIcon(
                                icon = Icons.Rounded.Screenshot,
                                text = stringResource(R.string.home_screen),
                                onClick = {
                                    setWallpaper(context, WallpaperManager.FLAG_SYSTEM, bitmap)
                                    setShowDialog(false)
                                },
                        )

                        MenuItemWithIcon(
                                icon = Icons.Rounded.Splitscreen,
                                text = stringResource(R.string.both),
                                onClick = {
                                    setWallpaper(context, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK, bitmap)
                                    setShowDialog(false)
                                },
                        )

                        HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        MenuItemWithIcon(
                                icon = Icons.Rounded.Upload,
                                text = stringResource(R.string.export),
                                onClick = {
                                    shouldExport.value = true
                                }
                        )

                        MenuItemWithIcon(
                                icon = Icons.Rounded.Upload,
                                text = stringResource(R.string.set_with),
                                onClick = {
                                    setWallpaperWithDedicatedApp(bitmap, context)
                                    setShowDialog(false)
                                }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            setShowDialog(false)
                        })
                {
                    Text(stringResource(id = R.string.cancel))
                }
            },
    )
}

fun setWallpaper(context: Context, flags: Int, bitmap: Bitmap) {
    val wallpaperManager = WallpaperManager.getInstance(context)
    wallpaperManager.setWallpaperOffsetSteps(0F, 0F)
    wallpaperManager.setBitmap(bitmap, null, true, flags)
}

fun setWallpaperWithDedicatedApp(bitmap: Bitmap, context: Context) {
    File(context.cacheDir, "temp_wallpaper.png").withDelete { file ->
        file.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }

        val uri = FileProvider.getUriForFile(
                context, "${context.packageName}.provider", file)

        val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
            setDataAndType(uri, "image/*")
            putExtra("mimeType", "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        Intent.createChooser(intent, "Set Wallpaper with...").let { chooser ->
            context.startActivity(chooser)
        }
    }
}

@Composable
fun ExportWallpaper(context: Context, bitmap: Bitmap, wallpaper: Wallpaper, onExport: () -> Unit = {}) {
    val wallpaperExportLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("image/x-png")) { uri ->
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }

            onExport()
        }
    }

    LaunchedEffect(Unit) {
        val extension = wallpaper.name?.substringAfterLast(".")
        val fileName = wallpaper.name?.replace(extension!!, "_edited.png") ?: "edited.png"
        wallpaperExportLauncher.launch(fileName)
    }
}
