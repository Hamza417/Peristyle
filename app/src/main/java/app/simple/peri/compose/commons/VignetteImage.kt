package app.simple.peri.compose.commons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import me.saket.telephoto.zoomable.glide.ZoomableGlideImage

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VignetteImage(
        model: Any,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        contentScale: ContentScale = ContentScale.Crop,
        colorFilter: ColorFilter? = null,
        onClick: () -> Unit = {},
        onLongClick: () -> Unit = {}
) {
    Box(
            modifier = modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    drawRect(
                            brush = Brush.radialGradient(
                                    colors = listOf(Color.Transparent, Color.Black),
                                    center = center,
                                    radius = size.minDimension / 2
                            ),
                            blendMode = BlendMode.Multiply
                    )
                }
                .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                )
    ) {
        ZoomableGlideImage(
                model = model,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale,
                colorFilter = colorFilter
        ) {
            it
                .disallowHardwareConfig()
        }
    }
}
