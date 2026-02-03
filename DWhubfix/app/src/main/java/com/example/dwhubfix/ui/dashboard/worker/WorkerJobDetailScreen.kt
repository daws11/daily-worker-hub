package com.example.dwhubfix.ui.dashboard.worker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dwhubfix.data.SupabaseRepository
import com.example.dwhubfix.model.Job
import com.example.dwhubfix.model.formatCurrency
import com.example.dwhubfix.ui.theme.Primary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerJobDetailScreen(
    jobId: String,
    onNavigateBack: () -> Unit,
    onNavigateToBusiness: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var job by remember { mutableStateOf<Job?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var showAcceptDialog by remember { mutableStateOf(false) }
    var isAccepting by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    // Fetch Job Detail
    LaunchedEffect(jobId) {
        scope.launch {
            val result = SupabaseRepository.getAvailableJobs(context)
            result.onSuccess { jobs ->
                job = jobs.find { it.id == jobId }
                if (job == null) {
                    errorMessage = "Job not found"
                }
                isLoading = false
            }.onFailure { error ->
                errorMessage = error.message
                isLoading = false
            }
        }
    }

    // Accept Job Dialog
    if (showAcceptDialog && job != null) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            title = { Text("Terima Pekerjaan?") },
            text = {
                Column {
                    Text("Anda akan melamar untuk:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        job!!.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Komisi platform sebesar 6% akan dipotong dari dompet Anda setelah pekerjaan selesai.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isAccepting = true
                            showAcceptDialog = false
                            val result = SupabaseRepository.acceptJob(context, job!!.id)
                            result.onSuccess {
                                isAccepting = false
                                snackbarHostState.showSnackbar("Pekerjaan berhasil diterima!")
                                onNavigateBack()
                            }.onFailure { error ->
                                isAccepting = false
                                snackbarHostState.showSnackbar("Gagal menerima pekerjaan: ${error.message}")
                            }
                        }
                    },
                    enabled = !isAccepting
                ) {
                    if (isAccepting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Text("Terima")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAcceptDialog = false }, enabled = !isAccepting) {
                    Text("Batal")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Detail Pekerjaan",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* Favorite */ }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorite")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            if (!isLoading && job != null) {
                Surface(
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Contact Business */ },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Chat")
                            }
                        }
                        Button(
                            onClick = { showAcceptDialog = true },
                            modifier = Modifier
                                .weight(2f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary,
                                contentColor = Color(0xFF102216)
                            )
                        ) {
                            Text("Terima Job", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFF6F8F6)
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            job != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Hero Section with Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Primary)
                    ) {
                        AsyncImage(
                            model = job.businessInfo?.avatarUrl ?: "",
                            contentDescription = "Business Cover",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.6f)
                                        )
                                    )
                                )
                        )
                        
                        // Category Badge
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                job.category ?: "General",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF102216)
                                )
                            )
                        }
                        
                        // Status Badge
                        Surface(
                            color = Color(0xFF4CAF50),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                        ) {
                            Text(
                                job.status.uppercase(),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Title & Wage
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            job.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Rp ${formatCurrency(job.wage?.toInt() ?: 0)}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                )
                                Text(
                                    "/ ${job.wageType ?: "shift"}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF9CA3AF)
                                    )
                                )
                            }
                            Spacer(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(20.dp)
                                    .background(Color(0xFFE5E7EB))
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Full Time",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Business Card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clickable {
                                job.businessId?.let { onNavigateToBusiness(it) }
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar
                            AsyncImage(
                                model = job.businessInfo?.avatarUrl,
                                contentDescription = "Business Avatar",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    job.businessInfo?.businessProfile?.businessName 
                                        ?: job.businessInfo?.fullName 
                                        ?: "Business Name",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        job.businessInfo?.businessProfile?.address 
                                            ?: job.location 
                                            ?: "Location",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF6B7280)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack, // Rotated for right
                                contentDescription = null,
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier
                                    .size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Description
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            "Deskripsi Pekerjaan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Text(
                            job.description ?: "Tidak ada deskripsi",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF4B5563),
                                lineHeight = 20.sp
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Requirements
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            "Persyaratan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        RequirementItem(
                            icon = Icons.Default.Work,
                            text = "Pengalaman minimal 1 tahun di bidang ${job.category ?: "hospitality"}"
                        )
                        RequirementItem(
                            icon = Icons.Default.Shield,
                            text = "Lulus verifikasi identitas"
                        )
                        RequirementItem(
                            icon = Icons.Default.AccessTime,
                            text = "Dapat mulai bekerja segera"
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Location Map Preview
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            "Lokasi Kerja",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFE5E7EB)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Map,
                                        contentDescription = null,
                                        tint = Color(0xFF9CA3AF),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        job.businessInfo?.businessProfile?.address 
                                            ?: job.location 
                                            ?: "Lokasi",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF6B7280)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                                    ) {
                                        Text(
                                            "Buka Peta",
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
                }
            }
        }
    }
}

@Composable
fun RequirementItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(Primary.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF4B5563),
            modifier = Modifier.weight(1f)
        )
    }
}
