package app.simple.peri.compose.nav

import android.content.Context
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.simple.peri.compose.screens.AutoWallpaper
import app.simple.peri.compose.screens.Folders
import app.simple.peri.compose.screens.Home
import app.simple.peri.compose.screens.LiveAutoWallpaper
import app.simple.peri.compose.screens.LiveWallpapers
import app.simple.peri.compose.screens.Settings
import app.simple.peri.compose.screens.Setup
import app.simple.peri.compose.screens.Tags
import app.simple.peri.compose.screens.Wallpaper
import app.simple.peri.compose.screens.isSetupComplete
import app.simple.peri.compose.subscreens.TaggedWallpapers
import app.simple.peri.compose.subscreens.WallpaperList

private const val ANIMATION_DURATION = 400
private const val DELAY = 100

@Composable
fun PeristyleNavigation(context: Context) {
    val navController = rememberNavController()
    val startDestination = if (isSetupComplete(context)) Routes.HOME else Routes.SETUP

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SETUP) {
            Setup(context, navController)
        }

        composableWithTransitions(Routes.HOME) {
            Home(navController)
        }

        composableWithTransitions(Routes.WALLPAPER) {
            Wallpaper(navController)
        }

        composableWithTransitions(Routes.WALLPAPERS_LIST) {
            WallpaperList(navController)
        }

        composableWithTransitions(Routes.SETTINGS) {
            Settings(navController)
        }

        composableWithTransitions(Routes.AUTO_WALLPAPER) {
            AutoWallpaper(navController)
        }

        composableWithTransitions(Routes.LIVE_AUTO_WALLPAPER) {
            LiveAutoWallpaper(navController)
        }

        composableWithTransitions(Routes.TAGS) {
            Tags(navController)
        }

        composableWithTransitions(Routes.FOLDERS) {
            Folders(navController)
        }

        composableWithTransitions(Routes.TAGGED_WALLPAPERS) { backStackEntry ->
            TaggedWallpapers(navController)
        }

        composableWithTransitions(Routes.LIVE_WALLPAPERS) {
            LiveWallpapers(navController)
        }
    }
}

fun NavGraphBuilder.composableWithTransitions(
        route: String,
        content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
            route = route,
            enterTransition = { scaleIntoContainer() },
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) },
            popExitTransition = { scaleOutOfContainer() },
            content = content
    )
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

fun slideIntoContainer(
        direction: SlideTransitionDirection = SlideTransitionDirection.LEFT,
        offset: Int = 1000
): EnterTransition {
    return slideInHorizontally(
            initialOffsetX = { if (direction == SlideTransitionDirection.LEFT) offset else -offset },
            animationSpec = tween(ANIMATION_DURATION, delayMillis = DELAY)
    ) + fadeIn(animationSpec = tween(ANIMATION_DURATION, delayMillis = DELAY))
}

fun slideOutOfContainer(
        direction: SlideTransitionDirection = SlideTransitionDirection.LEFT,
        offset: Int = 1000
): ExitTransition {
    return slideOutHorizontally(
            targetOffsetX = { if (direction == SlideTransitionDirection.LEFT) -offset else offset },
            animationSpec = tween(ANIMATION_DURATION, delayMillis = DELAY)
    ) + fadeOut(tween(delayMillis = DELAY))
}

enum class ScaleTransitionDirection {
    INWARDS,
    OUTWARDS
}

enum class SlideTransitionDirection {
    LEFT,
    RIGHT
}
