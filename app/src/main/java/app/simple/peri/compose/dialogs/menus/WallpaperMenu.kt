package app.simple.peri.compose.dialogs.menus

import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import app.simple.peri.R
import app.simple.peri.compose.constants.DIALOG_OPTION_FONT_SIZE
import app.simple.peri.compose.constants.DIALOG_TITLE_FONT_SIZE
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.viewmodels.ComposeWallpaperViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val composeWallpaperViewModel: ComposeWallpaperViewModel = viewModel(LocalContext.current as ComponentActivity)

    AlertDialog(
            onDismissRequest = { setShowDialog(false) },
            title = {
                Text(
                        text = wallpaper.name ?: "",
                )
            },
            text = {
                Box(
                        contentAlignment = Alignment.Center
                ) {
                    Column {
                        Button(
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
                                },
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                ),
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
                                        if (wallpaper.filePath.toFile().delete()) {
                                            withContext(Dispatchers.Main) {
                                                composeWallpaperViewModel.removeWallpaper(wallpaper)
                                                onDelete(wallpaper)
                                                setShowDialog(false)
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                ),
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
                                    onAddTag().also {
                                        setShowDialog(false)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                ),
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
                                },
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                ),
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
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                ),
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
                                    onCompress().also {
                                        setShowDialog(false)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                ),
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
                                    onReduceResolution().also {
                                        setShowDialog(false)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                ),
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

                        Spacer(modifier = Modifier.height(5.dp))

                        Button(
                                onClick = {
                                    composeWallpaperViewModel.reloadMetadata(wallpaper) {
                                        Log.i("WallpaperMenu", "Metadata reloaded: $it")
                                        Log.i("WallpaperMenu", "for wallpaper: $wallpaper")
                                        setShowDialog(false)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                        ) {
                            Text(
                                    text = context.getString(R.string.reload_metadata),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = DIALOG_OPTION_FONT_SIZE,
                                    fontWeight = FontWeight.SemiBold
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