package app.simple.peri.ui.nav

import android.content.Context
import android.os.Build
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.activity.ComponentActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.simple.peri.preferences.MainComposePreferences
import app.simple.peri.preferences.SharedPreferences
import app.simple.peri.ui.screens.AutoWallpaper
import app.simple.peri.ui.screens.Folders
import app.simple.peri.ui.screens.Home
import app.simple.peri.ui.screens.LiveAutoWallpaper
import app.simple.peri.ui.screens.LiveWallpapers
import app.simple.peri.ui.screens.Settings
import app.simple.peri.ui.screens.Setup
import app.simple.peri.ui.screens.Tags
import app.simple.peri.ui.screens.Wallpaper
import app.simple.peri.ui.screens.isSetupComplete
import app.simple.peri.ui.subscreens.TaggedWallpapers
import app.simple.peri.ui.subscreens.WallpaperList
import app.simple.peri.utils.ConditionUtils.invert

private const val ANIMATION_DURATION = 400
private const val DELAY = 100

private var predictiveBackCallback: OnBackInvokedCallback? = null

@Composable
fun PeristyleNavigation(context: Context) {

    val navController = rememberNavController()
    val disableAnimations = remember { mutableStateOf(MainComposePreferences.getDisableAnimations()) }
    val predictiveBack = remember { mutableStateOf(MainComposePreferences.isPredictiveBack()) }
    val startDestination = if (isSetupComplete(context)) Routes.HOME else Routes.SETUP

    SharedPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
        if (key == MainComposePreferences.DISABLE_ANIMATIONS) {
            disableAnimations.value = sharedPreferences.getBoolean(MainComposePreferences.DISABLE_ANIMATIONS, false)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (disableAnimations.value || predictiveBack.value.invert()) {
            disablePredictiveBack(context as ComponentActivity) {
                navController.popBackStack()
            }
        } else {
            enablePredictiveBack(context as ComponentActivity)
        }
    }

    NavHost(
            navController = navController,
            startDestination = startDestination,
            enterTransition = { if (disableAnimations.value) EnterTransition.None else scaleIntoContainer() },
            exitTransition = { if (disableAnimations.value) ExitTransition.None else scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { if (disableAnimations.value) EnterTransition.None else scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { if (disableAnimations.value) ExitTransition.None else scaleOutOfContainer() },
    ) {
        composable(Routes.SETUP) {
            Setup(context, navController)
        }

        composable(Routes.HOME) {
            Home(navController)
        }

        composable(Routes.WALLPAPER) {
            Wallpaper(navController)
        }

        composable(Routes.WALLPAPERS_LIST) {
            WallpaperList(navController)
        }

        composable(Routes.SETTINGS) {
            Settings(navController)
        }

        composable(Routes.AUTO_WALLPAPER) {
            AutoWallpaper(navController)
        }

        composable(Routes.LIVE_AUTO_WALLPAPER) {
            LiveAutoWallpaper(navController)
        }

        composable(Routes.TAGS) {
            Tags(navController)
        }

        composable(Routes.FOLDERS) {
            Folders(navController)
        }

        composable(Routes.TAGGED_WALLPAPERS) { backStackEntry ->
            TaggedWallpapers(navController)
        }

        composable(Routes.LIVE_WALLPAPERS) {
            LiveWallpapers(navController)
        }
    }
}

fun scaleIntoContainer(
        direction: ScaleTransitionDirection = ScaleTransitionDirection.INWARDS,
        initialScale: Float = if (direction == ScaleTransitionDirection.OUTWARDS) 0.9f else 1.1f
): EnterTransition {
    return scaleIn(
            animationSpec = tween(ANIMATION_DURATION, delayMillis = DELAY),
            initialScale = initialScale
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION, delayMillis = DELAY))
}

fun scaleOutOfContainer(
        direction: ScaleTransitionDirection = ScaleTransitionDirection.OUTWARDS,
        targetScale: Float = if (direction == ScaleTransitionDirection.INWARDS) 0.9f else 1.1f
): ExitTransition {
    return scaleOut(
            animationSpec = tween(
                    durationMillis = ANIMATION_DURATION,
                    delayMillis = DELAY
            ), targetScale = targetScale
    ) + fadeOut(tween(delayMillis = DELAY))
}

fun disablePredictiveBack(activity: ComponentActivity, onBack: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && predictiveBackCallback == null) {
        predictiveBackCallback = OnBackInvokedCallback {
            onBack()
        }
        activity.onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_OVERLAY,
                predictiveBackCallback!!
        )
    }
}

fun enablePredictiveBack(activity: ComponentActivity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && predictiveBackCallback != null) {
        activity.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(predictiveBackCallback!!)
        predictiveBackCallback = null
    }
}

enum class ScaleTransitionDirection {
    INWARDS,
    OUTWARDS
}
