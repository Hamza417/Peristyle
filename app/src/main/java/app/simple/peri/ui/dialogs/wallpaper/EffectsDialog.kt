package app.simple.peri.ui.dialogs.wallpaper

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import app.simple.peri.R
import app.simple.peri.utils.EffectGenerator

val RED = Color(0xFFCB4335.toInt())
val GREEN = Color(0xFF17A589.toInt())
val BLUE = Color(0xFF2E86C1.toInt())

@Composable
fun EffectsDialog(
        setShowDialog: (Boolean) -> Unit,
        initialBlurValue: Float = 0f,
        initialBrightnessValue: Float = 1f,
        initialContrastValue: Float = 1f,
        initialSaturationValue: Float = 1f,
        initialHueRedValue: Float = 0f,
        initialHueGreenValue: Float = 0f,
        initialHueBlueValue: Float = 0f,
        initialScaleRedValue: Float = 1f,
        initialScaleGreenValue: Float = 1f,
        initialScaleBlueValue: Float = 1f,
        onSaveEffects: (Float, Float, Float, Float, Float, Float, Float, Float, Float, Float) -> Unit,
        onApplyEffects: (Float, Float, Float, Float, Float, Float, Float, Float, Float, Float) -> Unit
) {
    val blurValue = remember { mutableFloatStateOf(initialBlurValue) }
    val brightnessValue = remember { mutableFloatStateOf(initialBrightnessValue) }
    val contrastValue = remember { mutableFloatStateOf(initialContrastValue) }
    val saturationValue = remember { mutableFloatStateOf(initialSaturationValue) }
    val hueRedValue = remember { mutableFloatStateOf(initialHueRedValue) }
    val hueGreenValue = remember { mutableFloatStateOf(initialHueGreenValue) }
    val hueBlueValue = remember { mutableFloatStateOf(initialHueBlueValue) }
    val scaleRedValue = remember { mutableFloatStateOf(initialScaleRedValue) }
    val scaleGreenValue = remember { mutableFloatStateOf(initialScaleGreenValue) }
    val scaleBlueValue = remember { mutableFloatStateOf(initialScaleBlueValue) }
    val isHueLocked = remember { mutableStateOf(false) }
    val isScaleLocked = remember { mutableStateOf(false) }

    LaunchedEffect(blurValue.floatValue,
                   brightnessValue.floatValue,
                   contrastValue.floatValue,
                   saturationValue.floatValue,
                   hueRedValue.floatValue,
                   hueGreenValue.floatValue,
                   hueBlueValue.floatValue,
                   scaleRedValue.floatValue,
                   scaleGreenValue.floatValue,
                   scaleBlueValue.floatValue) {
        onApplyEffects(blurValue.floatValue,
                       brightnessValue.floatValue,
                       contrastValue.floatValue,
                       saturationValue.floatValue,
                       hueRedValue.floatValue,
                       hueGreenValue.floatValue,
                       hueBlueValue.floatValue,
                       scaleRedValue.floatValue,
                       scaleGreenValue.floatValue,
                       scaleBlueValue.floatValue)
    }

    fun resetValues() {
        blurValue.floatValue = 0F
        brightnessValue.floatValue = 1F
        contrastValue.floatValue = 1F
        saturationValue.floatValue = 1F
        hueRedValue.floatValue = 0F
        hueGreenValue.floatValue = 0F
        hueBlueValue.floatValue = 0F
        scaleRedValue.floatValue = 1F
        scaleGreenValue.floatValue = 1F
        scaleBlueValue.floatValue = 1F
    }

    AlertDialog(
            onDismissRequest = { setShowDialog(false) },
            title = {
                Row {
                    Text(
                            text = stringResource(id = R.string.edit),
                            color = Color.White
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                            onClick = {
                                val randomEffect = EffectGenerator.generateRandomEffect()
                                // blurValue.floatValue = randomEffect.blurValue
                                brightnessValue.floatValue = randomEffect.brightnessValue
                                contrastValue.floatValue = randomEffect.contrastValue
                                saturationValue.floatValue = randomEffect.saturationValue
                                hueRedValue.floatValue = randomEffect.hueRedValue
                                hueGreenValue.floatValue = randomEffect.hueGreenValue
                                hueBlueValue.floatValue = randomEffect.hueBlueValue
                                scaleRedValue.floatValue = randomEffect.scaleRedValue
                                scaleGreenValue.floatValue = randomEffect.scaleGreenValue
                                scaleBlueValue.floatValue = randomEffect.scaleBlueValue
                            },
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = stringResource(id = R.string.random),
                                tint = Color.White
                        )
                    }
                }
            },
            text = {
                // Adjust the dim amount of the dialog background
                val window = (LocalView.current.parent as? DialogWindowProvider)?.window
                SideEffect {
                    window?.setDimAmount(0.15f)
                }

                LazyColumn {
                    item {
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
                                    onValueChange = {
                                        if (isHueLocked.value) {
                                            hueRedValue.floatValue = it
                                            hueGreenValue.floatValue = it
                                            hueBlueValue.floatValue = it
                                        } else {
                                            hueRedValue.floatValue = it
                                        }
                                    },
                                    valueRange = 0F..360F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = RED,
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                    value = hueGreenValue.floatValue,
                                    onValueChange = {
                                        if (isHueLocked.value) {
                                            hueRedValue.floatValue = it
                                            hueGreenValue.floatValue = it
                                            hueBlueValue.floatValue = it
                                        } else {
                                            hueGreenValue.floatValue = it
                                        }
                                    },
                                    valueRange = 0F..360F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = GREEN,
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                    value = hueBlueValue.floatValue,
                                    onValueChange = {
                                        if (isHueLocked.value) {
                                            hueRedValue.floatValue = it
                                            hueGreenValue.floatValue = it
                                            hueBlueValue.floatValue = it
                                        } else {
                                            hueBlueValue.floatValue = it
                                        }
                                    },
                                    valueRange = 0F..360F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = BLUE,
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            IconButton(
                                    onClick = {
                                        isHueLocked.value = !isHueLocked.value
                                    },
                            ) {
                                Icon(
                                        imageVector = if (isHueLocked.value) {
                                            Icons.Rounded.Lock
                                        } else {
                                            Icons.Rounded.LockOpen
                                        },
                                        contentDescription = stringResource(id = R.string.lock),
                                        tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                                text = stringResource(id = R.string.scale),
                                color = Color.White
                        )

                        Row {
                            Slider(
                                    value = scaleRedValue.floatValue,
                                    onValueChange = {
                                        if (isScaleLocked.value) {
                                            scaleRedValue.floatValue = it
                                            scaleGreenValue.floatValue = it
                                            scaleBlueValue.floatValue = it
                                        } else {
                                            scaleRedValue.floatValue = it
                                        }
                                    },
                                    valueRange = 0F..1F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = RED,
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                    value = scaleGreenValue.floatValue,
                                    onValueChange = {
                                        if (isScaleLocked.value) {
                                            scaleRedValue.floatValue = it
                                            scaleGreenValue.floatValue = it
                                            scaleBlueValue.floatValue = it
                                        } else {
                                            scaleGreenValue.floatValue = it
                                        }
                                    },
                                    valueRange = 0F..1F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = GREEN,
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                    value = scaleBlueValue.floatValue,
                                    onValueChange = {
                                        if (isScaleLocked.value) {
                                            scaleRedValue.floatValue = it
                                            scaleGreenValue.floatValue = it
                                            scaleBlueValue.floatValue = it
                                        } else {
                                            scaleBlueValue.floatValue = it
                                        }
                                    },
                                    valueRange = 0F..1F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = BLUE,
                                            activeTrackColor = Color.White,
                                            inactiveTrackColor = Color.White.copy(alpha = 0.1F),
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            IconButton(
                                    onClick = {
                                        isScaleLocked.value = !isScaleLocked.value
                                    },
                            ) {
                                Icon(
                                        imageVector = if (isScaleLocked.value) {
                                            Icons.Rounded.Lock
                                        } else {
                                            Icons.Rounded.LockOpen
                                        },
                                        contentDescription = stringResource(id = R.string.lock),
                                        tint = Color.White
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSaveEffects(
                            blurValue.floatValue,
                            brightnessValue.floatValue,
                            contrastValue.floatValue,
                            saturationValue.floatValue,
                            hueRedValue.floatValue,
                            hueGreenValue.floatValue,
                            hueBlueValue.floatValue,
                            scaleRedValue.floatValue,
                            scaleGreenValue.floatValue,
                            scaleBlueValue.floatValue
                    )
                    setShowDialog(false)
                }) {
                    Text(stringResource(id = R.string.save))
                }
            },
            dismissButton = {
                Button(onClick = { resetValues() }) {
                    Text(stringResource(id = R.string.reset))
                }
            },
            containerColor = Color.Black.copy(alpha = 0.25F)
    )
}

@Composable
fun AutoWallpaperEffectsDialog(
        setShowDialog: (Boolean) -> Unit,
        initialBlurValue: Float = 0f,
        initialBrightnessValue: Float = 1f,
        initialContrastValue: Float = 1f,
        initialSaturationValue: Float = 1f,
        initialHueRedValue: Float = 0f,
        initialHueGreenValue: Float = 0f,
        initialHueBlueValue: Float = 0f,
        initialScaleRedValue: Float = 1f,
        initialScaleGreenValue: Float = 1f,
        initialScaleBlueValue: Float = 1f,
        onApplyEffects: (Float, Float, Float, Float, Float, Float, Float, Float, Float, Float) -> Unit
) {
    val blurValue = remember { mutableFloatStateOf(initialBlurValue) }
    val brightnessValue = remember { mutableFloatStateOf(initialBrightnessValue) }
    val contrastValue = remember { mutableFloatStateOf(initialContrastValue) }
    val saturationValue = remember { mutableFloatStateOf(initialSaturationValue) }
    val hueRedValue = remember { mutableFloatStateOf(initialHueRedValue) }
    val hueGreenValue = remember { mutableFloatStateOf(initialHueGreenValue) }
    val hueBlueValue = remember { mutableFloatStateOf(initialHueBlueValue) }
    val scaleRedValue = remember { mutableFloatStateOf(initialScaleRedValue) }
    val scaleGreenValue = remember { mutableFloatStateOf(initialScaleGreenValue) }
    val scaleBlueValue = remember { mutableFloatStateOf(initialScaleBlueValue) }
    val isHueLocked = remember { mutableStateOf(false) }
    val isScaleLocked = remember { mutableStateOf(false) }

    LaunchedEffect(blurValue.floatValue,
                   brightnessValue.floatValue,
                   contrastValue.floatValue,
                   saturationValue.floatValue,
                   hueRedValue.floatValue,
                   hueGreenValue.floatValue,
                   hueBlueValue.floatValue,
                   scaleRedValue.floatValue,
                   scaleGreenValue.floatValue,
                   scaleBlueValue.floatValue) {
        onApplyEffects(blurValue.floatValue,
                       brightnessValue.floatValue,
                       contrastValue.floatValue,
                       saturationValue.floatValue,
                       hueRedValue.floatValue,
                       hueGreenValue.floatValue,
                       hueBlueValue.floatValue,
                       scaleRedValue.floatValue,
                       scaleGreenValue.floatValue,
                       scaleBlueValue.floatValue)
    }

    fun resetValues() {
        blurValue.floatValue = 0F
        brightnessValue.floatValue = 1F
        contrastValue.floatValue = 1F
        saturationValue.floatValue = 1F
        hueRedValue.floatValue = 0F
        hueGreenValue.floatValue = 0F
        hueBlueValue.floatValue = 0F
        scaleRedValue.floatValue = 1F
        scaleGreenValue.floatValue = 1F
        scaleBlueValue.floatValue = 1F
    }

    AlertDialog(
            onDismissRequest = { setShowDialog(false) },
            title = {
                Row {
                    Text(text = stringResource(id = R.string.apply_effects_summary))
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                            onClick = {
                                val randomEffect = EffectGenerator.generateRandomEffect()
                                blurValue.floatValue = randomEffect.blurValue
                                brightnessValue.floatValue = randomEffect.brightnessValue
                                contrastValue.floatValue = randomEffect.contrastValue
                                saturationValue.floatValue = randomEffect.saturationValue
                                hueRedValue.floatValue = randomEffect.hueRedValue
                                hueGreenValue.floatValue = randomEffect.hueGreenValue
                                hueBlueValue.floatValue = randomEffect.hueBlueValue
                                scaleRedValue.floatValue = randomEffect.scaleRedValue
                                scaleGreenValue.floatValue = randomEffect.scaleGreenValue
                                scaleBlueValue.floatValue = randomEffect.scaleBlueValue
                            },
                    ) {
                        Icon(
                                imageVector = Icons.Rounded.Shuffle,
                                contentDescription = stringResource(id = R.string.random),
                                tint = Color.White
                        )
                    }
                }
            },
            text = {
                LazyColumn {
                    item {
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
                        Row {
                            Slider(
                                    value = hueRedValue.floatValue,
                                    onValueChange = {
                                        if (isHueLocked.value) {
                                            hueRedValue.floatValue = it
                                            hueGreenValue.floatValue = it
                                            hueBlueValue.floatValue = it
                                        } else {
                                            hueRedValue.floatValue = it
                                        }
                                    },
                                    valueRange = 0F..360F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = RED,
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                    value = hueGreenValue.floatValue,
                                    onValueChange = {
                                        if (isHueLocked.value) {
                                            hueRedValue.floatValue = it
                                            hueGreenValue.floatValue = it
                                            hueBlueValue.floatValue = it
                                        } else {
                                            hueGreenValue.floatValue = it
                                        }
                                    },
                                    valueRange = 0F..360F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = GREEN,
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                    value = hueBlueValue.floatValue,
                                    onValueChange = {
                                        if (isHueLocked.value) {
                                            hueRedValue.floatValue = it
                                            hueGreenValue.floatValue = it
                                            hueBlueValue.floatValue = it
                                        } else {
                                            hueBlueValue.floatValue = it
                                        }
                                    },
                                    valueRange = 0F..360F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = BLUE,
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            IconButton(
                                    onClick = {
                                        isHueLocked.value = !isHueLocked.value
                                    },
                            ) {
                                Icon(
                                        imageVector = if (isHueLocked.value) {
                                            Icons.Rounded.Lock
                                        } else {
                                            Icons.Rounded.LockOpen
                                        },
                                        contentDescription = stringResource(id = R.string.lock),
                                        tint = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(id = R.string.scale))
                        Row {
                            Slider(
                                    value = scaleRedValue.floatValue,
                                    onValueChange = {
                                        if (isScaleLocked.value) {
                                            scaleRedValue.floatValue = it
                                            scaleGreenValue.floatValue = it
                                            scaleBlueValue.floatValue = it
                                        } else {
                                            scaleRedValue.floatValue = it
                                        }
                                    },
                                    valueRange = 0F..1F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = RED,
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                    value = scaleGreenValue.floatValue,
                                    onValueChange = {
                                        if (isScaleLocked.value) {
                                            scaleRedValue.floatValue = it
                                            scaleGreenValue.floatValue = it
                                            scaleBlueValue.floatValue = it
                                        } else {
                                            scaleGreenValue.floatValue = it
                                        }
                                    },
                                    valueRange = 0F..1F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = GREEN,
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Slider(
                                    value = scaleBlueValue.floatValue,
                                    onValueChange = {
                                        if (isScaleLocked.value) {
                                            scaleRedValue.floatValue = it
                                            scaleGreenValue.floatValue = it
                                            scaleBlueValue.floatValue = it
                                        } else {
                                            scaleBlueValue.floatValue = it
                                        }
                                    },
                                    valueRange = 0F..1F,
                                    colors = SliderDefaults.colors(
                                            thumbColor = BLUE,
                                    ),
                                    modifier = Modifier.weight(1F)
                            )
                            IconButton(
                                    onClick = {
                                        isScaleLocked.value = !isScaleLocked.value
                                    },
                            ) {
                                Icon(
                                        imageVector = if (isScaleLocked.value) {
                                            Icons.Rounded.Lock
                                        } else {
                                            Icons.Rounded.LockOpen
                                        },
                                        contentDescription = stringResource(id = R.string.lock),
                                        tint = Color.White
                                )
                            }
                        }
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
    )
}
