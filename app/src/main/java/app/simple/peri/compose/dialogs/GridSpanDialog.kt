import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R

@Composable
fun NumberSelectionDialog(onDismiss: () -> Unit, onNumberSelected: (Int) -> Unit) {
    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = stringResource(R.string.grid_span_summary)) },
            text = {
                LazyVerticalGrid(
                        columns = GridCells.Fixed(3) // 3 columns
                ) {
                    items(6) { index ->
                        Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                onClick = {
                                    onNumberSelected(index + 1)
                                    onDismiss()
                                }) {
                            Text(text = (index + 1).toString())
                        }
                    }
                }
            },
            confirmButton = {},
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}
