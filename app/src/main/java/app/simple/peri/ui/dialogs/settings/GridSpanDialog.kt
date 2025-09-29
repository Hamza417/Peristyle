package app.simple.peri.ui.dialogs.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.utils.ConditionUtils.invert

@Composable
fun NumberSelectionDialog(onDismiss: () -> Unit, onNumberSelected: (Int) -> Unit) {
    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = stringResource(R.string.grid_span)) },
            text = {
                val forLandscape = remember { mutableStateOf(false) }
                val storedNumber = if (forLandscape.value) {
                    MainComposePreferences.getGridSpanCountLandscape()
                } else {
                    MainComposePreferences.getGridSpanCountPortrait()
                }

                val selectedNumber = remember { mutableIntStateOf(storedNumber) }

                Column {
                    Row {
                        Button(
                                onClick = {
                                    forLandscape.value = true
                                    selectedNumber.intValue = MainComposePreferences.getGridSpanCountLandscape()
                                },
                                colors = if (forLandscape.value) {
                                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                } else {
                                    ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .weight(1f)
                        ) {
                            Text(text = stringResource(R.string.landscape),
                                 color = if (forLandscape.value) {
                                     MaterialTheme.colorScheme.onPrimary
                                 } else {
                                     MaterialTheme.colorScheme.onSurface
                                 })
                        }

                        Button(
                                onClick = {
                                    forLandscape.value = false
                                    selectedNumber.intValue = MainComposePreferences.getGridSpanCountPortrait()
                                },
                                colors = if (forLandscape.value.invert()) {
                                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                } else {
                                    ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .weight(1f)
                        ) {
                            Text(text = stringResource(R.string.portrait),
                                 color = if (forLandscape.value.invert()) {
                                     MaterialTheme.colorScheme.onPrimary
                                 } else {
                                     MaterialTheme.colorScheme.onSurface
                                 })
                        }
                    }

                    HorizontalDivider(
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )

                    LazyVerticalGrid(
                            columns = GridCells.Fixed(1) // 3 columns
                    ) {
                        items(6) { index ->
                            val number = index + 1
                            Button(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    onClick = {
                                        selectedNumber.intValue = number
                                        if (forLandscape.value) {
                                            MainComposePreferences.setGridSpanCountLandscape(number)
                                        } else {
                                            MainComposePreferences.setGridSpanCountPortrait(number)
                                        }
                                        onNumberSelected(number)
                                        onDismiss()
                                    },
                                    colors = if (selectedNumber.intValue == number) {
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    } else {
                                        ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                        )
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
