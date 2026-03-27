package app.simple.peri.ui.dialogs.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.simple.peri.R

/**
 * Dialog to confirm deletion by typing "confirm"
 *
 * @author Hamza417
 */
@Composable
fun ConfirmDeleteDialog(
        title: String = stringResource(R.string.delete),
        message: String,
        onSure: () -> Unit,
        onDismiss: () -> Unit
) {
    var confirmText by remember { mutableStateOf("") }
    val isConfirmEnabled = confirmText.equals("confirm", ignoreCase = true)

    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(text = title)
            },
            text = {
                Column {
                    Text(
                            text = message,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = Italic
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                            value = confirmText,
                            onValueChange = { confirmText = it },
                            label = { Text(stringResource(R.string.type_confirm_to_delete)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                        onClick = { onSure() },
                        enabled = isConfirmEnabled
                ) {
                    Text(stringResource(id = R.string.yes))
                }
            },
            dismissButton = {
                Button(onClick = { onDismiss() }) {
                    Text(stringResource(id = R.string.no))
                }
            },
    )
}

