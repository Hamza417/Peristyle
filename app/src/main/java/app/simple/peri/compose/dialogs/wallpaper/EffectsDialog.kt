package app.simple.peri.compose.dialogs.wallpaper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.simple.peri.R

@Composable
fun EffectsDialog(
        showDialog: Boolean,
        setShowDialog: (Boolean) -> Unit,
        initialBlurValue: Float = 0f,
        initialBrightnessValue: Float = 1f,
        initialContrastValue: Float = 1f,
        onApplyEffects: (Float, Float, Float) -> Unit
) {
    if (showDialog) {
        val blurValue = remember { mutableFloatStateOf(initialBlurValue) }
        val brightnessValue = remember { mutableFloatStateOf(initialBrightnessValue) }
        val contrastValue = remember { mutableFloatStateOf(initialContrastValue) }

        LaunchedEffect(blurValue.floatValue, brightnessValue.floatValue, contrastValue.floatValue) {
            onApplyEffects(blurValue.floatValue, brightnessValue.floatValue, contrastValue.floatValue)
        }

        AlertDialog(
                onDismissRequest = { setShowDialog(false) },
                title = { Text(text = stringResource(id = R.string.edit)) },
                text = {
                    Column {
                        Text(text = stringResource(id = R.string.blur))
                        Slider(
                                value = blurValue.floatValue,
                                onValueChange = { blurValue.floatValue = it },
                                valueRange = 0f..25f
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(id = R.string.brightness))
                        Slider(
                                value = brightnessValue.floatValue,
                                onValueChange = { brightnessValue.floatValue = it },
                                valueRange = -255F..255F
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(id = R.string.contrast))
                        Slider(
                                value = contrastValue.floatValue,
                                onValueChange = { contrastValue.floatValue = it },
                                valueRange = 0F..10F
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { setShowDialog(false) }) {
                        Text(stringResource(id = R.string.close))
                    }
                }
        )
    }
}
