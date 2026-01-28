package app.simple.peri.ui.dialogs.folders

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FolderOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.simple.peri.R
import app.simple.peri.models.Folder
import app.simple.peri.ui.commons.MenuItemWithIcon
import app.simple.peri.utils.ConditionUtils.invert
import app.simple.peri.utils.FileUtils.toSize

@Composable
fun FolderMenu(folder: Folder? = null, onDismiss: () -> Unit, onOptionSelected: (String) -> Unit) {

    val revokeText = stringResource(R.string.revoke)
    val addNomediaText = stringResource(R.string.add_nomedia)
    val removeNomediaText = stringResource(R.string.remove_nomedia)
    val deleteText = stringResource(R.string.delete)
    val nomediaText = when (folder?.isNomedia?.invert()) {
        true -> addNomediaText
        else -> removeNomediaText
    }

    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Column {
                    Text(
                            text = folder?.name ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Visible,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee()
                    )

                    Text(
                            text = "${folder?.count ?: 0} ${stringResource(R.string.wallpapers)}, ${folder?.totalSize?.toSize() ?: "0 B"}",
                            textAlign = TextAlign.Start,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .basicMarquee()
                                .padding(top = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Visible
                    )
                }
            },
            text = {
                LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        // Folder Actions Section
                        MenuItemWithIcon(
                                icon = Icons.Rounded.Block,
                                text = revokeText,
                                onClick = {
                                    onOptionSelected(revokeText)
                                    onDismiss()
                                }
                        )

                        MenuItemWithIcon(
                                icon = Icons.Rounded.FolderOff,
                                text = nomediaText,
                                onClick = {
                                    onOptionSelected(nomediaText)
                                    onDismiss()
                                }
                        )

                        HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // Destructive Action Section
                        MenuItemWithIcon(
                                icon = Icons.Rounded.Delete,
                                text = deleteText,
                                onClick = {
                                    onOptionSelected(deleteText)
                                    onDismiss()
                                },
                                isDestructive = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { onDismiss() }
                ) {
                    Text(text = stringResource(R.string.close))
                }
            }
    )
}
