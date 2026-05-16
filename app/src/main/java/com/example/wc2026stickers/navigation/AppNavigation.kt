package com.wc2026stickers.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wc2026stickers.app.ui.duplicates.DuplicatesScreen
import com.wc2026stickers.app.ui.friendmatcher.FriendMatcherScreen
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

private data class NavTab(val screen: Screen, val label: String, val icon: ImageVector)

private val NAV_TABS = listOf(
    NavTab(Screen.Home, "Home", Icons.Default.Home),
    NavTab(Screen.Teams, "Teams", Icons.Default.Groups),
    NavTab(Screen.QuickAdd, "Quick Add", Icons.Default.Add),
)

private val TOP_LEVEL_ROUTES = NAV_TABS.map { it.screen.route }.toSet()

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentRoute in TOP_LEVEL_ROUTES,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar {
                    NAV_TABS.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.screen.route,
                            onClick = {
                                navController.navigate(tab.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = null) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToTeam = { code -> navController.navigate(Screen.TeamDetail.createRoute(code)) },
                    onNavigateToMissing = { navController.navigate(Screen.Missing.route) },
                    onNavigateToFriendMatcher = { navController.navigate(Screen.FriendMatcher.route) },
                    onNavigateToDuplicates = { navController.navigate(Screen.Duplicates.route) },
                    onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                    onNavigateToKpiRanking = { kpiType -> navController.navigate(Screen.KpiRanking.createRoute(kpiType)) }
                )
            }

            composable(Screen.Teams.route) {
                TeamsListScreen(
                    onTeamClick = { code -> navController.navigate(Screen.TeamDetail.createRoute(code)) },
                    onNavigateToQuickAdd = {
                        navController.navigate(Screen.QuickAdd.route) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(
                route = Screen.TeamDetail.route,
                arguments = listOf(navArgument("teamCode") { type = NavType.StringType })
            ) {
                TeamDetailScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToQuickAdd = {
                        navController.navigate(Screen.QuickAdd.route) {
                            launchSingleTop = true
                        }
                    }
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
                val prevRoute = navController.previousBackStackEntry?.destination?.route
                QuickAddScreen(
                    onBack = if (prevRoute != null && prevRoute !in TOP_LEVEL_ROUTES) {
                        { navController.popBackStack() }
                    } else null
                )
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
}
