package app.simple.peri.compose.dialogs.wallpaper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        initialSaturationValue: Float = 1f,
        initialHueRedValue: Float = 0f,
        initialHueGreenValue: Float = 0f,
        initialHueBlueValue: Float = 0f,
        onApplyEffects: (Float, Float, Float, Float, Float, Float, Float) -> Unit
) {
    val space = 16.dp

    if (showDialog) {
        val blurValue = remember { mutableFloatStateOf(initialBlurValue) }
        val brightnessValue = remember { mutableFloatStateOf(initialBrightnessValue) }
        val contrastValue = remember { mutableFloatStateOf(initialContrastValue) }
        val saturationValue = remember { mutableFloatStateOf(initialSaturationValue) }
        val hueRedValue = remember { mutableFloatStateOf(initialHueRedValue) }
        val hueGreenValue = remember { mutableFloatStateOf(initialHueGreenValue) }
        val hueBlueValue = remember { mutableFloatStateOf(initialHueBlueValue) }

        LaunchedEffect(blurValue.floatValue,
                       brightnessValue.floatValue,
                       contrastValue.floatValue,
                       saturationValue.floatValue,
                       hueRedValue.floatValue,
                       hueGreenValue.floatValue,
                       hueBlueValue.floatValue) {
            onApplyEffects(blurValue.floatValue,
                           brightnessValue.floatValue,
                           contrastValue.floatValue,
                           saturationValue.floatValue,
                           hueRedValue.floatValue,
                           hueGreenValue.floatValue,
                           hueBlueValue.floatValue)
        }

        fun resetValues() {
            blurValue.floatValue = 0F
            brightnessValue.floatValue = 1F
            contrastValue.floatValue = 1F
            saturationValue.floatValue = 1F
            hueRedValue.floatValue = 0F
            hueGreenValue.floatValue = 0F
            hueBlueValue.floatValue = 0F
        }

        AlertDialog(
                onDismissRequest = { setShowDialog(false) },
                title = {
                    Text(
                            text = stringResource(id = R.string.edit),
                            color = Color.White
                    )
                },
                text = {
                    Column {
                        Text(
                                text = stringResource(id = R.string.blur),
                                color = Color.White
                        )
                        Slider(
                                value = blurValue.floatValue,
                                onValueChange = { blurValue.floatValue = it },
                                valueRange = 0f..25f,
                                colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.White,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                                text = stringResource(id = R.string.brightness),
                                color = Color.White
                        )
                        Slider(
                                value = brightnessValue.floatValue,
                                onValueChange = { brightnessValue.floatValue = it },
                                valueRange = -255F..255F,
                                colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.White,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                                text = stringResource(id = R.string.contrast),
                                color = Color.White
                        )
                        Slider(
                                value = contrastValue.floatValue,
                                onValueChange = { contrastValue.floatValue = it },
                                valueRange = 0F..10F,
                                colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.White,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                                text = stringResource(id = R.string.saturation),
                                color = Color.White
                        )
                        Slider(
                                value = saturationValue.floatValue,
                                onValueChange = { saturationValue.floatValue = it },
                                valueRange = 0F..2F,
                                colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.White,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                                text = stringResource(id = R.string.hue),
                                color = Color.White
                        )

                        Row {
                            Slider(
                                    value = hueRedValue.floatValue,
                                    onValueChange = { hueRedValue.floatValue = it },
                                    valueRange = 0F..360F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = Color.Red,
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                    value = hueGreenValue.floatValue,
                                    onValueChange = { hueGreenValue.floatValue = it },
                                    valueRange = 0F..360F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = Color.Green,
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                    value = hueBlueValue.floatValue,
                                    onValueChange = { hueBlueValue.floatValue = it },
                                    valueRange = 0F..360F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = Color.Blue,
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { setShowDialog(false) }) {
                        Text(stringResource(id = R.string.close))
                    }
                },
                dismissButton = {
                    Button(onClick = { resetValues() }) {
                        Text(stringResource(id = R.string.reset))
                    }
                },
                containerColor = Color.Black.copy(alpha = 0.25F),
        )
    }
}

@Composable
fun AutoWallpaperEffectsDialog(
        showDialog: Boolean,
        setShowDialog: (Boolean) -> Unit,
        initialBlurValue: Float = 0f,
        initialBrightnessValue: Float = 1f,
        initialContrastValue: Float = 1f,
        initialSaturationValue: Float = 1f,
        initialHueValue: Float = 0f,
        onApplyEffects: (Float, Float, Float, Float, Float) -> Unit
) {
    if (showDialog) {
        val blurValue = remember { mutableFloatStateOf(initialBlurValue) }
        val brightnessValue = remember { mutableFloatStateOf(initialBrightnessValue) }
        val contrastValue = remember { mutableFloatStateOf(initialContrastValue) }
        val saturationValue = remember { mutableFloatStateOf(initialSaturationValue) }
        val hueValue = remember { mutableFloatStateOf(initialHueValue) }

        LaunchedEffect(blurValue.floatValue,
                       brightnessValue.floatValue,
                       contrastValue.floatValue,
                       saturationValue.floatValue,
                       hueValue.floatValue) {
            onApplyEffects(blurValue.floatValue,
                           brightnessValue.floatValue,
                           contrastValue.floatValue,
                           saturationValue.floatValue,
                           hueValue.floatValue)
        }

        fun resetValues() {
            blurValue.floatValue = 0F
            brightnessValue.floatValue = 1F
            contrastValue.floatValue = 1F
            saturationValue.floatValue = 1F
            hueValue.floatValue = 0F
        }

        AlertDialog(
                onDismissRequest = { setShowDialog(false) },
                title = { Text(text = stringResource(id = R.string.apply_effects_summary)) },
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
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(id = R.string.saturation))
                        Slider(
                                value = saturationValue.floatValue,
                                onValueChange = { saturationValue.floatValue = it },
                                valueRange = 0F..2F
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(id = R.string.hue))
                        Slider(
                                value = hueValue.floatValue,
                                onValueChange = { hueValue.floatValue = it },
                                valueRange = 0F..360F
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = { setShowDialog(false) }) {
                        Text(stringResource(id = R.string.close))
                    }
                },
                dismissButton = {
                    Button(onClick = { resetValues() }) {
                        Text(stringResource(id = R.string.reset))
                    }
                },
        )
    }
}
