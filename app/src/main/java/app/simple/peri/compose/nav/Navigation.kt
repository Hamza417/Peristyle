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
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.simple.peri.compose.screens.AutoWallpaper
import app.simple.peri.compose.screens.FolderList
import app.simple.peri.compose.screens.Folders
import app.simple.peri.compose.screens.Home
import app.simple.peri.compose.screens.Settings
import app.simple.peri.compose.screens.Setup
import app.simple.peri.compose.screens.TaggedWallpapers
import app.simple.peri.compose.screens.Tags
import app.simple.peri.compose.screens.Wallpaper
import app.simple.peri.compose.screens.isSetupComplete
import app.simple.peri.utils.ConditionUtils.invert

@Composable
fun PeristyleNavigation(context: Context) {
    val navController = rememberNavController()
    val startDestination = if (isSetupComplete(context)) Routes.HOME else Routes.SETUP

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.SETUP) {
            if (isSetupComplete(context).invert()) {
                Setup(context, navController)
            } else {
                navController.navigate(Routes.HOME)
            }
        }

        composableWithTransitions(Routes.HOME) {
            Home(navController)
        }

        composableWithTransitions(Routes.WALLPAPER) {
            Wallpaper(context, navController)
        }

        composableWithTransitions(Routes.WALLPAPERS_LIST) {
            FolderList(navController)
        }

        composableWithTransitions(Routes.SETTINGS) {
            Settings(navController)
        }

        composableWithTransitions(Routes.AUTO_WALLPAPER) {
            AutoWallpaper(navController)
        }

        composableWithTransitions(Routes.TAGS) {
            Tags(navController)
        }

        composableWithTransitions(Routes.FOLDERS) {
            Folders(navController)
        }

        composableWithTransitions("${Routes.TAGGED_WALLPAPERS}/{tag}") { backStackEntry ->
            val tag = backStackEntry.arguments?.getString("tag")
            TaggedWallpapers(navController, tag)
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
            animationSpec = tween(220, delayMillis = 90),
            initialScale = initialScale
    ) + fadeIn(animationSpec = tween(220, delayMillis = 90))
}

fun scaleOutOfContainer(
        direction: ScaleTransitionDirection = ScaleTransitionDirection.OUTWARDS,
        targetScale: Float = if (direction == ScaleTransitionDirection.INWARDS) 0.9f else 1.1f
): ExitTransition {
    return scaleOut(
            animationSpec = tween(
                    durationMillis = 220,
                    delayMillis = 90
            ), targetScale = targetScale
    ) + fadeOut(tween(delayMillis = 90))
}

enum class ScaleTransitionDirection {
    INWARDS,
    OUTWARDS
}
