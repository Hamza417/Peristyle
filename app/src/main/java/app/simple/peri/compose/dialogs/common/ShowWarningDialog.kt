package app.simple.peri.compose.dialogs.common

import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ShowWarningDialog(title: String, warning: String, context: Context, onDismiss: () -> Unit) {
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
