package app.simple.peri.compose.dialogs.menus

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences

@Composable
fun CompressOptions(isCompress: Boolean = true, onDismiss: () -> Unit, onPercentage: (Int) -> Unit) {
    val list = (10..70 step 10).toList()

    AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = {
                if (isCompress) {
                    Text(text = stringResource(id = R.string.compress))
                } else {
                    Text(text = stringResource(id = R.string.reduce_resolution))
                }
            },
            text = {
                LazyVerticalGrid(
                        columns = GridCells.Fixed(1), // 1 column to mimic rows
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.5f),
                ) {
                    items(list.size) { index ->
                        Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    onPercentage(list[index]).also {
                                        onDismiss()
                                    }
                                },
                                colors = if (MainComposePreferences.getSemaphoreCount() == list[index]) {
                                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                } else {
                                    ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                        ) {
                            Text(
                                    text = list[index].toString() + "%",
                                    color = if (MainComposePreferences.getSemaphoreCount() == list[index]) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.fillMaxWidth()
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
                    Text(text = stringResource(id = R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}
