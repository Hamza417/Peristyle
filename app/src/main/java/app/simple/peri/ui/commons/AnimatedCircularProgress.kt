package app.simple.peri.ui.commons

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.simple.peri.utils.CommonUtils.toSeconds
import app.simple.peri.viewmodels.HomeScreenViewModel

@Composable
fun AnimatedCircularProgress(modifier: Modifier = Modifier) {
    val homeScreenViewModel: HomeScreenViewModel = viewModel(
            LocalActivity.current as ComponentActivity
    )

    val progress = homeScreenViewModel.countDownFlow
        .collectAsState(initial = HomeScreenViewModel.RANDOM_WALLPAPER_DELAY).value.toFloat().toSeconds()
        .div(HomeScreenViewModel.RANDOM_WALLPAPER_DELAY.toFloat().toSeconds())
        .coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "")

    Canvas(modifier = modifier.size(32.dp)) {
        val strokeWidth = 4.dp.toPx()
        val radius = size.minDimension / 2 - strokeWidth / 2
        val center = Offset(size.width / 2, size.height / 2)

        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color.Red, Color.Yellow, Color.Green)
            ),
            center = center,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )

        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(Color.Cyan, Color.Magenta, Color.Blue)
            ),
            startAngle = -90f,
            sweepAngle = 360 * animatedProgress,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth)
        )
    }
}
