package com.example.dwhubfix.ui.dashboard.business

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.dwhubfix.ui.theme.DailyWorkerHubTheme
import com.example.dwhubfix.data.SupabaseRepository
import com.example.dwhubfix.model.BusinessStats
import com.example.dwhubfix.model.RateBaliSuggestion
import com.example.dwhubfix.model.Job
import com.example.dwhubfix.model.formatCurrency
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessHomeScreen(
    onNavigateToPostJob: () -> Unit,
    onNavigateToFindWorker: () -> Unit,
    onNavigateToWallet: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Dashboard Stats State
    var businessStats by remember { mutableStateOf<BusinessStats?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var activeJobs by remember { mutableStateOf<List<Job>>(emptyList()) }
    
    // Fetch Dashboard Data
    fun fetchDashboardData() {
        scope.launch {
            isLoading = true
            
            // 1. Fetch Business Stats
            val statsResult = SupabaseRepository.getBusinessStats(context)
            statsResult.onSuccess { stats ->
                businessStats = stats
            }
            
            // 2. Fetch Active Jobs (business's posted jobs)
            val jobsResult = SupabaseRepository.getBusinessJobs(context)
            jobsResult.onSuccess { jobs ->
                activeJobs = jobs.filter { it.status == "open" }
            }
            
            isLoading = false
        }
    }
    
    // Initial fetch
    LaunchedEffect(Unit) {
        fetchDashboardData()
    }
    
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
            if (businessStats != null && !isLoading) {
                item {
                    StatsCardsRow(
                        activeShiftsToday = businessStats!!.activeShiftsToday,
                        workersHiredThisWeek = businessStats!!.workersHiredThisWeek,
                        totalSpendingThisMonth = businessStats!!.totalSpendingThisMonth,
                        pendingPatches = businessStats!!.pendingPatches
                    )
                }
            }
            
            // 2. Quick Actions
            item {
                QuickActionsSection(onNavigateToPostJob, onNavigateToFindWorker)
            }
            
            // 3. Recent Activity (Active Jobs)
            item {
                if (isLoading) {
                    LoadingStateCard()
                } else if (activeJobs.isNotEmpty()) {
                    RecentJobsSection(
                        jobs = activeJobs.take(5), // Show top 5
                        onViewAll = { /* Navigate to Jobs Screen */ }
                    )
                } else {
                    EmptyRecentActivityCard()
                }
            }
            
            // 4. Rate Bali Suggestion
            if (businessStats != null && businessStats!!.rateBaliSuggestion != null) {
                item {
                    RateBaliSuggestionCard(
                        suggestion = businessStats!!.rateBaliSuggestion!!,
                        onUse = { /* Apply suggestion */ }
                    )
                }
            }
            
            // 5. Wallet Quick View
            if (businessStats != null) {
                item {
                    WalletQuickViewCard(
                        balance = businessStats!!.walletBalance ?: 0.0,
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
            androidx.compose.foundation.Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
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
fun RecentJobsSection(
    jobs: List<Job>,
    onViewAll: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Lowongan Aktif",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            TextButton(onClick = onViewAll) {
                Text("Lihat Semua", color = Color(0xFF13EC5B), fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        jobs.forEach { job ->
            BusinessJobCard(job)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun BusinessJobCard(job: Job) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Job Icon
            Surface(
                color = Color(0xFFFFF7ED),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("💼", fontSize = 24.sp)
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Job Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    job.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                )
                Text(
                    "${job.businessInfo?.businessProfile?.businessName} • ${job.location}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Hari Ini",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                }
            }
            
            // Wage
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Rp ${formatCurrency(job.wage?.toInt() ?: 0)}rb",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF13EC5B))
                )
                Text(
                    "/ shift",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9CA3AF))
                )
            }
        }
    }
}

@Composable
fun RateBaliSuggestionCard(
    suggestion: RateBaliSuggestion,
    onUse: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF13EC5B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                androidx.compose.foundation.Surface(
                    color = Color(0xFF13EC5B).copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        androidx.compose.foundation.Icon(
                            Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = Color(0xFF13EC5B),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Column {
                    Text(
                        "Rekomendasi Gaji",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                    )
                    Text(
                        "Berdasarkan lokasi (${suggestion.region})",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                    Text(
                        "UMK: Rp ${formatCurrency(suggestion.umk.toInt())}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                    )
                    Text(
                        "Rp ${formatCurrency(suggestion.dailyWage.toInt())} / shift",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onUse,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF13EC5B),
                    contentColor = Color.White
                )
            ) {
                Text("Gunakan Gaji Ini", fontWeight = FontWeight.Bold)
            }
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
