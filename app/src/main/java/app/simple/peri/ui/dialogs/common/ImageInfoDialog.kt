@file:Suppress("LocalVariableName")

package app.simple.peri.ui.dialogs.common

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.simple.peri.R
import app.simple.peri.models.Wallpaper
import app.simple.peri.utils.CommonUtils
import app.simple.peri.utils.FileUtils.toSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ImageInfoDialog(
        wallpaper: Wallpaper,
        onDismiss: () -> Unit
) {
    val UNKNOWN = "-"

    val context = LocalContext.current
    var format by remember { mutableStateOf(UNKNOWN) }
    var bitDepth by remember { mutableStateOf(UNKNOWN) }
    var colorSpace by remember { mutableStateOf(UNKNOWN) }

    LaunchedEffect(wallpaper) {
        withContext(Dispatchers.IO) {
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                if (wallpaper.filePath.isNotEmpty()) {
                    BitmapFactory.decodeFile(wallpaper.filePath, options)
                } else if (wallpaper.uri.isNotEmpty()) {
                    context.contentResolver.openInputStream(Uri.parse(wallpaper.uri))?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                }
                format = options.outMimeType?.substringAfterLast('/')?.uppercase(Locale.getDefault()) ?: UNKNOWN

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    bitDepth = options.outConfig?.name ?: UNKNOWN
                }

                colorSpace = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    options.outColorSpace?.name ?: UNKNOWN
                } else {
                    "SRGB"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(text = stringResource(R.string.information))
            },
            text = {
                LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            item {
                                InfoItem(
                                        label = stringResource(R.string.name),
                                        value = wallpaper.name ?: stringResource(R.string.unknown)
                                )
                                InfoItem(
                                        label = stringResource(R.string.file_path),
                                        value = wallpaper.filePath
                                )

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    InfoItem(
                                            label = stringResource(R.string.resolution),
                                            value = "${wallpaper.width ?: 0}x${wallpaper.height ?: 0}",
                                            modifier = Modifier.weight(1f)
                                    )

                                    val w = wallpaper.width ?: 0
                                    val h = wallpaper.height ?: 0
                                    val mp = if (w > 0 && h > 0) {
                                        String.format(LocalLocale.current.platformLocale, "%.1f MP", (w * h) / 1000000.0)
                                    } else {
                                        "0 MP"
                                    }

                                    InfoItem(
                                            label = stringResource(R.string.megapixels),
                                            value = mp,
                                            modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    val w = wallpaper.width ?: 0
                                    val h = wallpaper.height ?: 0
                                    val orientation = when {
                                        w > h -> stringResource(R.string.landscape)
                                        h > w -> stringResource(R.string.portrait)
                                        else -> "Square"
                                    }
                                    InfoItem(
                                            label = stringResource(R.string.orientation),
                                            value = orientation,
                                            modifier = Modifier.weight(1f)
                                    )

                                    val aspectRatio = if (w > 0 && h > 0) {
                                        val gcd = CommonUtils.gcd(w, h)
                                        "${w / gcd}:${h / gcd}"
                                    } else {
                                        "-"
                                    }

                                    InfoItem(
                                            label = stringResource(R.string.aspect_ratio),
                                            value = aspectRatio,
                                            modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    InfoItem(
                                            label = stringResource(R.string.size),
                                            value = wallpaper.size.toSize(),
                                            modifier = Modifier.weight(1f)
                                    )
                                    InfoItem(
                                            label = stringResource(R.string.format),
                                            value = format,
                                            modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    InfoItem(
                                            label = stringResource(R.string.bit_depth),
                                            value = bitDepth,
                                            modifier = Modifier.weight(1f)
                                    )
                                    InfoItem(
                                            label = stringResource(R.string.color_space),
                                            value = colorSpace,
                                            modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth()) {
                                    val prominentColorHex = String.format("#%06X", (0xFFFFFF and wallpaper.prominentColor))
                                    InfoItem(
                                            label = stringResource(R.string.prominent_color),
                                            value = prominentColorHex,
                                            modifier = Modifier.weight(1f)
                                    )
                                    InfoItem(
                                            label = stringResource(R.string.date),
                                            value = SimpleDateFormat.getDateInstance().format(Date(wallpaper.dateModified)),
                                            modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                )
            },
            confirmButton = {
                Button(onClick = { onDismiss() }) {
                    Text(stringResource(id = R.string.close))
                }
            }
    )
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.primary
        )
        Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
    }
}