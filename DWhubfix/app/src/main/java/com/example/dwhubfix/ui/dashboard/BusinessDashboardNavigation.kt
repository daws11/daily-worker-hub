package com.example.dwhubfix.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.dwhubfix.ui.dashboard.business.BusinessHomeScreen
import com.example.dwhubfix.ui.dashboard.business.JobPostingScreen
import com.example.dwhubfix.ui.dashboard.business.WorkerManagementScreen
import com.example.dwhubfix.ui.dashboard.business.BusinessProfileScreen

@Composable
fun BusinessDashboardNavigation(
    onNavigateBack: () -> Unit
) {
    val navController = rememberNavController()
    
    val navItems = listOf(
        DashboardNavItem("business_home", "Beranda", Icons.Default.Home),
        DashboardNavItem("business_post_job", "Buat Lowongan", Icons.Default.Add),
        DashboardNavItem("business_workers", "Pekerja", Icons.Default.People),
        DashboardNavItem("business_profile", "Profil", Icons.Default.Person)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "business_home"

    DashboardShell(
        items = navItems,
        selectedRoute = currentRoute,
        onItemSelected = { route ->
            navController.navigate(route) {
                // Fixed navigation options temporarily to unblock build
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "business_home") {
            composable("business_home") {
                BusinessHomeScreen(
                    onNavigateToPostJob = { navController.navigate("business_post_job") },
                    onNavigateToFindWorker = { navController.navigate("business_workers") },
                    onNavigateToWallet = { /* TODO: Navigate to wallet */ }
                )
            }
            composable("business_post_job") {
                JobPostingScreen()
            }
            composable("business_workers") {
                WorkerManagementScreen()
            }
            composable("business_profile") {
                BusinessProfileScreen()
            }
        }
    }
}
