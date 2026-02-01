package com.example.dwhubfix.ui.dashboard.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dwhubfix.data.SupabaseRepository
import com.example.dwhubfix.model.JobApplication
import com.example.dwhubfix.model.formatCurrency
import com.example.dwhubfix.model.getStatusText
import kotlinx.coroutines.launch

@Composable
fun MyJobsScreen(
    onNavigateToDetail: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Aktif", "Menunggu", "Selesai")

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                )
            }
        }

        when (selectedTab) {
            0 -> JobApplicationList(
                statuses = listOf("accepted", "ongoing", "interview"),
                onJobClick = onNavigateToDetail
            )
            1 -> JobApplicationList(
                statuses = listOf("pending"),
                onJobClick = onNavigateToDetail
            )
            2 -> JobApplicationList(
                statuses = listOf("completed", "rated", "rejected", "cancelled"),
                onJobClick = onNavigateToDetail
            )
        }
    }
}

@Composable
fun JobApplicationList(
    statuses: List<String>,
    onJobClick: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var jobs by remember { mutableStateOf<List<JobApplication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(statuses) {
        isLoading = true
        errorMessage = null
        try {
            val result = SupabaseRepository.getMyJobs(context, *statuses.toTypedArray())
            result.onSuccess {
                jobs = it
            }.onFailure {
                errorMessage = it.message ?: "Gagal memuat data"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Terjadi kesalahan"
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorMessage != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* Retry logic could be added here involving a refresh key */ }) {
                    Text("Coba Lagi")
                }
            }
        } else if (jobs.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = "Empty",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Belum ada pekerjaan di sini",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(jobs) { application ->
                    JobApplicationCard(application = application, onClick = {
                        application.job?.let { job -> onJobClick(job.id) }
                    })
                }
            }
        }
    }
}

@Composable
fun JobApplicationCard(
    application: JobApplication,
    onClick: () -> Unit
) {
    val job = application.job ?: return
    val businessProfile = job.businessInfo?.businessProfile
    val businessName = businessProfile?.businessName ?: job.businessInfo?.fullName ?: "Unknown Business"
    
    // Determine status color
    val statusColor = when (application.status) {
        "accepted", "ongoing", "completed", "rated" -> Color(0xFF4CAF50) // Green
        "pending", "interview" -> Color(0xFFFF9800) // Orange
        "rejected", "cancelled" -> Color(0xFFF44336) // Red
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Business Info & Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (job.businessInfo?.avatarUrl != null) {
                    AsyncImage(
                        model = job.businessInfo.avatarUrl,
                        contentDescription = "Business Avatar",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Business",
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                            .padding(6.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = businessName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = application.createdAt?.take(10) ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                SuggestionChip(
                    onClick = { },
                    label = { 
                        Text(
                            text = application.getStatusText(),
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = statusColor.copy(alpha = 0.1f),
                        labelColor = statusColor
                    ),
                    border = null
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Job Title and Wage
            Text(
                text = job.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Location",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = job.location ?: "Lokasi tidak tersedia",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = "Wage",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                // Convert wage Double to Int for formatter if it's integer-like, or allow formatter to take Double?
                // formatCurrency in EarningsSummary.kt takes Int.
                val wageInt = job.wage?.toInt() ?: 0
                val wageText = formatCurrency(wageInt)
                val wageType = if (job.wageType != null) "/ ${job.wageType}" else ""
                
                Text(
                    text = "$wageText $wageType",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
