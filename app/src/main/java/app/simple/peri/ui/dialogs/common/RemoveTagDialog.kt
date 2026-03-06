package app.simple.peri.ui.dialogs.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.simple.peri.R
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Wallpaper
import app.simple.peri.ui.constants.DIALOG_TITLE_FONT_SIZE
import app.simple.peri.viewmodels.TagsViewModel

@Composable
fun RemoveTagDialog(wallpaper: Wallpaper, onDismiss: () -> Unit) {
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory(wallpaper.id)
    )

    val wallpaperTags = remember { mutableStateListOf<String>() }

    tagsViewModel.getWallpaperTags().observeAsState().value?.let {
        wallpaperTags.clear()
        wallpaperTags.addAll(it)
    }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                        text = stringResource(id = R.string.remove_tag),
                        fontSize = DIALOG_TITLE_FONT_SIZE,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                )
            },
            text = {
                if (wallpaperTags.isEmpty()) {
                    Text(
                            text = "No tags assigned to this wallpaper",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn {
                        items(wallpaperTags) { tagName ->
                            Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                        text = tagName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                        onClick = {
                                            tagsViewModel.removeTagFromWallpaper(tagName, wallpaper.id) {
                                                // Tag removed successfully
                                            }
                                        }
                                ) {
                                    Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Remove tag",
                                            tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text(stringResource(id = R.string.close))
                }
            }
    )
}


