package com.wc2026stickers.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wc2026stickers.app.ui.friendmatcher.FriendMatcherScreen
import com.wc2026stickers.app.ui.duplicates.DuplicatesScreen
import com.wc2026stickers.app.ui.home.HomeScreen
import com.wc2026stickers.app.ui.kpiranking.KpiRankingScreen
import com.wc2026stickers.app.ui.missing.MissingScreen
import com.wc2026stickers.app.ui.quickadd.QuickAddScreen
import com.wc2026stickers.app.ui.search.SearchScreen
import com.wc2026stickers.app.ui.teamdetail.TeamDetailScreen
import com.wc2026stickers.app.ui.teams.TeamsListScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Teams : Screen("teams")
    data object TeamDetail : Screen("team/{teamCode}") {
        fun createRoute(teamCode: String) = "team/$teamCode"
    }
    data object Missing : Screen("missing")
    data object FriendMatcher : Screen("friend-matcher")
    data object Duplicates : Screen("duplicates")
    data object QuickAdd : Screen("quickadd")
    data object Search : Screen("search")
    data object KpiRanking : Screen("kpi/{kpiType}") {
        fun createRoute(kpiType: String) = "kpi/$kpiType"
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTeams = { navController.navigate(Screen.Teams.route) },
                onNavigateToTeam = { code -> navController.navigate(Screen.TeamDetail.createRoute(code)) },
                onNavigateToMissing = { navController.navigate(Screen.Missing.route) },
                onNavigateToFriendMatcher = { navController.navigate(Screen.FriendMatcher.route) },
                onNavigateToDuplicates = { navController.navigate(Screen.Duplicates.route) },
                onNavigateToQuickAdd = { navController.navigate(Screen.QuickAdd.route) },
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToKpiRanking = { kpiType -> navController.navigate(Screen.KpiRanking.createRoute(kpiType)) }
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

        composable(Screen.FriendMatcher.route) {
            FriendMatcherScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Duplicates.route) {
            DuplicatesScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.QuickAdd.route) {
            QuickAddScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Search.route) {
            SearchScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Screen.KpiRanking.route,
            arguments = listOf(navArgument("kpiType") { type = NavType.StringType })
        ) {
            KpiRankingScreen(
                onBack = { navController.popBackStack() },
                onTeamClick = { code -> navController.navigate(Screen.TeamDetail.createRoute(code)) }
            )
        }
    }
}
