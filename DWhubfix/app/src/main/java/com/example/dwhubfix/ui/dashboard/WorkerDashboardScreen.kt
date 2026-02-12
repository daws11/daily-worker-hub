package com.example.dwhubfix.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.dwhubfix.data.SessionManager
import com.example.dwhubfix.data.SupabaseRepository
import com.example.dwhubfix.model.Booking
import com.example.dwhubfix.model.WorkerStats
import kotlinx.coroutines.launch

// =====================================================
// WORKER DASHBOARD SCREEN
// Main dashboard for workers to view stats, bookings, and earnings
// =====================================================

@Composable
fun WorkerDashboardScreen(
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    // State
    var activeTab by remember { mutableStateOf("overview") }
    var isLoading by remember { mutableStateOf(true) }
    var workerStats by remember { mutableStateOf<WorkerStats?>(null) }
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }

    // Load data
    LaunchedEffect(Unit) {
        isLoading = true
        val userId = SessionManager.getUserId(context)
        if (userId != null) {
            val stats = SupabaseRepository.getWorkerStats(userId)
            workerStats = stats
            val bookingsList = SupabaseRepository.getWorkerBookings(userId, limit = 10)
            bookings = bookingsList
        }
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8F6))
    ) {
        // Top Bar
        TopBar(
            onNavigateBack = onNavigateBack,
            title = "Dashboard",
            onMenuClick = { /* TODO: Open menu */ }
        )

        // Content
        if (isLoading) {
            LoadingIndicator()
        } else {
            when (activeTab) {
                "overview" -> OverviewTab(
                    stats = workerStats ?: WorkerStats(),
                    onRefresh = {
                        isLoading = true
                        scope.launch {
                            val userId = SessionManager.getUserId(context)
                            if (userId != null) {
                                workerStats = SupabaseRepository.getWorkerStats(userId)
                                bookings = SupabaseRepository.getWorkerBookings(userId, limit = 10)
                            }
                            isLoading = false
                        }
                    }
                )
                "bookings" -> BookingsTab(
                    bookings = bookings,
                    onRefresh = {
                        isLoading = true
                        scope.launch {
                            val userId = SessionManager.getUserId(context)
                            if (userId != null) {
                                bookings = SupabaseRepository.getWorkerBookings(userId, limit = 20)
                            }
                            isLoading = false
                        }
                    }
                )
                "earnings" -> EarningsTab(
                    stats = workerStats ?: WorkerStats(),
                    bookings = bookings
                )
                "wallet" -> WalletTab(
                    stats = workerStats ?: WorkerStats(),
                    onRefresh = {
                        isLoading = true
                        scope.launch {
                            val userId = SessionManager.getUserId(context)
                            if (userId != null) {
                                workerStats = SupabaseRepository.getWorkerStats(userId)
                            }
                            isLoading = false
                        }
                    }
                )
            }
        }

        // Bottom Navigation
        BottomNavigation(
            activeTab = activeTab,
            onTabClick = { activeTab = it }
        )
    }
}

// =====================================================
// TOP BAR
// =====================================================
@Composable
private fun TopBar(
    title: String,
    onNavigateBack: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onNavigateBack,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = "Back",
                tint = Color(0xFF2563EB),
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF111827),
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Menu Button
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = Color(0xFF2563EB),
            )
        }
    }
}

// =====================================================
// BOTTOM NAVIGATION
// =====================================================
@Composable
private fun BottomNavigation(
    activeTab: String,
    onTabClick: (String) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .height(64.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Overview Tab
            TabButton(
                icon = Icons.Default.Home,
                label = "Overview",
                isActive = activeTab == "overview",
                onClick = { onTabClick("overview") }
            )

            // Bookings Tab
            TabButton(
                icon = Icons.Default.List,
                label = "Jobs",
                isActive = activeTab == "bookings",
                onClick = { onTabClick("bookings") }
            )

            // Earnings Tab
            TabButton(
                icon = Icons.Default.Money,
                label = "Earnings",
                isActive = activeTab == "earnings",
                onClick = { onTabClick("earnings") }
            )

            // Wallet Tab
            TabButton(
                icon = Icons.Default.Wallet,
                label = "Wallet",
                isActive = activeTab == "wallet",
                onClick = { onTabClick("wallet") }
            )
        }
    }
}

@Composable
private fun TabButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) Color(0xFF2563EB) else Color(0xFF9CA3AF),
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isActive) Color(0xFF2563EB) else Color(0xFF9CA3AF),
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// =====================================================
// OVERVIEW TAB
// =====================================================
@Composable
private fun OverviewTab(
    stats: WorkerStats,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Statistik Saya",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF111827),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Stats Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Shifts Card
            StatCard(
                title = "Total Shift",
                value = "${stats.totalShiftsCompleted}",
                icon = Icons.Default.FrontHand,
                color = Color(0xFF2563EB)
            )

            // Total Earnings Card
            StatCard(
                title = "Total Earnings",
                value = "Rp ${stats.totalEarnings}",
                icon = Icons.Default.Money,
                color = Color(0xFF16A34A)
            )

            // Rating Card
            StatCard(
                title = "Rating",
                value = "${stats.ratingAvg} ?",
                icon = Icons.Default.CalendarMonth,
                color = Color(0xFFCA8A04)
            )

            // Reliability Card
            StatCard(
                title = "Reliabilitas",
                value = "${stats.reliabilityScore}%",
                icon = Icons.Default.CheckCircle,
                color = if (stats.reliabilityScore >= 90) Color(0xFF16A34A) else Color(0xFFCA8A04),
            )
        }

        // Quick Actions
        Text(
            text = "Aksi Cepat",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF111827),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { /* TODO: Find Jobs */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                ),
            ) {
                Text("Cari Kerjaan")
            }

            Button(
                onClick = { /* TODO: My Bookings */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A),
                    contentColor = Color.White
                ),
            ) {
                Text("Pesananku Saya")
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp,
        color = Color.White,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// =====================================================
// BOOKINGS TAB
// =====================================================
@Composable
private fun BookingsTab(
    bookings: List<Booking>,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Riwayat Kerja",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF111827),
            )

            IconButton(
                onClick = onRefresh,
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color(0xFF2563EB),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (bookings.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(bookings, key = { it.id }) { booking ->
                    BookingItem(booking = booking)
                }
            }
        }
    }
}

@Composable
private fun BookingItem(
    booking: Booking
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = booking.shift.jobTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = booking.shift.business.businessName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                    )

                    Text(
                        text = "${booking.shift.date} . ${booking.shift.startTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9CA3AF),
                    )
                }

                // Status Badge
                StatusBadge(
                    status = booking.status.name.lowercase()
                )
            }

            // Action Buttons
            if (booking.status.name == "pending" || booking.status.name == "confirmed") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* TODO: Navigate to shift detail */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A),
                            contentColor = Color.White
                        ),
                    ) {
                        Text("Lihat Shift")
                    }

                    Button(
                        onClick = { /* TODO: Clock in */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                    ) {
                        Text("Clock In")
                    }
                }
            }

            if (booking.status.name == "completed") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text(
                        text = "Pendapatan: Rp ${booking.totalEarnings}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: String
) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "pending" -> Color(0xFFF59E0B) to Color(0xFFFEF3C7)
        "confirmed" -> Color(0xFF2563EB) to Color(0xFFEFF6FF)
        "completed" -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
        "cancelled" -> Color(0xFFEF4444) to Color(0xFFFEE2E2)
        "no_show" -> Color(0xFF9CA3AF) to Color(0xFFFECACA)
        "in_progress" -> Color(0xFFCA8A04) to Color(0xFFFED7AA)
        "clocked_in" -> Color(0xFF16A34A) to Color(0xFFDCFCE7)
        else -> Color(0xFF9CA3AF) to Color(0xFFF3F4F6)
    }

    Surface(
        shape = CircleShape,
        color = backgroundColor,
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

// =====================================================
// EARNINGS TAB
// =====================================================
@Composable
private fun EarningsTab(
    stats: WorkerStats,
    bookings: List<Booking>,
) {
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
    ) {
        // Total Earnings Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 4.dp,
            color = Color(0xFF16A34A),
        ) {
            Column(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 20.dp)
            ) {
                Text(
                    text = "Total Pendapatan",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Rp ${stats.totalEarnings}",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Dari ${stats.totalShiftsCompleted} Shift",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFDCFCE7),
                )
            }
        }

        // Recent Earnings
        Text(
            text = "Pendapatan Terakhir",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF111827),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        val recentEarnings = bookings.filter { it.status.name == "completed" }.take(5)

        if (recentEarnings.isEmpty()) {
            Text(
                text = "Belum ada pendapatan",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B7280),
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(recentEarnings, key = { it.id }) { booking ->
                    EarningsItem(booking = booking)
                }
            }
        }
    }
}

@Composable
private fun EarningsItem(
    booking: Booking
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = booking.shift.date,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = booking.shift.business.businessName,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6B7280),
                )
            }

            Text(
                text = "Rp ${booking.totalEarnings}",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF16A34A),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// =====================================================
// WALLET TAB
// =====================================================
@Composable
private fun WalletTab(
    stats: WorkerStats,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Dompet Saya",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF111827),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Wallet Balance Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(48.dp)
                    )

                    Column {
                        Text(
                            text = "Saldo Tersedia",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280),
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Rp ${stats.walletBalance}",
                            style = MaterialTheme.typography.displaySmall,
                            color = Color(0xFF111827),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { /* TODO: Top Up */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF16A34A),
                            contentColor = Color.White
                        ),
                    ) {
                        Text("Top Up")
                    }

                    Button(
                        onClick = { /* TODO: Cash Out */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                    ) {
                        Text("Cash Out")
                    }
                }
            }
        }

        // Transaction History
        Text(
            text = "Riwayat Transaksi",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF111827),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        // Sample transactions
        val sampleTransactions = listOf(
            "Shift #1 - Waiter Hotel Mulia" to "Rp 150.000",
            "Top Up via GoPay" to "Rp 200.000",
            "Cash Out - Bank BCA" to "Rp 50.000"
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(sampleTransactions, key = { it.first }) { transaction ->
                TransactionItem(transaction)
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Pair<String, String>
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = transaction.first,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF111827),
            )

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// =====================================================
// LOADING INDICATOR
// =====================================================
@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xFF2563EB),
        )
    }
}

// =====================================================
// EMPTY STATE
// =====================================================
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Belum ada riwayat kerja",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF6B7280),
            )

            Text(
                text = "Cari kerjaan yang tersedia",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF9CA3AF),
            )

            Button(
                onClick = { /* TODO: Navigate to job search */ },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White
                ),
            ) {
                Text("Cari Kerjaan")
            }
        }
    }
}
