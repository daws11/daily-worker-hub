package com.example.dwhubfix.ui.dashboard.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import org.osmdroid.config.Configuration
import com.example.dwhubfix.model.JobFilters
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.example.dwhubfix.ui.theme.Primary
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.example.dwhubfix.data.SupabaseRepository
import com.example.dwhubfix.data.MatchingRepository
import com.example.dwhubfix.model.JobWithScore
import com.example.dwhubfix.model.formatCurrency
import kotlin.math.*

// Default user location (Bali center)
private val USER_LOCATION = GeoPoint(-8.5069, 115.2625)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerHomeScreen(
    onNavigateToDetail: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Job State
    var jobs by remember { mutableStateOf<List<JobWithScore>>(emptyList()) }
    var displayedJobs by remember { mutableStateOf<List<JobWithScore>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Filter & Search State
    var showFilterSheet by remember { mutableStateOf(false) }
    var showMap by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // UI State
    val snackbarHostState = remember { SnackbarHostState() }
    var showAcceptDialog by remember { mutableStateOf(false) }
    var selectedJob by remember { mutableStateOf<com.example.dwhubfix.model.Job?>(null) }
    var isAccepting by remember { mutableStateOf(false) }
    
    // Map selected job (for map view)
    var mapSelectedJob by remember { mutableStateOf<com.example.dwhubfix.model.Job?>(null) }
    
    // Scroll State
    val scrollState = rememberScrollState()

    // Fetch jobs function (Updated to use Smart Matching)
    fun fetchJobs() {
        scope.launch {
            isLoading = true
            
            // Get worker location (for now use USER_LOCATION constant)
            // TODO: Get real GPS location
            
            val result = MatchingRepository.getJobsForWorker(
                context = context,
                workerLocation = USER_LOCATION
            )
            
            result.onSuccess { jobList ->
                jobs = jobList
                displayedJobs = jobList // Smart matching already returns prioritized jobs
                isLoading = false
            }
            result.onFailure { error ->
                isLoading = false
                snackbarHostState.showSnackbar("Gagal memuat jobs: ${error.message}")
            }
        }
    }
    
    // Initial fetch
    LaunchedEffect(Unit) {
        fetchJobs()
    }
    
    // Accept Job Dialog
    if (showAcceptDialog && selectedJob != null) {
        AlertDialog(
            onDismissRequest = { showAcceptDialog = false },
            title = { Text("Terima Pekerjaan?") },
            text = { 
                Column {
                    Text("Anda akan melamar untuk:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        selectedJob!!.title,
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
                            val result = SupabaseRepository.acceptJob(context, selectedJob!!.id)
                            result.onSuccess {
                                isAccepting = false
                                snackbarHostState.showSnackbar("Pekerjaan berhasil diterima!")
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

    // Filter Sheet
    if (showFilterSheet) {
        JobFilterSheet(
            onDismiss = { showFilterSheet = false },
            onApplyFilters = { filters ->
                // Note: Filtering is now handled by backend (MatchingRepository)
                // This filter sheet is for UI preferences only (categories, distance, etc.)
                // We re-fetch jobs when filters are applied
                searchQuery = "" // Reset search
                fetchJobs() // Re-fetch with new preferences
            },
            initialFilters = JobFilters()
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF6F8F6))) {
        if (showMap) {
            // Map View
            JobMapScreen(
                jobs = displayedJobs,
                selectedJob = mapSelectedJob,
                onJobSelected = { job ->
                    mapSelectedJob = job
                },
                onNavigateBack = { showMap = false }
            )
        } else {
            // List View
            // 1. Background Map (osmdroid)
            AndroidView(
                factory = { context ->
                    org.osmdroid.views.MapView(context).apply {
                        setMultiTouchControls(true)
                        // Set a default user agent to avoid getting banned
                        org.osmdroid.config.Configuration.getInstance().userAgentValue = context.packageName
                        
                        // Set default start point (Bali)
                        controller.setZoom(15.0) // Closer zoom to see marker
                        val startPoint = USER_LOCATION
                        controller.setCenter(startPoint)

                        // Add markers for jobs (simplified)
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
                        onClick = { showFilterSheet = true },
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
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                color = Color.Black
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            placeholder = {
                                Text(
                                    "Cari posisi atau area...",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        )
                        if (searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips (Visual only - actual filtering is in backend)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // View Toggle (List/Map)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        Surface(
                            onClick = { showMap = false },
                            shape = RoundedCornerShape(6.dp),
                            color = if (!showMap) Color(0xFF111813) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.List, contentDescription = null, tint = if (!showMap) Color.White else Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                                Text("List", color = if (!showMap) Color.White else Color(0xFF6B7280), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                        Surface(
                            onClick = { showMap = true },
                            shape = RoundedCornerShape(6.dp),
                            color = if (showMap) Color(0xFF111813) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, tint = if (showMap) Color.White else Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                                Text("Peta", color = if (showMap) Color.White else Color(0xFF6B7280), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Main Filter Button
                    FilterChip(
                        selected = false,
                        onClick = { showFilterSheet = true },
                        label = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Filter")
                            }
                        },
                        leadingIcon = { 
                            Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                    
                    // Reset Button
                    FilterChip(
                        selected = false,
                        onClick = { 
                            searchQuery = ""
                            fetchJobs()
                        },
                        label = { Text("Reset") }
                    )
                }
            }
            
            // 3. Bottom Job Card Preview (Floating)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                 if (displayedJobs.isNotEmpty()) {
                     // Show first job for now as preview
                     val jobWithScore = displayedJobs.first()
                     JobCard(
                         job = jobWithScore.job,
                         score = jobWithScore.score,
                         isCompliant = jobWithScore.job.isCompliant ?: true,
                         onAcceptClick = {
                             selectedJob = jobWithScore.job
                             showAcceptDialog = true
                         }
                     )
                 } else if (isLoading) {
                     // Loading state
                     Card(
                         colors = CardDefaults.cardColors(containerColor = Color.White),
                         elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                         shape = RoundedCornerShape(16.dp),
                         modifier = Modifier.fillMaxWidth()
                     ) {
                         Column(
                             modifier = Modifier.padding(16.dp),
                             horizontalAlignment = Alignment.CenterHorizontally
                         ) {
                             CircularProgressIndicator(color = Primary)
                             Spacer(modifier = Modifier.height(12.dp))
                             Text("Memuat pekerjaan...", style = MaterialTheme.typography.bodySmall)
                         }
                     }
                 } else if (jobs.isEmpty()) {
                     // Empty state - no jobs at all
                     JobCardPreview()
                 } else {
                     // No jobs match filters
                     Card(
                         colors = CardDefaults.cardColors(containerColor = Color.White),
                         elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                         shape = RoundedCornerShape(16.dp),
                         modifier = Modifier.fillMaxWidth()
                     ) {
                         Column(
                             modifier = Modifier.padding(24.dp),
                             horizontalAlignment = Alignment.CenterHorizontally
                         ) {
                             Icon(
                                 Icons.Default.WorkOff,
                                 contentDescription = null,
                                 tint = Color.Gray,
                                 modifier = Modifier.size(48.dp)
                             )
                             Spacer(modifier = Modifier.height(12.dp))
                             Text(
                                 "Tidak ada pekerjaan yang cocok",
                                 style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                             )
                             Spacer(modifier = Modifier.height(4.dp))
                             Text(
                                 "Coba ubah filter atau kata kunci pencarian",
                                 style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                             )
                         }
                     }
                 }
            }
            // End of List View
        }
        // End of if (showMap)
        
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
    score: com.example.dwhubfix.model.JobMatchScore,
    isCompliant: Boolean,
    onAcceptClick: () -> Unit
) {
    val title = job.title ?: "Job Title"
    val business = job.businessInfo?.businessProfile?.businessName ?: job.businessInfo?.fullName ?: "Business Name"
    val wage = job.wage ?: 0.0
    
    // Calculate distance
    val jobLat = job.businessInfo?.businessProfile?.latitude
    val jobLon = job.businessInfo?.businessProfile?.longitude
    val distance = if (jobLat != null && jobLon != null) {
        val distKm = com.example.dwhubfix.utils.calculateDistance(
            USER_LOCATION.latitude,
            USER_LOCATION.longitude,
            jobLat,
            jobLon
        )
        com.example.dwhubfix.utils.formatDistance(distKm)
    } else {
        "0.0 km"
    }
    
    val icon = "💼" // Default icon, could be dynamic based on job category
    
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
                    
                    // Compliance Badge
                    Spacer(modifier = Modifier.height(4.dp))
                    if (isCompliant) {
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "✅ Compliant",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "⚠️ Non-Compliant",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFFDC2626),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Rp ${formatCurrency(wage.toInt())}rb",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Primary)
                    )
                    // Match Score Badge
                    Surface(
                        color = if (score.score >= 70) Color(0xFFD1FAE5) else Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "${score.score.toInt()}% Match",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (score.score >= 70) Color(0xFF059669) else Color(0xFF7F1D1D),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        "/ ${job.wageType ?: "shift"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                    )
                }
            }
            
            // Chips (location, time)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallTag("📍 Bali")
                SmallTag("⏰ 17:00 - 22:00")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Detail", color = Color(0xFF111813), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAcceptClick, 
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
            
            // Action Buttons
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
fun FilterChip(
    selected: Boolean,
    label: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) Color(0xFF111813) else Color.White,
        contentColor = if (selected) Color.White else Color(0xFF6B7280),
        shape = CircleShape,
        border = if (!selected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)) else null,
        shadowElevation = if (selected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.invoke()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun SmallTag(
    text: String,
    backgroundColor: Color = Color(0xFFF9FAFB),
    contentColor: Color = Color(0xFF6B7280)
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            text,
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
