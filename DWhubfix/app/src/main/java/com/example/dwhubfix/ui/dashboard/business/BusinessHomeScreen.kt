package com.example.dwhubfix.ui.dashboard.business

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.dwhubfix.ui.theme.DailyWorkerHubTheme
import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.model.formatCurrency
import com.example.dwhubfix.presentation.business.home.BusinessHomeUiEvent
import com.example.dwhubfix.presentation.business.home.BusinessHomeViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHomeScreen(
    onNavigateToPostJob: () -> Unit,
    onNavigateToFindWorker: () -> Unit,
    onNavigateToWallet: () -> Unit,
    viewModel: BusinessHomeViewModel = hiltViewModel()
) {
    // Collect state from ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Dashboard Bisnis",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToPostJob,
                containerColor = Color(0xFF13EC5B)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Buat Lowongan")
            }
        },
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
        containerColor = Color(0xFFF9FAFB)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Stats Cards Row
            if (uiState.stats != null && !uiState.isLoading) {
                item {
                    StatsCardsRow(
                        activeShiftsToday = uiState.stats!!.activeShiftsToday,
                        workersHiredThisWeek = uiState.stats!!.workersHiredThisWeek,
                        totalSpendingThisMonth = uiState.stats!!.totalSpendingThisMonth,
                        pendingPatches = uiState.stats!!.pendingPatches
                    )
                }
            }

            // 2. Quick Actions
            item {
                QuickActionsSection(onNavigateToPostJob, onNavigateToFindWorker)
            }

            // 3. Loading State for Initial Load
            if (uiState.isLoading) {
                item {
                    LoadingStateCard()
                }
            } else {
                // 4. Recent Activity (placeholder - would need actual jobs)
                item {
                    EmptyRecentActivityCard()
                }
            }

            // 5. Wallet Quick View
            if (uiState.stats != null) {
                item {
                    WalletQuickViewCard(
                        balance = uiState.stats!!.walletBalance,
                        onNavigateToWallet = onNavigateToWallet
                    )
                }
            }
        }
    }
}

@Composable
fun StatsCardsRow(
    activeShiftsToday: Int,
    workersHiredThisWeek: Int,
    totalSpendingThisMonth: Double,
    pendingPatches: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Active Shifts Today
        StatCard(
            icon = Icons.Default.DateRange,
            title = "Shift Hari Ini",
            value = activeShiftsToday.toString(),
            color = Color(0xFF13EC5B),
            modifier = Modifier.weight(1f)
        )

        // Workers Hired This Week
        StatCard(
            icon = Icons.Default.Group,
            title = "Worker Tersewa",
            value = workersHiredThisWeek.toString(),
            color = Color(0xFF10B981),
            modifier = Modifier.weight(1f)
        )

        // Total Spending This Month
        StatCard(
            icon = Icons.Default.AttachMoney,
            title = "Pengeluaran",
            value = "Rp ${formatCurrency(totalSpendingThisMonth.toInt())}",
            color = Color(0xFFEF4444),
            modifier = Modifier.weight(1f)
        )

        // Pending Patches
        StatCard(
            icon = Icons.Default.EditNote,
            title = "Patch Pending",
            value = pendingPatches.toString(),
            color = Color(0xFFF59E0B),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
            )
        }
    }
}

@Composable
fun QuickActionsSection(
    onNavigateToPostJob: () -> Unit,
    onNavigateToFindWorker: () -> Unit
) {
    Column {
        Text(
            "Aksi Cepat",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.Edit,
                title = "Buat Lowongan",
                subtitle = "Posting pekerjaan baru",
                color = Color(0xFF13EC5B),
                onClick = onNavigateToPostJob,
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Default.PersonSearch,
                title = "Cari Worker",
                subtitle = "Jelajahi talent",
                color = Color.White,
                onClick = onNavigateToFindWorker,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (color == Color.White) Color(0xFF13EC5B) else Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (color == Color.White) Color(0xFF111813) else Color.White
                )
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = if (color == Color.White) Color(0xFF6B7280) else Color.White.copy(alpha = 0.8f)
                )
            )
        }
    }
}

@Composable
fun WalletQuickViewCard(
    balance: Double,
    onNavigateToWallet: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.clickable(onClick = onNavigateToWallet).fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Saldo Dompet",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                )
                Text(
                    "Rp ${formatCurrency(balance.toInt())}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF6B7280)
            )
        }
    }
}

@Composable
fun LoadingStateCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().height(200.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFF13EC5B)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Memuat data...",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
            )
        }
    }
}

@Composable
fun EmptyRecentActivityCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Inbox,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(48.dp).padding(bottom = 16.dp)
            )
            Text(
                "Belum ada lowongan",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
            )
            Text(
                "Buat lowongan pekerjaan pertama Anda",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280)),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* Navigate to Post Job */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF13EC5B),
                    contentColor = Color.White
                )
            ) {
                Text("Buat Lowongan", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Preview
@Composable
fun BusinessHomeScreenPreview() {
    DailyWorkerHubTheme {
        BusinessHomeScreen(
            onNavigateToPostJob = {},
            onNavigateToFindWorker = {},
            onNavigateToWallet = {}
        )
    }
}
