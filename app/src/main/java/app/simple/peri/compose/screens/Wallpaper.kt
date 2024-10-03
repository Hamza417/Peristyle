package app.simple.peri.compose.screens

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import app.simple.peri.R
import app.simple.peri.compose.dialogs.wallpaper.EffectsDialog
import app.simple.peri.compose.nav.Routes
import app.simple.peri.constants.Misc
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.BitmapUtils.applyEffects
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.utils.FileUtils.toUri
import app.simple.peri.viewmodels.TagsViewModel
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.glide.ZoomableGlideImage

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Wallpaper(context: Context,
              navController: NavHostController,
              sharedTransitionScope: SharedTransitionScope,
              animatedContentScope: AnimatedContentScope) {

    val wallpaper = navController.previousBackStackEntry?.savedStateHandle?.get<Wallpaper>(Routes.WALLPAPER_ARG)
    var showDialog by remember { mutableStateOf(false) }
    var drawable by remember { mutableStateOf<Drawable?>(null) }
    var blurValue by remember { mutableFloatStateOf(0f) } // 0F..25F
    var brightnessValue by remember { mutableFloatStateOf(0f) } // -255F..255F
    var contrastValue by remember { mutableFloatStateOf(1f) } // 0F..10F
    var saturationValue by remember { mutableFloatStateOf(1f) } // 0F..2F
    var hueValue by remember { mutableFloatStateOf(0f) } // 0F..360F
    var tags by remember { mutableStateOf(emptyList<String>()) }
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory(wallpaper?.md5 ?: "")
    )
    var displayWidth by remember { mutableIntStateOf(0) }
    var displayHeight by remember { mutableIntStateOf(0) }
    val showEditDialog = remember { mutableStateOf(false) }
    val showDetailsCard = remember { mutableStateOf(true) }

    if (showEditDialog.value) {
        EffectsDialog(
                showDialog = showEditDialog.value,
                setShowDialog = {
                    showEditDialog.value = it
                    showDetailsCard.value = !it
                },
                initialBlurValue = blurValue,
                initialBrightnessValue = brightnessValue,
                initialContrastValue = contrastValue,
                initialSaturationValue = saturationValue,
                initialHueValue = hueValue,
                onApplyEffects = { blur, brightness, contrast, saturation, hue ->
                    blurValue = blur
                    brightnessValue = brightness
                    contrastValue = contrast
                    saturationValue = saturation
                    hueValue = hue
                }
        )
    }

    displayWidth = LocalView.current.width
    displayHeight = LocalView.current.height

    tagsViewModel.getWallpaperTags().observeAsState().value?.let {
        tags = it
    }

    with(sharedTransitionScope) {
        Box(
                modifier = Modifier
                    .fillMaxSize()
                    .sharedElement(
                            sharedTransitionScope.rememberSharedContentState(key = wallpaper?.uri ?: ""),
                            animatedVisibilityScope = animatedContentScope
                    ),
        ) {
            val currentScale = remember {
                mutableStateOf(ContentScale.Crop)
            }

            val hazeState = remember { HazeState() }

            // Initialize the main color matrix
            val colorMatrix = ColorMatrix()

            // Create color matrices for each transformation
            val rotateRedMatrix = ColorMatrix().apply { setToRotateRed(hueValue) }
            val rotateGreenMatrix = ColorMatrix().apply { setToRotateGreen(hueValue) }
            val rotateBlueMatrix = ColorMatrix().apply { setToRotateBlue(hueValue) }
            val saturationMatrix = ColorMatrix().apply { setToSaturation(saturationValue) }
            val contrastMatrix = ColorMatrix(
                    floatArrayOf(
                            contrastValue, 0f, 0f, 0f, brightnessValue,
                            0f, contrastValue, 0f, 0f, brightnessValue,
                            0f, 0f, contrastValue, 0f, brightnessValue,
                            0f, 0f, 0f, 1f, 0f
                    )
            )

            // Manually combine the matrices
            val combinedMatrix = FloatArray(20) // Array to hold the combined matrix
            val tempMatrix = FloatArray(20) // Temporary array for intermediate results

            // Multiply the red and green rotation matrices and store the result in tempMatrix
            multiplyMatrices(rotateRedMatrix.values, rotateGreenMatrix.values, tempMatrix)

            // Multiply the result with the blue rotation matrix and store in combinedMatrix
            multiplyMatrices(tempMatrix, rotateBlueMatrix.values, combinedMatrix)

            // Multiply the result with the saturation matrix and store in tempMatrix
            multiplyMatrices(combinedMatrix, saturationMatrix.values, tempMatrix)

            // Multiply the result with the contrast matrix and store in combinedMatrix
            multiplyMatrices(tempMatrix, contrastMatrix.values, combinedMatrix)

            // Set the combined matrix to the main color matrix
            colorMatrix.set(ColorMatrix(combinedMatrix))

            Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(state = hazeState)
                        .drawWithContent {
                            // call record to capture the content in the graphics layer
                            graphicsLayer.record {
                                // draw the contents of the composable into the graphics layer
                                this@drawWithContent.drawContent()
                            }
                            // draw the graphics layer on the visible canvas
                            drawLayer(graphicsLayer)
                        },
            ) {
                ZoomableGlideImage(
                        model = wallpaper?.uri?.toUri(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(blurValue.dp),
                        alignment = Alignment.Center,
                        contentScale = currentScale.value,
                        colorFilter = ColorMatrixColorFilter(colorMatrix),
                        onClick = {
                            showDetailsCard.value = !showDetailsCard.value
                        },
                        onLongClick = {
                            showEditDialog.value = true
                            showDetailsCard.value = false
                        },
                )
                {
                    it.transition(withCrossFade())
                        .disallowHardwareConfig()
                        .fitCenter()
                }
            }

            if (showDetailsCard.value) {
                Card(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .clip(RoundedCornerShape(32.dp))
                            .hazeChild(
                                    state = hazeState,
                                    style = HazeDefaults.style(backgroundColor = Color(0x50000000), blurRadius = 15.dp)),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(
                                containerColor = Color.Transparent,
                        )
                ) {
                    Text(
                            text = wallpaper?.name ?: "",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 16.dp, start = 16.dp),
                            fontSize = 22.sp
                    )

                    Text(
                            text = buildString {
                                append(wallpaper?.width ?: 0)
                                append(" x ")
                                append(wallpaper?.height ?: 0)
                                append(", ")
                                append(wallpaper?.size?.toSize() ?: "")
                            },
                            fontWeight = FontWeight.Light,
                            color = Color.White,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                            fontSize = 18.sp
                    )

                    if (tags.isNotEmpty()) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Label,
                                    contentDescription = "",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .width(32.dp)
                                        .height(32.dp)
                                        .padding(start = 12.dp, bottom = 16.dp)
                            )
                            Text(
                                    text = tags.joinToString(", "),
                                    fontWeight = FontWeight.Light,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 8.dp, bottom = 16.dp),
                                    fontSize = 18.sp
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row {
                        val showLaunchedEffect = remember { mutableStateOf(false) }

                        if (showLaunchedEffect.value) {
                            LaunchedEffect(showLaunchedEffect) {
                                coroutineScope.launch {
                                    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap().copy(Bitmap.Config.ARGB_8888, true)
                                    bitmap.applyEffects(
                                            blur = blurValue.times(Misc.BLUR_TIMES),
                                            brightness = brightnessValue,
                                            contrast = contrastValue,
                                            saturation = saturationValue,
                                            hue = hueValue
                                    )
                                    drawable = BitmapDrawable(context.resources, bitmap)
                                    showDialog = true
                                    showLaunchedEffect.value = false
                                }
                            }
                        }

                        Button(
                                onClick = {
                                    showEditDialog.value = true
                                    showDetailsCard.value = false
                                },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .wrapContentWidth()
                                    .padding(start = 16.dp, bottom = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Unspecified,
                                )
                        ) {
                            Text(
                                    text = context.getString(R.string.edit),
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(12.dp),
                                    fontWeight = FontWeight.SemiBold
                            )

                            Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp)
                                        .padding(end = 8.dp)
                            )
                        }

                        Button(
                                onClick = {
                                    showLaunchedEffect.value = true
                                },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .weight(0.5F)
                                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                )
                        ) {
                            Text(
                                    text = context.getString(R.string.set_as_wallpaper),
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(12.dp),
                                    fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            if (showDialog) {
                ScreenSelectionDialog(
                        setShowDialog = { showDialog = it },
                        context = context,
                        drawable = drawable
                )
            }
        }
    }
}

@Composable
fun ScreenSelectionDialog(setShowDialog: (Boolean) -> Unit, context: Context, drawable: Drawable?) {
    Dialog(onDismissRequest = { setShowDialog(false) }) {
        Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White
        ) {
            Box(
                    contentAlignment = Alignment.Center
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = stringResource(id = R.string.set_as_wallpaper),
                                style = TextStyle(
                                        fontSize = 24.sp,
                                        fontFamily = FontFamily.Default,
                                        fontWeight = FontWeight.Bold
                                )
                        )
                        Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "",
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(30.dp)
                                    .clickable { setShowDialog(false) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                            onClick = {
                                setWallpaper(context, WallpaperManager.FLAG_LOCK, drawable!!)
                                setShowDialog(false)
                            },
                            colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                    ) {
                        Text(
                                text = context.getString(R.string.lock_screen),
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Button(
                            onClick = {
                                setWallpaper(context, WallpaperManager.FLAG_SYSTEM, drawable!!)
                                setShowDialog(false)
                            },
                            colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                    ) {
                        Text(
                                text = context.getString(R.string.home_screen),
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Button(
                            onClick = {
                                setWallpaper(context, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK, drawable!!)
                                setShowDialog(false)
                            },
                            colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                    ) {
                        Text(
                                text = context.getString(R.string.both),
                                color = Color.Black,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

fun setWallpaper(context: Context, flags: Int, drawable: Drawable) {
    val wallpaperManager = WallpaperManager.getInstance(context)
    wallpaperManager.setWallpaperOffsetSteps(0F, 0F)
    wallpaperManager.setBitmap(drawable.toBitmap(), null, true, flags)
}

/**
 * Multiplies two color matrices and stores the result in the provided result array.
 *
 * @param m1 The first color matrix as a float array.
 * @param m2 The second color matrix as a float array.
 * @param result The array to store the result of the multiplication.
 */
fun multiplyMatrices(m1: FloatArray, m2: FloatArray, result: FloatArray) {
    for (i in 0..19) {
        val row = i / 5
        val col = i % 5
        // Perform the matrix multiplication and store the result
        result[i] = m1[row * 5] * m2[col] +
                m1[row * 5 + 1] * m2[col + 5] +
                m1[row * 5 + 2] * m2[col + 10] +
                m1[row * 5 + 3] * m2[col + 15] +
                m1[row * 5 + 4] * m2[col + 4]
    }
}
