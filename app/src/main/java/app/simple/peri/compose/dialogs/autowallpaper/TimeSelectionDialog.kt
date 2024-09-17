package app.simple.peri.compose.dialogs.autowallpaper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R
import app.simple.peri.preferences.MainPreferences

@Composable
fun TimeSelectionDialog(onDismiss: () -> Unit, onOptionSelected: (Pair<String, Long>) -> Unit) {
    val options = listOf(
            stringResource(R.string.off) to 0L,
            stringResource(R.string.every_minute) to 60_000L,
            stringResource(R.string.every_5_minutes) to 300_000L,
            stringResource(R.string.every_15_minutes) to 900_000L,
            stringResource(R.string.every_30_minutes) to 1_800_000L,
            stringResource(R.string.every_1_hour) to 3_600_000L,
            stringResource(R.string.every_3_hours) to 10_800_000L,
            stringResource(R.string.every_6_hours) to 21_600_000L,
            stringResource(R.string.every_12_hours) to 43_200_000L,
            stringResource(R.string.every_24_hours) to 86_400_000L,
            stringResource(R.string.every_3_days) to 259_200_000L,
            stringResource(R.string.every_7_days) to 604_800_000L
    )

    val storedInterval = MainPreferences.getAutoWallpaperInterval().toLong()
    val selectedOption = remember { mutableStateOf(options.find { it.second == storedInterval }) }

    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(text = stringResource(R.string.auto_wallpaper)) },
            text = {
                Column {
                    options.forEach { option ->
                        Button(
                                onClick = {
                                    selectedOption.value = option
                                    onOptionSelected(option)
                                    onDismiss()
                                },
                                colors = when (selectedOption.value) {
                                    option -> {
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    }

                                    else -> {
                                        ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent,
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                    text = option.first,
                                    color = when (selectedOption.value) {
                                        option -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurface
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
                        },
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}
