package com.example.dwhubfix.ui.dashboard.worker

import com.example.dwhubfix.ui.theme.Primary
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.dwhubfix.data.SupabaseRepository
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerHomeScreen(
    onNavigateToDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var jobs by remember { mutableStateOf<List<com.example.dwhubfix.model.Job>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var selectedJob by remember { mutableStateOf<com.example.dwhubfix.model.Job?>(null) }
    var isAccepting by remember { mutableStateOf(false) }

    // Fetch jobs function
    fun fetchJobs() {
        scope.launch {
            isLoading = true
            val result = SupabaseRepository.getAvailableJobs(context)
            result.onSuccess { 
                jobs = it 
                isLoading = false
            }
            result.onFailure {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchJobs()
    }
    
    // Accept Job Logic
    if (showAcceptDialog && selectedJob != null) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            title = { Text("Terima Pekerjaan?") },
            text = { 
                Column {
                    Text("Anda akan melamar untuk posisi:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(selectedJob!!.title, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Komisi platform sebesar 6% akan dipotong dari dompet Anda setelah pekerjaan selesai.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isAccepting = true
                            showAcceptDialog = false
                            val result = SupabaseRepository.acceptJob(context, selectedJob!!.id)
                            result.onSuccess {
                                isAccepting = false
                                snackbarHostState.showSnackbar("Pekerjaan berhasil diterima! Cek menu 'Pekerjaan'.")
                                fetchJobs() // Refresh list
                            }
                            result.onFailure { error ->
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

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF6F8F6))) {
        
        // 1. Background Map (osmdroid)
        AndroidView(
            factory = { context ->
                org.osmdroid.views.MapView(context).apply {
                    setMultiTouchControls(true)
                    // Set a default user agent to avoid getting banned
                    org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName
                    
                    // Set default start point (Bali)
                    controller.setZoom(15.0) // Closer zoom to see marker
                    val startPoint = GeoPoint(-8.5069, 115.2625)
                    controller.setCenter(startPoint)

                    // Add a dummy marker
                    val marker = Marker(this)
                    marker.position = startPoint
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    marker.title = "Warung Bamboo Indah"
                    marker.snippet = "Daily Server - Rp 150rb"
                    overlays.add(marker)
                    
                    invalidate() // Refresh map
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Search & Filter Bar
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha=0.9f),
                            Color.White.copy(alpha=0.0f)
                        )
                    )
                )
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Surface(
                        color = Color.White.copy(alpha=0.8f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Text(
                            "Lokasi: Bali, Indonesia",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                    Text(
                        "Cari Lowongan",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111813)
                        )
                    )
                }
                
                // Filter Button
                Surface(
                    onClick = { /* Filter */ },
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                         Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF111813))
                    }
                }
            }

            // Search Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cari posisi atau area...", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = true, label = "Map")
                FilterChip(selected = false, label = "List")
                FilterChip(selected = false, label = "Server")
                FilterChip(selected = false, label = "Kitchen")
            }
        }
        
        // 3. Bottom Job Card Preview (Floating)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
             if (jobs.isNotEmpty()) {
                 // Show first job for now as preview
                 val job = jobs.first()
                 JobCard(
                     job = job,
                     onAcceptClick = {
                         selectedJob = job
                         showAcceptDialog = true
                     }
                 )
             } else if (isLoading) {
                 // Loading state (optional)
             } else {
                 // Empty state or static preview
                 JobCardPreview() 
             }
        }
        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
        )
    }
}

@Composable
fun JobCard(
    job: com.example.dwhubfix.model.Job,
    onAcceptClick: () -> Unit
) {
    val title = job.title ?: "Job Title"
    val business = job.businessInfo?.businessProfile?.businessName ?: "Business Name"
    val wage = job.wage?.toString() ?: "0"
    val distance = "0.0 km" // Distance calculation would be done separately
    val icon = "💼" // Default icon, could be dynamic based on job category
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.LightGray)
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFFFF7ED),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(icon, fontSize = 24.sp)
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(business, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                        Text(" • ", color = Color.Gray)
                        Text(distance, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Rp ${wage.take(3)}rb", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Primary))
                    Text("/ SHIFT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF)))
                }
            }
            // Buttons and Chips/Tags can be dynamic too but keeping simple for now
            Spacer(modifier = Modifier.height(16.dp))
             Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallTag("📍 Bali")
                SmallTag("⏰ 17:00 - 22:00")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) { Text("Detail", color = Color.Black) }
                Button(
                    onClick = onAcceptClick, 
                    modifier = Modifier.weight(1f), 
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Terima", color = Color.Black) }
            }
        }
    }
}

@Composable
fun FilterChip(selected: Boolean, label: String) {
    Surface(
        color = if (selected) Color(0xFF111813) else Color.White,
        contentColor = if (selected) Color.White else Color(0xFF6B7280),
        shape = CircleShape,
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
        shadowElevation = if (selected) 4.dp else 1.dp
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun JobCardPreview() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.LightGray)
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon Box
                Surface(
                    color = Color(0xFFFFF7ED), // orange-50
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(48.dp)
                ) {
                    // Placeholder for Restaurant icon
                    Box(contentAlignment = Alignment.Center) {
                        Text("🍽️", fontSize = 24.sp)
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Daily Server",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Warung Bamboo Indah",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                        Text(" • ", color = Color.Gray)
                        Text("0.8 km", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Rp 150rb",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Primary)
                    )
                    Text(
                        "/ SHIFT",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallTag("📍 Ubud")
                SmallTag("⏰ 17:00 - 22:00")
                SmallTag("📅 Hari Ini", backgroundColor = Color(0xFFEFF6FF), contentColor = Color(0xFF2563EB))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Detail", color = Color(0xFF111813), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color(0xFF111813)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Terima Job", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SmallTag(text: String, backgroundColor: Color = Color(0xFFF9FAFB), contentColor: Color = Color(0xFF6B7280)) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = contentColor
        )
    }
}

@Preview
@Composable
fun WorkerHomeScreenPreview() {
    WorkerHomeScreen(onNavigateToDetail = {})
}
