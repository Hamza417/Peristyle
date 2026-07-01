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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import app.simple.peri.R
import app.simple.peri.abstraction.AutoWallpaperUtils
import app.simple.peri.activities.main.LocalDisplaySize
import app.simple.peri.constants.Misc
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.ui.commons.MenuItemWithCheckbox
import app.simple.peri.ui.commons.MenuItemWithIcon
import app.simple.peri.ui.dialogs.common.PleaseWaitDialog
import app.simple.peri.ui.dialogs.common.ShowWarningDialog
import app.simple.peri.utils.BitmapUtils.applyEffects
import app.simple.peri.utils.FileUtils.withDelete
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ScreenSelectionDialog(
        setShowDialog: (Boolean) -> Unit,
        context: Context,
        bitmap: Bitmap,
        wallpaper: Wallpaper,
        blurValue: Float,
        colorMatrix: ColorMatrix) {

    val shouldExport = remember { mutableStateOf(false) }
    val showDoneDialog = remember { mutableStateOf(false) }
    val isCropWallpaper = remember { mutableStateOf(MainComposePreferences.getWallpaperCropMode()) }
    val showPleaseWaitDialog = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val width = LocalDisplaySize.current.width
    val height = LocalDisplaySize.current.height

    if (showPleaseWaitDialog.value) {
        PleaseWaitDialog {
            showPleaseWaitDialog.value = false
        }
    }

    if (shouldExport.value) {
        ExportWallpaper(
                context = context,
                providedBitmap = bitmap,
                wallpaper = wallpaper,
                crop = isCropWallpaper.value,
                blurValue = blurValue,
                colorMatrix = colorMatrix,
                width = width,
                height = height
        ) {
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
                        MenuItemWithCheckbox(
                                isChecked = isCropWallpaper.value,
                                text = stringResource(R.string.crop_wallpaper),
                                summary = stringResource(R.string.crop_wallpaper_summary),
                                onCheckedChange = {
                                    MainComposePreferences.setWallpaperCropMode(it)
                                    isCropWallpaper.value = it
                                }
                        )

                        HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        MenuItemWithIcon(
                                icon = Icons.Rounded.ScreenLockPortrait,
                                text = stringResource(R.string.lock_screen),
                                onClick = {
                                    showPleaseWaitDialog.value = true
                                    scope.launch {
                                        setWallpaper(
                                                context = context,
                                                flags = WallpaperManager.FLAG_LOCK,
                                                providedBitmap = bitmap,
                                                wallpaper = wallpaper,
                                                crop = isCropWallpaper.value,
                                                blurValue = blurValue,
                                                colorMatrix = colorMatrix,
                                                width = width,
                                                height = height
                                        )
                                        setShowDialog(false)
                                    }
                                }
                        )

                        MenuItemWithIcon(
                                icon = Icons.Rounded.Screenshot,
                                text = stringResource(R.string.home_screen),
                                onClick = {
                                    showPleaseWaitDialog.value = true
                                    scope.launch {
                                        setWallpaper(
                                                context = context,
                                                flags = WallpaperManager.FLAG_SYSTEM,
                                                providedBitmap = bitmap,
                                                wallpaper = wallpaper,
                                                crop = isCropWallpaper.value,
                                                blurValue = blurValue,
                                                colorMatrix = colorMatrix,
                                                width = width,
                                                height = height
                                        )
                                        setShowDialog(false)
                                    }
                                }
                        )

                        MenuItemWithIcon(
                                icon = Icons.Rounded.Splitscreen,
                                text = stringResource(R.string.both),
                                onClick = {
                                    scope.launch {
                                        showPleaseWaitDialog.value = true
                                        setWallpaper(
                                                context = context,
                                                flags = WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK,
                                                providedBitmap = bitmap,
                                                wallpaper = wallpaper,
                                                crop = isCropWallpaper.value,
                                                blurValue = blurValue,
                                                colorMatrix = colorMatrix,
                                                width = width,
                                                height = height
                                        )
                                        setShowDialog(false)
                                    }
                                }
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
                                    setWallpaperWithDedicatedApp(
                                            context = context,
                                            flags = WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK,
                                            providedBitmap = bitmap,
                                            wallpaper = wallpaper,
                                            crop = isCropWallpaper.value,
                                            blurValue = blurValue,
                                            colorMatrix = colorMatrix,
                                            width = width,
                                            height = height
                                    )

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

suspend fun setWallpaper(
        context: Context,
        flags: Int,
        providedBitmap: Bitmap,
        wallpaper: Wallpaper,
        crop: Boolean = false,
        blurValue: Float,
        colorMatrix: ColorMatrix,
        width: Int,
        height: Int
) {
    // Shift execution to the background I/O thread
    withContext(Dispatchers.IO) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        wallpaperManager.setWallpaperOffsetSteps(0F, 0F)

        try {
            if (crop) {
                // Use the bitmap passed in the parameters
                wallpaperManager.setBitmap(providedBitmap, null, true, flags)
            } else {
                // Decode the bitmap from the URI without blocking the main thread
                AutoWallpaperUtils.getBitmapFromFile(
                        wallpaper.filePath,
                        width,
                        height,
                        crop = false,
                        recycle = false) {
                    // Apply blur and color adjustments to the bitmap
                    wallpaperManager.setBitmap(
                            /* fullImage = */
                            it.applyEffects(
                                    blur = blurValue.times(Misc.BLUR_TIMES),
                                    colorMatrix = colorMatrix),
                            /* visibleCropHint = */ null,
                            /* allowBackup = */ true,
                            /* which = */ flags)
                }
            }
        } catch (e: Exception) {
            // WallpaperManager can throw exceptions (e.g., if permissions are missing or file is too large)
            e.printStackTrace()
        }
    }
}

fun setWallpaperWithDedicatedApp(
        context: Context,
        flags: Int,
        providedBitmap: Bitmap,
        wallpaper: Wallpaper,
        crop: Boolean = false,
        blurValue: Float,
        colorMatrix: ColorMatrix,
        width: Int,
        height: Int) {
    File(context.cacheDir, "temp_wallpaper.png").withDelete { file ->
        file.outputStream().use { outputStream ->
            if (crop) {
                providedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            } else {
                AutoWallpaperUtils.getBitmapFromFile(
                        wallpaper.filePath,
                        width,
                        height,
                        crop = false,
                        recycle = false) {
                    it.applyEffects(
                            blur = blurValue.times(Misc.BLUR_TIMES),
                            colorMatrix = colorMatrix)
                    it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }
            }
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
fun ExportWallpaper(
        context: Context,
        providedBitmap: Bitmap,
        wallpaper: Wallpaper,
        crop: Boolean = false,
        blurValue: Float,
        colorMatrix: ColorMatrix,
        width: Int,
        height: Int,
        onExport: () -> Unit = {}
) {
    val wallpaperExportLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("image/x-png")) { uri ->
        uri?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                // Apply effects to the bitmap before exporting
                if (crop) {
                    providedBitmap
                        .compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                } else {
                    AutoWallpaperUtils.getBitmapFromFile(
                            wallpaper.filePath,
                            width,
                            height,
                            crop = false,
                            recycle = false) {
                        it.applyEffects(
                                blur = blurValue.times(Misc.BLUR_TIMES),
                                colorMatrix = colorMatrix)

                        it.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                }

                onExport()
            }
        }
    }

    LaunchedEffect(Unit) {
        val extension = wallpaper.name?.substringAfterLast(".")
        val fileName = wallpaper.name?.replace(extension!!, "_edited.png") ?: "edited.png"
        wallpaperExportLauncher.launch(fileName)
    }
}
