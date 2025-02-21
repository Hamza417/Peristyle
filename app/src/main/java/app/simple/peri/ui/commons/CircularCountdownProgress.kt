package app.simple.peri.ui.commons

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.simple.peri.utils.CommonUtils.toSeconds
import app.simple.peri.viewmodels.HomeScreenViewModel

@Composable
fun CircularCountdownProgress(modifier: Modifier = Modifier) {
    val homeScreenViewModel: HomeScreenViewModel = viewModel(
            LocalActivity.current as ComponentActivity
    )

    val progress = homeScreenViewModel.countDownFlow
        .collectAsState(initial = HomeScreenViewModel.RANDOM_WALLPAPER_DELAY).value.toFloat().toSeconds()
        .div(HomeScreenViewModel.RANDOM_WALLPAPER_DELAY.toFloat().toSeconds())
        .coerceIn(0f, 1f)

    CircularProgressIndicator(
            progress = { progress },
            modifier = modifier
                .size(32.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            gapSize = 0.dp,
            trackColor = Color.Transparent
    )
}
