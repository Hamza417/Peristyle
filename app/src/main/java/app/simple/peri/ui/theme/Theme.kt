package app.simple.peri.ui.theme

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.Window
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import app.simple.peri.preferences.MainComposePreferences

private val DarkColorScheme = darkColorScheme(
        primary = Purple80,
        secondary = PurpleGrey80,
        tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
        primary = Purple40,
        secondary = PurpleGrey40,
        tertiary = Pink40
)

val LocalBarsSize = staticCompositionLocalOf<BarSize> {
    error("No BarsSize provided!")
}

@Composable
fun PeristyleTheme(
        // Dynamic color is available on Android 12+
        dynamicColor: Boolean = true,
        content: @Composable () -> Unit
) {
    val darkTheme = when (MainComposePreferences.getThemeMode()) {
        MainComposePreferences.THEME_MODE_DARK -> true
        MainComposePreferences.THEME_MODE_LIGHT -> false
        else -> isSystemInDarkTheme()
    }
    var statusBarHeight by remember { mutableIntStateOf(0) }
    var navigationBarHeight by remember { mutableIntStateOf(0) }

    statusBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.statusBars()).top
    navigationBarHeight = WindowInsetsCompat.toWindowInsetsCompat(
            LocalView.current.rootWindowInsets).getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

    val statusBarHeightPx = statusBarHeight
    val statusBarHeightDp = with(LocalDensity.current) { statusBarHeightPx.toDp() }
    val navigationBarHeightPx = navigationBarHeight
    val navigationBarHeightDp = with(LocalDensity.current) { navigationBarHeightPx.toDp() }

    val barSize = BarSize(statusBarHeightDp, navigationBarHeightDp)

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            makeAppFullScreen(window)

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme.not()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = darkTheme.not()
        }
    }

    CompositionLocalProvider(LocalBarsSize provides barSize) {
        MaterialTheme(
                colorScheme = colorScheme,
                typography = Typography,
                content = content
        )
    }
}

@Preview
@Composable
fun PeristyleThemePreview(
        @PreviewParameter(BooleanPreviewParameterProvider::class) darkTheme: Boolean
) {
    PeristyleTheme(darkTheme = darkTheme) {
        // Your Composable content here
    }
}

class BooleanPreviewParameterProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean> = sequenceOf(true, false)
}

@Suppress("DEPRECATION")
private fun makeAppFullScreen(window: Window) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT

    // Disable navigation bar contrast
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
        window.isStatusBarContrastEnforced = false
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        window.navigationBarDividerColor = Color.TRANSPARENT
    }
}

data class BarSize(
        val statusBarHeight: Dp = 0.dp,
        val navigationBarHeight: Dp = 0.dp
)