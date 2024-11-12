package app.simple.peri.compose.dialogs.autowallpaper

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import app.simple.peri.R
import app.simple.peri.models.Folder
import app.simple.peri.viewmodels.ComposeWallpaperViewModel

@Composable
fun FoldersDialog(selected: Int, setShowing: (Boolean) -> Unit, onFolder: (Folder) -> Unit) {
    val composeWallpaperViewModel: ComposeWallpaperViewModel = viewModel(LocalContext.current as ComponentActivity)
    val folders = remember { mutableStateOf(emptyList<Folder>()) }

    composeWallpaperViewModel.getFoldersLiveData().observeAsState().value?.let {
        folders.value = it
    }

    AlertDialog(
            onDismissRequest = { },
            title = { Text(text = stringResource(R.string.folder)) },
            text = {
                Column {
                    folders.value.chunked(1).forEach { rowFolders ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            rowFolders.forEach { folder ->
                                Button(
                                        onClick = { onFolder(folder) },
                                        colors = if (folder.hashcode == selected) {
                                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                        } else {
                                            ButtonDefaults.buttonColors(
                                                    containerColor = Color.Transparent,
                                                    contentColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(4.dp)
                                ) {
                                    Text(
                                            text = folder.name,
                                            fontWeight = FontWeight.Bold,
                                            color = if (folder.hashcode == selected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            }
                                    )
                                    Text(
                                            text = stringResource(id = R.string.tag_count, folder.count),
                                            fontWeight = FontWeight.Light,
                                            color = if (folder.hashcode == selected) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { setShowing(false) },
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}
