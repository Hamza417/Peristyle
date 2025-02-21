package app.simple.peri.ui.dialogs.folders

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R
import app.simple.peri.models.Folder
import app.simple.peri.ui.constants.DIALOG_OPTION_FONT_SIZE
import app.simple.peri.utils.ConditionUtils.invert

@Composable
fun FolderMenu(folder: Folder? = null, onDismiss: () -> Unit, onOptionSelected: (String) -> Unit) {
    val options = listOf(
            stringResource(R.string.revoke),
            when (folder?.isNomedia?.invert()) {
                true -> stringResource(R.string.add_nomedia)
                else -> stringResource(R.string.remove_nomedia)
            },
    )

    AlertDialog(
            title = {
                Text(
                        text = folder?.name ?: "",
                )
            },
            onDismissRequest = { onDismiss() },
            text = {
                Column {
                    options.forEach { option ->
                        Button(
                                onClick = {
                                    onOptionSelected(option)
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                ),
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                    text = option,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = DIALOG_OPTION_FONT_SIZE,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = {
                            onDismiss()
                        }
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}
