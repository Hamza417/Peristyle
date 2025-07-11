package app.simple.peri.ui.dialogs.autowallpaper

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
fun UsageTimesDeleteDialog(onDismiss: () -> Unit) {
    val list = (0..30).toList()

    AlertDialog(
            onDismissRequest = {
                onDismiss()
            },
            title = {
                Column {
                    Text(text = stringResource(id = R.string.delete_after))
                    Text(
                            text = stringResource(id = R.string.delete_summary),
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
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    MainComposePreferences.setMaxSetCount(list[index])
                                    onDismiss()
                                },
                                colors = if (MainComposePreferences.getMaxSetCount() == list[index]) {
                                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                } else {
                                    ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                        ) {
                            val text = if (list[index] == 0) {
                                stringResource(id = R.string.disable)
                            } else {
                                stringResource(id = R.string.times, list[index])
                            }

                            Text(
                                    text = text,
                                    color = if (MainComposePreferences.getMaxSetCount() == list[index]) {
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
