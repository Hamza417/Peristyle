package app.simple.peri.compose.dialogs.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.simple.peri.R
import app.simple.peri.compose.constants.DIALOG_TITLE_FONT_SIZE
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Tag
import app.simple.peri.models.Wallpaper
import app.simple.peri.viewmodels.TagsViewModel

@Composable
fun AddTagDialog(wallpaper: Wallpaper, onDismiss: () -> Unit) {
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory()
    )
    val tags = remember { mutableListOf<Tag>() }
    val tag = remember { mutableStateOf("") }

    tagsViewModel.getTags().observeAsState().value?.let {
        tags.clear()
        tags.addAll(it)
    }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                        text = stringResource(id = R.string.add_tag),
                        fontSize = DIALOG_TITLE_FONT_SIZE,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif,
                        style = TextStyle.Default,
                )
            },
            text = {
                Column {
                    TextField(
                            value = tag.value,
                            onValueChange = { tag.value = it },
                            label = {
                                Text(text = stringResource(id = R.string.name))
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                            )
                    )

                    LazyRow {
                        items(tags.size) { index ->
                            TagItem(tag = tags[index]) {
                                tag.value = it
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            tagsViewModel.addTag(tag.value, wallpaper) {
                                onDismiss()
                            }
                        })
                {
                    Text(stringResource(id = R.string.add))
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
    )
}

@Composable
fun TagItem(tag: Tag, onClick: (String) -> Unit) {
    Button(
            onClick = {
                onClick(tag.name)
            },
            modifier = androidx.compose.ui.Modifier.padding(start = 2.dp, end = 2.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant)
    ) {
        Text(text = tag.name)
    }
}
