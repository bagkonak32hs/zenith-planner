@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.selcuk.zenithplanner.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.selcuk.zenithplanner.R
import com.selcuk.zenithplanner.ZenithPlannerApplication
import com.selcuk.zenithplanner.ui.theme.PlannerBlack
import com.selcuk.zenithplanner.ui.theme.PlannerPaper

internal enum class PlannerDestination(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    Dashboard("dashboard", R.string.dashboard, Icons.Outlined.Home),
    Calendar("calendar", R.string.calendar, Icons.Outlined.CalendarMonth),
    Daily("daily", R.string.daily_planner, Icons.Outlined.Today),
    Goals("goals", R.string.goals, Icons.Outlined.Flag),
    Habits("habits", R.string.habits, Icons.Outlined.CheckCircle),
    Motivation("motivation", R.string.motivation, Icons.Outlined.FormatQuote),
    Notes("notes", R.string.notes, Icons.Outlined.Notes),
    Finance("finance", R.string.finance, Icons.Outlined.AccountBalanceWallet),
    Settings("settings", R.string.settings, Icons.Outlined.Settings)
}

@Composable
fun ZenithPlannerApp() {
    val application = LocalContext.current.applicationContext as ZenithPlannerApplication
    val viewModel: PlannerViewModel = viewModel(
        factory = PlannerViewModel.factory(
            application.repository,
            application.firebaseSyncManager,
            application.subscriptionManager
        )
    )
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: PlannerDestination.Dashboard.route
    val useRail = LocalConfiguration.current.screenWidthDp >= 720

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (!useRail) {
                PlannerBottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (useRail) {
                PlannerRail(navController = navController, currentRoute = currentRoute)
            }
            val onUpgrade = { navController.navigate("subscription") }
            NavHost(
                navController = navController,
                startDestination = PlannerDestination.Dashboard.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(PlannerDestination.Dashboard.route) {
                    DashboardScreen(viewModel, onNavigate = { navController.navigateTopLevel(it.route) })
                }
                composable(PlannerDestination.Calendar.route) {
                    CalendarScreen(viewModel, onOpenDay = { navController.navigateTopLevel(PlannerDestination.Daily.route) })
                }
                composable(PlannerDestination.Daily.route) {
                    DailyPlannerScreen(viewModel)
                }
                composable(PlannerDestination.Goals.route) {
                    GoalsScreen(viewModel, onUpgrade = onUpgrade)
                }
                composable(PlannerDestination.Habits.route) {
                    HabitsScreen(viewModel)
                }
                composable(PlannerDestination.Motivation.route) {
                    MotivationScreen(viewModel)
                }
                composable(PlannerDestination.Notes.route) {
                    NotesScreen(viewModel, onUpgrade = onUpgrade)
                }
                composable(PlannerDestination.Finance.route) {
                    FinanceScreen(viewModel, onUpgrade = onUpgrade)
                }
                composable(PlannerDestination.Settings.route) {
                    SettingsScreen(viewModel, onUpgrade = onUpgrade)
                }
                composable("subscription") {
                    ProUpgradeScreen(viewModel, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

private fun NavController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private val bottomBarDestinations = listOf(
    PlannerDestination.Dashboard,
    PlannerDestination.Daily,
    PlannerDestination.Habits,
    PlannerDestination.Finance,
    PlannerDestination.Settings
)

@Composable
private fun PlannerBottomBar(navController: NavController, currentRoute: String) {
    NavigationBar(containerColor = PlannerPaper) {
        bottomBarDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { navController.navigateTopLevel(destination.route) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.titleRes), maxLines = 1, overflow = TextOverflow.Ellipsis) }
            )
        }
    }
}

@Composable
private fun PlannerRail(navController: NavController, currentRoute: String) {
    NavigationRail(containerColor = PlannerBlack) {
        Spacer(Modifier.height(16.dp))
        PlannerDestination.entries.forEach { destination ->
            NavigationRailItem(
                selected = currentRoute == destination.route,
                onClick = { navController.navigateTopLevel(destination.route) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(stringResource(destination.titleRes), maxLines = 1) },
                alwaysShowLabel = false
            )
        }
    }
}

