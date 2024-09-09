package app.simple.peri.compose.nav

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.simple.peri.compose.screens.HomeScreen

@Composable
fun PeristyleNavigation(context: Context) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(context, navController)
        }

        composable(Routes.LIST) {
            // ListScreen()
        }

        composable(Routes.WALLPAPER) {
            // WallpaperScreen()
        }

        composable(Routes.SETTINGS) {
            // SettingsScreen()
        }
    }
}
