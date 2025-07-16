package app.simple.peri.ui.dialogs.wallhaven

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun SearchDialog(
        onDismiss: () -> Unit,
        onSearch: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Search Wallhaven") },
            text = {
                TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Enter search term") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    onSearch(searchQuery)
                    onDismiss()
                }) {
                    Text("Search")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
    )
}