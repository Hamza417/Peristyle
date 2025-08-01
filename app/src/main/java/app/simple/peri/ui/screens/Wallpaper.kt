package app.simple.peri.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import app.simple.peri.R
import app.simple.peri.constants.Misc
import app.simple.peri.factories.TagsViewModelFactory
import app.simple.peri.models.Effect
import app.simple.peri.models.WallhavenFilter
import app.simple.peri.models.WallhavenWallpaper
import app.simple.peri.models.Wallpaper
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.services.LiveAutoWallpaperService
import app.simple.peri.ui.commons.FolderBrowser
import app.simple.peri.ui.commons.LaunchEffectActivity
import app.simple.peri.ui.dialogs.common.PleaseWaitDialog
import app.simple.peri.ui.dialogs.wallpaper.EffectsDialog
import app.simple.peri.ui.dialogs.wallpaper.ScreenSelectionDialog
import app.simple.peri.ui.nav.Routes
import app.simple.peri.utils.BitmapUtils.applyEffects
import app.simple.peri.utils.BitmapUtils.multiplyMatrices
import app.simple.peri.utils.ConditionUtils.isNotNull
import app.simple.peri.utils.FileUtils.toFile
import app.simple.peri.utils.FileUtils.toSize
import app.simple.peri.viewmodels.StateViewModel
import app.simple.peri.viewmodels.TagsViewModel
import app.simple.peri.viewmodels.WallhavenViewModel
import app.simple.peri.viewmodels.WallpaperUsageViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.Target
import com.kyant.liquidglass.GlassStyle
import com.kyant.liquidglass.liquidGlass
import com.kyant.liquidglass.liquidGlassProvider
import com.kyant.liquidglass.refraction.InnerRefraction
import com.kyant.liquidglass.refraction.RefractionAmount
import com.kyant.liquidglass.refraction.RefractionHeight
import com.kyant.liquidglass.rememberLiquidGlassProviderState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.saket.telephoto.zoomable.glide.ZoomableGlideImage
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun Wallpaper(navController: NavHostController, associatedWallpaper: Wallpaper? = null) {
    val context = LocalContext.current
    val savedStateHandle = navController.previousBackStackEntry?.savedStateHandle
    val stateViewModel: StateViewModel = viewModel()
    val wallpaperUsageViewModel: WallpaperUsageViewModel = viewModel()
    val wallhavenViewModel: WallhavenViewModel = hiltViewModel()

    val wallpaper: Any? by rememberSaveable(savedStateHandle) {
        if (stateViewModel.getWallpaper() == null) {
            try {
                mutableStateOf(savedStateHandle?.get<Wallpaper>(Routes.WALLPAPER_ARG)
                                   ?: associatedWallpaper)
            } catch (_: ClassCastException) {
                mutableStateOf(savedStateHandle?.get<WallhavenWallpaper>(Routes.WALLPAPER_ARG))
            }
        } else {
            mutableStateOf(stateViewModel.getWallpaper())
        }
    }

    stateViewModel.setWallpaper(wallpaper) // Manually save state?

    var showScreenSelectionDialog by remember { mutableStateOf(false) }
    var showDownloadFolderScreen by remember { mutableStateOf(false) }
    var launchedEffectDownloader by remember { mutableStateOf(false) }
    var path by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var blurValue by remember { stateViewModel::blurValue } // 0F..25F
    var brightnessValue by remember { stateViewModel::brightnessValue } // -255F..255F
    var contrastValue by remember { stateViewModel::contrastValue } // 0F..10F
    var saturationValue by remember { stateViewModel::saturationValue } // 0F..2F
    var hueValueRed by remember { stateViewModel::hueValueRed } // 0F..360F
    var hueValueGreen by remember { stateViewModel::hueValueGreen } // 0F..360F
    var hueValueBlue by remember { stateViewModel::hueValueBlue } // 0F..360F
    var scaleValueRed by remember { stateViewModel::scaleValueRed } // 0F..1F
    var scaleValueGreen by remember { stateViewModel::scaleValueGreen } // 0F..1F
    var scaleValueBlue by remember { stateViewModel::scaleValueBlue } // 0F..1F
    var tags by remember { mutableStateOf(emptyList<String>()) }
    val wallhavenTags = wallhavenViewModel.tags.collectAsState().value
    val setTimes = wallpaperUsageViewModel.dataFlow.collectAsState().value.find {
        it.wallpaperId == if (wallpaper is Wallpaper) {
            (wallpaper as Wallpaper).id
        } else {
            (wallpaper as WallhavenWallpaper).id
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    val tagsViewModel: TagsViewModel = viewModel(
            factory = TagsViewModelFactory(if (wallpaper is Wallpaper) {
                (wallpaper as Wallpaper).id
            } else {
                0
            }, "")
    )
    val showEditDialog = remember { mutableStateOf(false) }
    val showDetailsCard = remember { mutableStateOf(true) }
    val launchEffectActivity = remember { mutableStateOf(false) }
    val providerState = rememberLiquidGlassProviderState(
            backgroundColor = Color.White
    )

    if (wallpaper is WallhavenWallpaper && (wallpaper as WallhavenWallpaper).id.isNotEmpty()) {
        LaunchedEffect((wallpaper as WallhavenWallpaper).id) {
            wallhavenViewModel.fetchWallpaperTags((wallpaper as WallhavenWallpaper).id)
        }
    }

    if (showEditDialog.value) {
        EffectsDialog(
                setShowDialog = {
                    showEditDialog.value = it
                    showDetailsCard.value = !it
                },
                initialBlurValue = blurValue,
                initialBrightnessValue = brightnessValue,
                initialContrastValue = contrastValue,
                initialSaturationValue = saturationValue,
                initialHueRedValue = hueValueRed,
                initialHueGreenValue = hueValueGreen,
                initialHueBlueValue = hueValueBlue,
                initialScaleRedValue = scaleValueRed,
                initialScaleGreenValue = scaleValueGreen,
                initialScaleBlueValue = scaleValueBlue,
                onApplyEffects = { blur, brightness, contrast, saturation, hueRed, hueGreen, hueBlue, scaleRed, scaleGreen, scaleBlue ->
                    blurValue = blur
                    brightnessValue = brightness
                    contrastValue = contrast
                    saturationValue = saturation
                    hueValueRed = hueRed
                    hueValueGreen = hueGreen
                    hueValueBlue = hueBlue
                    scaleValueRed = scaleRed
                    scaleValueGreen = scaleGreen
                    scaleValueBlue = scaleBlue
                },
                onSaveEffects = { blur, brightness, contrast, saturation, hueRed, hueGreen, hueBlue, scaleRed, scaleGreen, scaleBlue ->
                    stateViewModel.saveEffectInDatabase(
                            Effect(blur, brightness, contrast, saturation, hueRed, hueGreen, hueBlue, scaleRed, scaleGreen, scaleBlue)) {
                        Log.i("Wallpaper", "Effect saved")
                    }
                }
        )
    }

    if (showDownloadFolderScreen) {
        FolderBrowser(
                onStorageGranted = { s ->
                    path = s
                    showDownloadFolderScreen = false
                    launchedEffectDownloader = true

                },
                onCancel = {
                    showDownloadFolderScreen = false
                }
        )
    }

    if (launchedEffectDownloader) {
        val url = (wallpaper as WallhavenWallpaper).path
        val fileName = "/wallhaven_${(wallpaper as WallhavenWallpaper).id}.jpg"

        PleaseWaitDialog(stateText = stringResource(R.string.downloading)) {
            /* no-op */
        }

        LaunchedEffect(url) {
            getCachedOrDownloadWallpaper(context, url, path + fileName)
            launchedEffectDownloader = false
        }
    }

    if (launchEffectActivity.value) {
        when {
            wallpaper.isNotNull() && wallpaper is Wallpaper -> {
                LaunchEffectActivity(
                        wallpaper = wallpaper as Wallpaper,
                        onEffect = { effect ->
                            blurValue = effect.blurValue
                            brightnessValue = effect.brightnessValue
                            contrastValue = effect.contrastValue
                            saturationValue = effect.saturationValue
                            hueValueRed = effect.hueRedValue
                            hueValueGreen = effect.hueGreenValue
                            hueValueBlue = effect.hueBlueValue
                            scaleValueRed = effect.scaleRedValue
                            scaleValueGreen = effect.scaleGreenValue
                            scaleValueBlue = effect.scaleBlueValue
                            Log.i("Wallpaper", "Effect launched: $effect")
                            launchEffectActivity.value = false
                        },
                        onCanceled = {
                            launchEffectActivity.value = false
                        })
            }
            wallpaper.isNotNull() && wallpaper is WallhavenWallpaper -> {
                val url = (wallpaper as WallhavenWallpaper).originalUrl
                val fileName = "/thumb_wallhaven_${(wallpaper as WallhavenWallpaper).id}.jpg"
                var loadedWallpaper by remember { mutableStateOf<Wallpaper?>(null) }

                LaunchedEffect(url) {
                    val file = getCachedOrDownloadWallpaper(context, url, context.cacheDir.absolutePath + fileName)
                    if (file != null) {
                        loadedWallpaper = Wallpaper.createFromFile(file, context)
                    }
                }

                if (loadedWallpaper == null && !launchedEffectDownloader) {
                    PleaseWaitDialog(stateText = stringResource(R.string.downloading)) {
                        /* no-op */
                    }
                }

                if (loadedWallpaper != null) {
                    LaunchEffectActivity(
                            wallpaper = loadedWallpaper!!,
                            onEffect = { effect ->
                                blurValue = effect.blurValue
                                brightnessValue = effect.brightnessValue
                                contrastValue = effect.contrastValue
                                saturationValue = effect.saturationValue
                                hueValueRed = effect.hueRedValue
                                hueValueGreen = effect.hueGreenValue
                                hueValueBlue = effect.hueBlueValue
                                scaleValueRed = effect.scaleRedValue
                                scaleValueGreen = effect.scaleGreenValue
                                scaleValueBlue = effect.scaleBlueValue
                                Log.i("Wallpaper", "Effect launched: $effect")
                                launchEffectActivity.value = false
                            },
                            onCanceled = {
                                launchEffectActivity.value = false
                            }
                    )
                }
            }
        }
    }

    tagsViewModel.getWallpaperTags().observeAsState().value?.let {
        tags = it
    }

    Box(
            modifier = Modifier.fillMaxSize(),
    ) {
        val currentScale = remember {
            mutableStateOf(ContentScale.Crop)
        }

        // Initialize the main color matrix
        val colorMatrix = ColorMatrix()

        // Create color matrices for each transformation
        val rotateRedMatrix = ColorMatrix().apply { setToRotateRed(hueValueRed) }
        val rotateGreenMatrix = ColorMatrix().apply { setToRotateGreen(hueValueGreen) }
        val rotateBlueMatrix = ColorMatrix().apply { setToRotateBlue(hueValueBlue) }
        val saturationMatrix = ColorMatrix().apply { setToSaturation(saturationValue) }
        val scaleMatrix = ColorMatrix().apply { setToScale(scaleValueRed, scaleValueGreen, scaleValueBlue, 1F) }
        val scale = contrastValue
        val translate = (-0.5f * scale + 0.5f + brightnessValue / 255f) * 255f
        val contrastMatrix = ColorMatrix(
                floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                )
        )

        val combinedMatrix = FloatArray(20) // Array to hold the combined matrix
        val tempMatrix = FloatArray(20) // Temporary array for intermediate results

        // Multiply the red and green rotation matrices and store the result in tempMatrix
        multiplyMatrices(rotateRedMatrix.values, rotateGreenMatrix.values, tempMatrix)

        // Multiply the result with the blue rotation matrix and store in combinedMatrix
        multiplyMatrices(tempMatrix, rotateBlueMatrix.values, combinedMatrix)

        // Multiply the result with the saturation matrix and store in tempMatrix
        multiplyMatrices(combinedMatrix, saturationMatrix.values, tempMatrix)

        // Multiply the result with the scale matrix and store in combinedMatrix
        multiplyMatrices(tempMatrix, scaleMatrix.values, combinedMatrix)

        // Multiply the result with the contrast matrix and store in tempMatrix
        multiplyMatrices(combinedMatrix, contrastMatrix.values, tempMatrix)

        // Set the combined matrix to the main color matrix
        colorMatrix.set(ColorMatrix(tempMatrix))

        Box(
                modifier = Modifier
                    .fillMaxSize()
                    .liquidGlassProvider(providerState)
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
            when (wallpaper) {
                is Wallpaper -> {
                    ZoomableGlideImage(
                            model = (wallpaper as Wallpaper).filePath.toFile(),
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
                is WallhavenWallpaper -> {
                    ZoomableGlideImage(
                            model = (wallpaper as WallhavenWallpaper).path,
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
                            .thumbnail(
                                    Glide.with(context)
                                        .load((wallpaper as WallhavenWallpaper).originalUrl)
                            )
                    }
                }
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
                        .liquidGlass(
                                providerState,
                                GlassStyle(
                                        shape = RoundedCornerShape(32.dp),
                                        innerRefraction = InnerRefraction(
                                                height = RefractionHeight(16.dp),
                                                amount = RefractionAmount((-100).dp)
                                        ),
                                        material = com.kyant.liquidglass.material.GlassMaterial(
                                                blurRadius = 8.dp,
                                                brush = SolidColor(Color.Transparent),
                                                alpha = 0.3f
                                        )
                                )
                        ),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent,
                    )
            ) {
                Text(
                        text = stateViewModel.getWallpaperName() ?: "",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                        fontSize = 20.sp
                )

                Text(
                        text = buildString {
                            append(stateViewModel.getWallpaperWidth() ?: 0)
                            append(" x ")
                            append(stateViewModel.getWallpaperHeight() ?: 0)
                            append(", ")
                            append(stateViewModel.getWallpaperSize()?.toSize() ?: "")
                            append(", ")
                            when (wallpaper) {
                                is Wallpaper -> {
                                    append(context.getString(R.string.times, setTimes?.usageCount ?: 0))
                                }
                                is WallhavenWallpaper -> {
                                    append((wallpaper as WallhavenWallpaper).category)
                                    append(", ")
                                    append(context.getString(R.string.views, (wallpaper as WallhavenWallpaper).viewsCount))
                                }
                            }
                        },
                        fontWeight = FontWeight.Light,
                        color = Color.White,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                        fontSize = 18.sp
                )

                if (tags.isNotEmpty() || wallhavenTags.isNotEmpty()) {
                    when (wallpaper) {
                        is Wallpaper -> {
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
                        }
                        is WallhavenWallpaper -> {
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
                                            .padding(start = 16.dp, bottom = 8.dp)
                                )
                                LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp, bottom = 8.dp)
                                ) {
                                    item {
                                        AssistChip(
                                                onClick = {
                                                    val wallpaperId = (wallpaper as? WallhavenWallpaper)?.id ?: ""
                                                    val filter = navController.previousBackStackEntry?.savedStateHandle
                                                        ?.get<WallhavenFilter>(Routes.WALLHAVEN_FILTER)
                                                        ?.copy(query = "like:$wallpaperId")
                                                    navController.navigate(Routes.WALLHAVEN) {
                                                        navController.currentBackStackEntry?.savedStateHandle
                                                            ?.set(Routes.WALLHAVEN_ARG, filter)
                                                    }
                                                },
                                                label = { Text("like:${(wallpaper as WallhavenWallpaper).id}") },
                                                modifier = Modifier.padding(end = 8.dp),
                                                colors = AssistChipDefaults.assistChipColors(
                                                        containerColor = Color.White.copy(alpha = 0.15f),
                                                        labelColor = Color.White
                                                )
                                        )
                                    }
                                    items(wallhavenTags.size) { position ->
                                        AssistChip(
                                                onClick = {
                                                    val filter = navController.previousBackStackEntry?.savedStateHandle?.get<WallhavenFilter>(Routes.WALLHAVEN_FILTER)?.copy(
                                                            query = "id:" + wallhavenTags[position].id.toString()
                                                    )
                                                    navController.navigate(Routes.WALLHAVEN) {
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(Routes.WALLHAVEN_ARG, filter)
                                                    }
                                                },
                                                label = {
                                                    Text(wallhavenTags[position].name)
                                                },
                                                modifier = Modifier.padding(
                                                        end = if (position == wallhavenTags.size - 1) 16.dp else 8.dp
                                                ),
                                                colors = AssistChipDefaults.assistChipColors(
                                                        containerColor = Color.White.copy(alpha = 0.15f),
                                                        labelColor = Color.White
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column {
                    val showWallpaperLaunchedEffect = remember { mutableStateOf(false) }

                    if (showWallpaperLaunchedEffect.value) {
                        LaunchedEffect(showWallpaperLaunchedEffect) {
                            coroutineScope.launch {
                                bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                    .copy(MainComposePreferences.getWallpaperColorSpace(), true)
                                    .applyEffects(
                                            blur = blurValue.times(Misc.BLUR_TIMES),
                                            colorMatrix = colorMatrix
                                    )

                                showScreenSelectionDialog = true
                                showWallpaperLaunchedEffect.value = false

                                try {
                                    context.stopService(LiveAutoWallpaperService.getIntent(context))
                                } catch (_: IllegalStateException) {
                                    Log.e("Wallpaper", "Service not running")
                                }
                            }
                        }
                    }

                    Row {
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
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(12.dp),
                                    fontWeight = FontWeight.SemiBold
                            )

                            Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp)
                                        .padding(end = 8.dp)
                            )
                        }

                        Button(
                                onClick = {
                                    launchEffectActivity.value = true
                                },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .weight(1F)
                                    .padding(start = 16.dp, bottom = 16.dp, end = 16.dp),
                                colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Unspecified,
                                )
                        ) {
                            Text(
                                    text = context.getString(R.string.saved_effects),
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(12.dp),
                                    fontWeight = FontWeight.SemiBold
                            )

                            Icon(
                                    imageVector = Icons.Rounded.Bookmarks,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(24.dp)
                                        .padding(end = 8.dp)
                            )
                        }
                    }

                    Row {
                        Button(
                                onClick = {
                                    showWallpaperLaunchedEffect.value = true
                                },
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .wrapContentHeight()
                                    .weight(1F)
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

                        if (wallpaper is WallhavenWallpaper) {
                            Button(
                                    onClick = {
                                        showDownloadFolderScreen = true
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .wrapContentHeight()
                                        .wrapContentWidth()
                                        .padding(start = 0.dp, end = 16.dp, bottom = 16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White,
                                    )
                            ) {
                                Text(
                                        text = context.getString(R.string.save),
                                        color = Color.Black,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(12.dp),
                                        fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showScreenSelectionDialog) {
            when (wallpaper) {
                is Wallpaper -> {
                    ScreenSelectionDialog(
                            setShowDialog = { showScreenSelectionDialog = it },
                            context = context,
                            bitmap = bitmap!!,
                            wallpaper = wallpaper as Wallpaper
                    )
                }
                is WallhavenWallpaper -> {
                    var downloadedWallpaper by remember { mutableStateOf<Wallpaper?>(null) }
                    var showPleaseWaitDialog by remember { mutableStateOf(false) }
                    val context = LocalContext.current
                    val url = (wallpaper as WallhavenWallpaper).path
                    val fileName = "/wallhaven_${(wallpaper as WallhavenWallpaper).id}.jpg"

                    LaunchedEffect(url) {
                        showPleaseWaitDialog = true
                        val file = getCachedOrDownloadWallpaper(context, url, context.cacheDir.absolutePath + fileName)
                        if (file != null) {
                            downloadedWallpaper = Wallpaper.createFromFile(file, context)
                        }

                        bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                            .copy(MainComposePreferences.getWallpaperColorSpace(), true)
                            .applyEffects(
                                    blur = blurValue.times(Misc.BLUR_TIMES),
                                    colorMatrix = colorMatrix
                            )

                        showPleaseWaitDialog = false
                    }

                    if (showPleaseWaitDialog) {
                        PleaseWaitDialog(stateText = stringResource(R.string.downloading)) { /* no-op */ }
                    }

                    if (downloadedWallpaper != null) {
                        ScreenSelectionDialog(
                                setShowDialog = { showScreenSelectionDialog = it },
                                context = context,
                                bitmap = bitmap!!,
                                wallpaper = downloadedWallpaper!!
                        )
                    }
                }
            }
        }
    }
}

suspend fun getCachedOrDownloadWallpaper(context: Context, imageUrl: String, absolutePath: String): File? = withContext(Dispatchers.IO) {
    try {
        // Try to get the cached file from Glide
        val future = Glide.with(context)
            .downloadOnly()
            .load(imageUrl)
            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
        val cachedFile = future.get() // This is blocking, so it's safe in Dispatchers.IO

        if (cachedFile != null && cachedFile.exists()) {
            val destFile = File(absolutePath)
            if (cachedFile.absolutePath != destFile.absolutePath) {
                destFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
                cachedFile.copyTo(destFile, overwrite = true)
            }
            return@withContext destFile
        }
    } catch (_: Exception) {
        // Ignore and fallback to download
    }
    // Fallback to your download logic
    downloadWallpaper(imageUrl, absolutePath)
}

suspend fun downloadWallpaper(imageUrl: String, absolutePath: String): File? = withContext(Dispatchers.IO) {
    var connection: HttpURLConnection? = null
    try {
        val url = URL(imageUrl)
        connection = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 10000
            readTimeout = 15000
            instanceFollowRedirects = true
            requestMethod = "GET"
            connect()
        }

        // Handle HTTP redirects manually if needed
        if (connection.responseCode in 300..399) {
            val newUrl = connection.getHeaderField("Location")
            connection.disconnect()
            return@withContext downloadWallpaper(newUrl, absolutePath)
        }

        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            Log.e("Download", "Server returned HTTP ${connection.responseCode}")
            return@withContext null
        }

        val file = File(absolutePath)
        file.parentFile?.let { if (!it.exists()) it.mkdirs() }
        if (file.exists()) {
            Log.w("Download", "File already exists: ${file.absolutePath}")
            return@withContext file
        }

        FileOutputStream(file).use { output ->
            connection.inputStream.use { input ->
                input.copyTo(output)
            }
        }

        Log.d("Download", "Downloaded to ${file.absolutePath}")
        file
    } catch (e: Exception) {
        Log.e("Download", "Error: ${e.message}", e)
        null
    } finally {
        connection?.disconnect()
    }
}


