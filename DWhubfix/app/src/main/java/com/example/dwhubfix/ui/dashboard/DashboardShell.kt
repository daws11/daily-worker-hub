package com.example.dwhubfix.ui.dashboard

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dwhubfix.ui.theme.Primary

data class DashboardNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null
)

@Composable
fun DashboardShell(
    items: List<DashboardNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        bottomBar = {
            DashboardBottomBar(
                items = items,
                selectedRoute = selectedRoute,
                onItemClick = onItemSelected
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()) 
                // We don't use top padding because we want content to go behind status bar if needed
                // or handle it individually per screen (e.g. map view)
        ) {
            content()
        }
    }
}

@Composable
fun DashboardBottomBar(
    items: List<DashboardNavItem>,
    selectedRoute: String,
    onItemClick: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val isSelected = selectedRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item.route) },
                label = {
                    Text(
                        text = item.label,
                        style = if (isSelected) 
                            MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        else 
                            MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp
                    )
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected && item.selectedIcon != null) item.selectedIcon else item.icon,
                        contentDescription = item.label
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = Color.Transparent, // No pill background
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}
