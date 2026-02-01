package com.example.dwhubfix.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dwhubfix.ui.dashboard.worker.WorkerHomeScreen

@Composable
fun WorkerDashboardNavigation(
    onNavigateBack: () -> Unit // Should probably logout or exit
) {
    val navController = rememberNavController()
    
    val navItems = listOf(
        DashboardNavItem("worker_home", "Beranda", Icons.Default.Home),
        DashboardNavItem("worker_jobs", "Job", Icons.Default.Work),
        DashboardNavItem("worker_wallet", "Dompet", Icons.Default.AccountBalanceWallet),
        DashboardNavItem("worker_profile", "Profil", Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "worker_home"

    DashboardShell(
        items = navItems,
        selectedRoute = currentRoute,
        onItemSelected = { route ->
            navController.navigate(route) {
                // Pop up to the start destination of the graph to
                // avoid building up a large stack of destinations
                // on the back stack as users select items
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                // Avoid multiple copies of the same destination when
                // reselecting the same item
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                restoreState = true
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "worker_home") {
            composable("worker_home") {
                WorkerHomeScreen(
                    onNavigateToDetail = { /* TODO */ }
                )
            }
            composable("worker_jobs") {
                com.example.dwhubfix.ui.dashboard.worker.MyJobsScreen(
                    onNavigateToDetail = { /* TODO: Navigate to Detail */ }
                )
            }
            composable("worker_wallet") {
                com.example.dwhubfix.ui.dashboard.worker.WalletScreen()
            }
            composable("worker_profile") {
                com.example.dwhubfix.ui.dashboard.worker.ProfileScreen(
                    onLogout = onNavigateBack
                )
            }
        }
    }
}
