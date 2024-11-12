package app.simple.peri.compose.dialogs.tags

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.simple.peri.R
import app.simple.peri.compose.constants.DIALOG_OPTION_FONT_SIZE
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Tag
import app.simple.peri.viewmodels.TagsViewModel

@Composable
fun TagsMenu(setShowDialog: (Boolean) -> Unit, tag: Tag) {
    val context = LocalContext.current
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory()
    )

    AlertDialog(
            onDismissRequest = { setShowDialog(false) },
            title = {
                Text(
                        text = tag.name,
                )
            },
            text = {
                Box(
                        contentAlignment = Alignment.Center
                ) {
                    Column {
                        Button(
                                onClick = {
                                    tagsViewModel.deleteTag(tag)
                                    setShowDialog(false)
                                },
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                        ) {
                            Text(
                                    text = context.getString(R.string.delete),
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
