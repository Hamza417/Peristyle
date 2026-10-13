package app.simple.peri.ui.dialogs.autowallpaper

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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import app.simple.peri.R
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.services.LiveAutoWallpaperService

@Composable
fun TransitionEffectSelectionDialog(onDismiss: () -> Unit) {
    val options = listOf(
            Pair(R.string.crossfade, LiveAutoWallpaperService.TRANSITION_CROSSFADE),
            Pair(R.string.zoom, LiveAutoWallpaperService.TRANSITION_ZOOM),
            Pair(R.string.diagonal_blocks, LiveAutoWallpaperService.TRANSITION_BLOCKS)
    )

    val storedOption = MainComposePreferences.getLiveWallpaperTransitionEffect()
    val selectedOption = remember { mutableStateOf(options.find { it.second == storedOption } ?: options[0]) }

    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Column {
                    Text(text = stringResource(id = R.string.transition_effect))
                    Text(
                            text = stringResource(id = R.string.transition_effect_summary),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                    )
                }
            },
            text = {
                Column {
                    options.forEach { option ->
                        Button(
                                onClick = {
                                    selectedOption.value = option
                                    MainComposePreferences.setLiveWallpaperTransitionEffect(option.second)
                                    onDismiss()
                                },
                                colors = when (selectedOption.value) {
                                    option -> {
                                        ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                    }

                                    else -> {
                                        ButtonDefaults.buttonColors(
                                                containerColor = Color.Transparent,
                                                contentColor = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                    text = stringResource(id = option.first),
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
                        }
                ) {
                    Text(text = stringResource(R.string.close))
                }
            },
            properties = DialogProperties(dismissOnClickOutside = true)
    )
}
