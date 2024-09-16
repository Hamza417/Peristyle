package app.simple.peri.compose.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SureDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
            onDismissRequest = { /* handle dialog close event */ },
            title = { Text(title) },
            text = { Text(text) },
            confirmButton = {
                Button(onClick = {
                    onConfirm()
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = {
                    onDismiss()
                }) {
                    Text("No")
                }
            }
    )
}
