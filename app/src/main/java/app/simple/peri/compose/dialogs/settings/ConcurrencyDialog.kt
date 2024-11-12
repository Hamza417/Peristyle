package app.simple.peri.compose.dialogs.settings

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences

@Composable
fun ConcurrencyDialog(onDismiss: () -> Unit) {
    val list = (1..20).toList()

    AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = {
                Column {
                    Text(text = stringResource(id = R.string.max_process))
                    Text(
                            text = stringResource(id = R.string.max_process_desc),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 16.sp
                    )
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
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                                onClick = {
                                    MainComposePreferences.setSemaphoreCount(list[index])
                                    onDismiss()
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
                                    text = list[index].toString(),
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
