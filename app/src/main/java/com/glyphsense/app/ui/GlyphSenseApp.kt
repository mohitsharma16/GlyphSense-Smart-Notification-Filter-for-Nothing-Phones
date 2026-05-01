package com.glyphsense.app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.glyphsense.app.ui.apps.AppPickerScreen
import com.glyphsense.app.ui.home.HomeScreen

@Composable
fun GlyphSenseApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(onManageApps = { navController.navigate(Routes.APPS) })
        }
        composable(Routes.APPS) {
            AppPickerScreen(onBack = { navController.popBackStack() })
        }
    }
}

private object Routes {
    const val HOME = "home"
    const val APPS = "apps"
}