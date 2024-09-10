package app.simple.peri.compose.nav

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.simple.peri.compose.screens.HomeScreen
import app.simple.peri.compose.screens.Setup
import app.simple.peri.compose.screens.WallpaperScreen
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
                Setup(context, navController)
            }
        }

        composable(Routes.HOME) {
            HomeScreen(context, navController)
        }

        composable(route = Routes.WALLPAPER) { backStackEntry ->
            WallpaperScreen(context, navController)
        }
    }
}
