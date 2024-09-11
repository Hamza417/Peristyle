package app.simple.peri.compose.nav

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.simple.peri.compose.screens.AutoWallpaper
import app.simple.peri.compose.screens.Home
import app.simple.peri.compose.screens.List
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

    NavHost(navController = navController, startDestination = Routes.SETUP) {
        composable(Routes.SETUP) {
            if (isSetupComplete(context).invert()) {
                Setup(context, navController)
            } else {
                navController.navigate(Routes.HOME)
            }
        }

        composable(Routes.HOME) {
            Home(navController)
        }

        composable(route = Routes.WALLPAPER) {
            Wallpaper(context, navController)
        }

        composable(route = Routes.WALLPAPERS) {
            List(navController)
        }

        composable(route = Routes.SETTINGS) {
            Settings(navController)
        }

        composable(route = Routes.AUTO_WALLPAPER) {
            AutoWallpaper(navController)
        }

        composable(route = Routes.TAGS) {
            Tags(navController)
        }

        composable(route = "${Routes.TAGGED_WALLPAPERS}/{tag}") { backStackEntry ->
            val tag = backStackEntry.arguments?.getString("tag")
            TaggedWallpapers(navController, tag)
        }
    }
}
