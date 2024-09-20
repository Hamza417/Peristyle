import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences

@Composable
fun NumberSelectionDialog(onDismiss: () -> Unit, onNumberSelected: (Int) -> Unit) {
    val storedNumber = MainComposePreferences.getGridSpanCount()
    val selectedNumber = remember { mutableIntStateOf(storedNumber) }

    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = stringResource(R.string.grid_span_summary)) },
            text = {
                LazyVerticalGrid(
                        columns = GridCells.Fixed(3) // 3 columns
                ) {
                    items(6) { index ->
                        val number = index + 1
                        Button(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                onClick = {
                                    selectedNumber.intValue = number
                                    onNumberSelected(number)
                                    onDismiss()
                                },
                                colors = if (selectedNumber.intValue == number) {
                                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                } else {
                                    ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                                }
                        ) {
                            Text(
                                    text = number.toString(),
                                    color = if (selectedNumber.intValue == number) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                        onClick = { onDismiss() },
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}
