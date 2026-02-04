package com.example.dwhubfix.ui.dashboard.business

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dwhubfix.ui.theme.DailyWorkerHubTheme
import com.example.dwhubfix.model.Job
import com.example.dwhubfix.model.formatCurrency
import com.example.dwhubfix.data.SupabaseRepository
import kotlinx.coroutines.launch

/**
 * BUSINESS JOB DETAIL SCREEN
 * 
 * Shows complete job details for business:
 * - Job title, description, wage, location
 * - Shift date & time
 * - Required skills & category
 * - Business profile info
 * - Status indicators
 * 
 * Based on business-model.md:
 * - Business can post jobs
 * - Business can see workers (candidates)
 * - Business can hire workers
 * - Platform Commission (6%)
 * 
 * Based on matching-algorithm.md:
 * - Worker candidates with scores
 * - Compliance checking (21 Days Rule)
 * - Match prioritization
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessJobDetailScreen(
    jobId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCandidateList: (String) -> Unit, // Navigate to candidate list
    onWorkerHired: (String) -> Unit // Callback when worker is hired
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State
    var job by remember { mutableStateOf<Job?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    
    // Fetch Job Details
    fun fetchJobDetails() {
        scope.launch {
            isLoading = true
            
            val result = SupabaseRepository.getJobById(context, jobId)
            
            result.onSuccess { jobData ->
                job = jobData
                isLoading = false
            }
            
            result.onFailure { error ->
                isLoading = false
                // Show error
            }
        }
    }
    
    // Initial fetch
    LaunchedEffect(jobId) {
        fetchJobDetails()
    }
    
    // Delete Job
    fun deleteJob() {
        scope.launch {
            isDeleting = true
            
            val result = SupabaseRepository.deleteJob(context, jobId)
            
            result.onSuccess {
                isDeleting = false
                showDeleteDialog = false
                onNavigateBack() // Navigate back after delete
            }
            
            result.onFailure { error ->
                isDeleting = false
                // Show error
            }
        }
    }
    
    // Delete Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Lowongan?") },
            text = { 
                Text("Apakah Anda yakin ingin menghapus lowongan ini? Tindakan ini tidak dapat dibatalkan.")
            },
            confirmButton = {
                Button(
                    onClick = { deleteJob() },
                    enabled = !isDeleting
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Hapus", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }, enabled = !isDeleting) {
                    Text("Batal")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Detail Pekerjaan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus Job")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF9FAFB),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                LoadingState()
            } else if (job != null) {
                JobDetailsSection(job = job!!, onNavigateToCandidateList = onNavigateToCandidateList)
            } else {
                ErrorState()
            }
        }
    }
}

@Composable
fun JobDetailsSection(
    job: Job,
    onNavigateToCandidateList: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 36.dp)
    ) {
        // Job Title & Wage Card
        JobTitleWageCard(job = job)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Job Description Card
        JobDescriptionCard(job = job)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Job Time & Location Card
        JobTimeLocationCard(job = job)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Requirements Card
        JobRequirementsCard(job = job)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Business Profile Card
        BusinessProfileCard(job = job)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Select Worker Button (NEW - FASE 4 INTEGRATION)
        if (job.status == "open") {
            Button(
                onClick = { onNavigateToCandidateList(job.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF13EC5B),
                    contentColor = Color(0xFF111813)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PersonSearch,
                        contentDescription = null,
                        tint = Color(0xFF111813),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Pilih Worker",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color(0xFF111813),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else if (job.status == "accepted") {
            // Show Worker Info if job is accepted (worker already hired)
            WorkerHiredCard(job = job)
        }
    }
}

@Composable
fun JobTitleWageCard(job: Job) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        job.title,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                    )
                    Text(
                        job.category ?: "Kategori Tidak Diketahui",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
                    )
                }
                
                // Wage
                Surface(
                    color = Color(0xFFFFF7ED),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = Color(0xFF13EC5B),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Rp ${formatCurrency(job.wage?.toInt() ?: 0)}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111813)
                            )
                        )
                    }
                }
            }
            
            Divider()
            
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = Icons.Default.Schedule,
                    label = "Shift",
                    value = job.shiftDate?.substring(0, 10) ?: "Hari Ini"
                )
                StatItem(
                    icon = Icons.Default.Schedule,
                    label = "Jam",
                    value = "${job.startTime ?: "08:00"} - ${job.endTime ?: "17:00"}"
                )
                StatItem(
                    icon = Icons.Default.Group,
                    label = "Worker",
                    value = job.workerCount ?: 1
                )
                StatItem(
                    icon = Icons.Default.LocationOn,
                    label = "Lokasi",
                    value = job.location ?: "Bali, Indonesia"
                )
            }
        }
    }
}

@Composable
fun JobDescriptionCard(job: Job) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Deskripsi Pekerjaan",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                job.description ?: "Tidak ada deskripsi pekerjaan.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF374151))
            )
        }
    }
}

@Composable
fun JobTimeLocationCard(job: Job) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Waktu & Lokasi",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color(0xFF13EC5B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Waktu Shift",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                    Text(
                        "${job.startTime ?: "08:00"} - ${job.endTime ?: "17:00"} (${job.shiftDate?.substring(0, 10) ?: "Hari Ini"})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Color(0xFF111813))
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Location
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFF13EC5B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Lokasi Kerja",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                    Text(
                        job.location ?: "Bali, Indonesia",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Color(0xFF111813))
                    )
                }
            }
        }
    }
}

@Composable
fun JobRequirementsCard(job: Job) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Persyaratan Pekerja",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Category
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    tint = Color(0xFF13EC5B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Kategori",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                    Text(
                        job.category ?: "Kategori Tidak Diketahui",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Color(0xFF111813))
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Wage Type
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.MonetizationOn,
                    contentDescription = null,
                    tint = Color(0xFF13EC5B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        "Tipe Gaji",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                    Text(
                        job.wageType ?: "Per Shift",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Color(0xFF111813))
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Is Urgent
            if (job.isUrgent) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PriorityHigh,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Urgensi",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                        )
                        Text(
                            "Job Mendesak",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessProfileCard(job: Job) {
    val businessProfile = job.businessInfo?.businessProfile
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Informasi Bisnis",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Business Name
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Business,
                    contentDescription = null,
                    tint = Color(0xFF13EC5B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    businessProfile?.businessName ?: job.businessInfo?.fullName ?: "Bisnis Tidak Diketahui",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = Color(0xFF111813))
                )
            }
        }
    }
}

@Composable
fun WorkerHiredCard(job: Job) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF10B981)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Worker Telah Dipilih",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                        )
                        Text(
                            "Job status: ${job.status}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF10B981))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(20.dp)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Color(0xFF111813))
        )
    }
}

@Composable
fun StatItem(
    icon: ImageVector,
    label: String,
    value: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(20.dp)
        )
        Text(
            value.toString(),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, color = Color(0xFF111813))
        )
    }
}

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xFF13EC5B)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Memuat detail...",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
        )
    }
}

@Composable
fun ErrorState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Gagal memuat detail",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFEF4444))
        )
    }
}

@Preview
@Composable
fun BusinessJobDetailScreenPreview() {
    DailyWorkerHubTheme {
        BusinessJobDetailScreen(
            jobId = "123",
            onNavigateBack = {},
            onNavigateToCandidateList = {},
            onWorkerHired = {}
        )
    }
}
