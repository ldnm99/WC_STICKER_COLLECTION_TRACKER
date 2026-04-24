package com.example.wc2026stickers.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wc2026stickers.ui.duplicates.DuplicatesScreen
import com.example.wc2026stickers.ui.home.HomeScreen
import com.example.wc2026stickers.ui.missing.MissingScreen
import com.example.wc2026stickers.ui.quickadd.QuickAddScreen
import com.example.wc2026stickers.ui.teamdetail.TeamDetailScreen
import com.example.wc2026stickers.ui.teams.TeamsListScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Teams : Screen("teams")
    data object TeamDetail : Screen("team/{teamCode}") {
        fun createRoute(teamCode: String) = "team/$teamCode"
    }
    data object Missing : Screen("missing")
    data object Duplicates : Screen("duplicates")
    data object QuickAdd : Screen("quickadd")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTeams = { navController.navigate(Screen.Teams.route) },
                onNavigateToMissing = { navController.navigate(Screen.Missing.route) },
                onNavigateToDuplicates = { navController.navigate(Screen.Duplicates.route) },
                onNavigateToQuickAdd = { navController.navigate(Screen.QuickAdd.route) }
            )
        }

        composable(Screen.Teams.route) {
            TeamsListScreen(
                onBack = { navController.popBackStack() },
                onTeamClick = { code -> navController.navigate(Screen.TeamDetail.createRoute(code)) },
                onNavigateToQuickAdd = { navController.navigate(Screen.QuickAdd.route) }
            )
        }

        composable(
            route = Screen.TeamDetail.route,
            arguments = listOf(navArgument("teamCode") { type = NavType.StringType })
        ) {
            TeamDetailScreen(
                onBack = { navController.popBackStack() },
                onNavigateToQuickAdd = { navController.navigate(Screen.QuickAdd.route) }
            )
        }

        composable(Screen.Missing.route) {
            MissingScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Duplicates.route) {
            DuplicatesScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.QuickAdd.route) {
            QuickAddScreen(onBack = { navController.popBackStack() })
        }
    }
}
