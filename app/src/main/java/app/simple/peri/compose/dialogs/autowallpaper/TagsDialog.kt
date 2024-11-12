package app.simple.peri.compose.dialogs.autowallpaper

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import app.simple.peri.R
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Tag
import app.simple.peri.viewmodels.TagsViewModel

@Composable
fun TagsDialog(selected: String, setShowing: (Boolean) -> Unit, onTag: (Tag) -> Unit) {
    val tagsViewModel: TagsViewModel = viewModel(factory = TagsViewModelFactory())
    val tags = remember { mutableStateOf(emptyList<Tag>()) }

    tagsViewModel.getTags().observeAsState().value?.let {
        tags.value = it
    }

    AlertDialog(
            onDismissRequest = { },
            title = { Text(text = stringResource(R.string.tags)) },
            text = {
                if (tags.value.isNotEmpty()) {
                    Column {
                        tags.value.chunked(3).forEach { rowTags ->
                            Row(modifier = Modifier.fillMaxWidth()) {
                                rowTags.forEach { tag ->
                                    Button(
                                            onClick = { onTag(tag) },
                                            colors = if (tag.name == selected) {
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
                                                text = tag.name,
                                                color = if (tag.name == selected) {
                                                    MaterialTheme.colorScheme.onPrimary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text(text = stringResource(R.string.tags_summary))
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
