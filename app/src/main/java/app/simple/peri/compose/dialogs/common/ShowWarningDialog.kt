package app.simple.peri.compose.dialogs.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun ShowWarningDialog(title: String, warning: String, onDismiss: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = title)
            },
            text = {
                Text(text = warning)
            },
            confirmButton = {
                Button(
                        onClick = onDismiss
                ) {
                    Text(text = context.getString(android.R.string.ok))
                }
            }
    )
}
