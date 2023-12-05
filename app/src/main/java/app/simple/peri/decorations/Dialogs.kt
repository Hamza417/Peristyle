package app.simple.peri.decorations

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsDialog(options: List<String>, onOptionSelected: (String) -> Unit) {
    AlertDialog(onDismissRequest = {
        // Dismiss the dialog when the user clicks outside the dialog or on the back
        // button. If you want to disable that functionality, simply use an empty
        // onCloseRequest.
    }) {
        Column {
            options.forEach { option ->
                TextButton(onClick = { onOptionSelected(option) }) {
                    Text(text = option)
                }
            }
        }
    }
}
