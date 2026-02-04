package com.example.dwhubfix.ui.dashboard.business

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.dwhubfix.ui.theme.DailyWorkerHubTheme
import com.example.dwhubfix.data.SupabaseRepository
import com.example.dwhubfix.data.BusinessMatchingRepository
import com.example.dwhubfix.model.Job
import com.example.dwhubfix.model.WorkerCandidate
import com.example.dwhubfix.model.formatCurrency
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessCandidateListScreen(
    jobId: String,
    onNavigateBack: () -> Unit,
    onWorkerSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State
    var job by remember { mutableStateOf<Job?>(null) }
    var candidates by remember { mutableStateOf<List<WorkerCandidate>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var displayedCandidates by remember { mutableStateOf<List<WorkerCandidate>>(emptyList()) }
    
    // Filter State
    var minRating by remember { mutableStateOf(0.0) }
    var maxDistance by remember { mutableStateOf(20.0) }
    var availableOnly by remember { mutableStateOf(true) }
    
    // Fetch Candidates
    fun fetchCandidates() {
        scope.launch {
            isLoading = true
            
            // 1. Fetch Job Details
            val jobResult = SupabaseRepository.getJobById(context, jobId)
            jobResult.onSuccess { jobData ->
                job = jobData
            }
            
            // 2. Fetch Worker Candidates
            val result = BusinessMatchingRepository.getWorkersForJob(
                jobId = jobId
            )

            result.onSuccess { matchingResult ->
                candidates = matchingResult.candidates
                displayedCandidates = matchingResult.candidates
                isLoading = false
            }
            
            result.onFailure { error ->
                isLoading = false
                // Show error
            }
        }
    }
    
    // Apply Filters
    fun applyFilters() {
        displayedCandidates = candidates.filter { candidate ->
            // 1. Search Filter (Name)
            val searchMatch = if (searchQuery.isNotBlank()) {
                candidate.workerName.lowercase().contains(searchQuery.lowercase())
            } else {
                true
            }
            
            // 2. Rating Filter
            val ratingMatch = candidate.rating >= minRating
            
            // 3. Distance Filter
            val distanceMatch = candidate.distanceValue <= maxDistance
            
            // 4. Availability Filter
            val availabilityMatch = if (availableOnly) {
                candidate.availabilityStatus == "Tersedia"
            } else {
                true
            }
            
            searchMatch && ratingMatch && distanceMatch && availabilityMatch
        }
    }
    
    // Initial fetch
    LaunchedEffect(Unit) {
        fetchCandidates()
    }
    
    // Re-apply filters when filter state changes
    LaunchedEffect(searchQuery, minRating, maxDistance, availableOnly) {
        if (!isLoading) {
            applyFilters()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Pilih Worker",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Job Summary Card
            if (job != null) {
                JobSummaryCard(job = job!!)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            FilterChipsRow(
                minRating = minRating,
                onMinRatingChange = { minRating = it },
                maxDistance = maxDistance,
                onMaxDistanceChange = { maxDistance = it },
                availableOnly = availableOnly,
                onAvailableOnlyToggle = { availableOnly = !availableOnly }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Candidates List
            if (isLoading) {
                LoadingCandidatesState()
            } else if (displayedCandidates.isEmpty()) {
                EmptyCandidatesState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(displayedCandidates) { candidate ->
                        BusinessCandidateCard(
                            candidate = candidate,
                            onSelect = {
                                onWorkerSelected(candidate.workerId)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JobSummaryCard(job: Job) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        job.shiftDate ?: "Hari Ini",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                }
            }
            
            // Wage
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Rp ${formatCurrency(job.wage?.toInt() ?: 0)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF13EC5B))
                )
                Text(
                    "/ shift",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
                )
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = Color.Black),
                decorationBox = { innerTextField ->
                    if (query.isEmpty()) {
                        Text("Cari nama worker...", color = Color.Gray, fontSize = 14.sp)
                    }
                    innerTextField()
                }
            )
            if (query.isNotBlank()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    minRating: Double,
    onMinRatingChange: (Double) -> Unit,
    maxDistance: Double,
    onMaxDistanceChange: (Double) -> Unit,
    availableOnly: Boolean,
    onAvailableOnlyToggle: () -> Unit
) {
    val ratingOptions = listOf("Semua", "4.0+", "3.5+", "3.0+")
    val distanceOptions = listOf("Semua", "<5 km", "<10 km", "<20 km")
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Rating Filter
        FilterChip(
            selected = minRating > 0,
            onClick = { onMinRatingChange(if (minRating == 0.0) 4.0 else 0.0) },
            label = { 
                Column {
                    Text("Min Rating", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    if (minRating > 0) Text("$minRating", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6B7280)))
                }
            },
            leadingIcon = {
                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            modifier = Modifier.height(40.dp)
        )
        
        // Distance Filter
        FilterChip(
            selected = maxDistance < 20.0,
            onClick = { 
                val nextDistance = when (maxDistance) {
                    20.0 -> 10.0
                    10.0 -> 5.0
                    5.0 -> 0.0
                    else -> 20.0
                }
                onMaxDistanceChange(nextDistance)
            },
            label = { 
                Column {
                    Text("Max Jarak", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    if (maxDistance < 20.0) Text("< $maxDistance km", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6B7280)))
                }
            },
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            modifier = Modifier.height(40.dp)
        )
        
        // Availability Filter
        FilterChip(
            selected = availableOnly,
            onClick = onAvailableOnlyToggle,
            label = { 
                Column {
                    Text("Tersedia Saja", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    if (!availableOnly) Text("(Semua)", style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6B7280)))
                }
            },
            leadingIcon = {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
            },
            modifier = Modifier.height(40.dp)
        )
    }
}

@Composable
fun BusinessCandidateCard(
    candidate: WorkerCandidate,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar & Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                if (candidate.avatarUrl != null) {
                    androidx.compose.foundation.Image(
                        painter = coil.compose.rememberAsyncImagePainter(model = candidate.avatarUrl),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp).clip(CircleShape)
                    )
                } else {
                    Surface(
                        color = Color(0xFFE5E7EB),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👤", fontSize = 24.sp)
                        }
                    }
                }
                
                // Name & Score
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            candidate.workerName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                        )
                        
                        // Match Score Badge
                        Surface(
                            color = if (candidate.matchScore >= 70) Color(0xFFD1FAE5) else Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "${candidate.matchScore.toInt()}% Match",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (candidate.matchScore >= 70) Color(0xFF059669) else Color(0xFF7F1D1D),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    // Stats
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Text(
                            candidate.rating.toString(),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                        )
                    }
                }
                
                // Select Button
                Button(
                    onClick = onSelect,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF13EC5B),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Skills
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                candidate.skills.take(3).forEach { skill ->
                    Surface(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            skill,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (candidate.skills.size > 3) {
                    Surface(
                        color = Color(0xFFF3F4F6),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "+${candidate.skills.size - 3}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Reliability & Compliance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reliability
                Column {
                    Text(
                        "Keandalan",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6B7280))
                    )
                    val reliabilityColor = if (candidate.reliabilityScore >= 12.0) Color(0xFF10B981) else Color(0xFFEF4444)
                    Text(
                        "${candidate.reliabilityScore.toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = reliabilityColor)
                    )
                }
                
                // Compliance Badge
                if (candidate.isCompliant) {
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats
            Column {
                Text(
                    "Skor Pencocokan",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6B7280))
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Progress Bars for Score Components
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Distance
                    ProgressBarRow("Jarak", candidate.breakdown.distanceScore, 25.0)
                    // Skill
                    ProgressBarRow("Skill", candidate.breakdown.skillScore, 30.0)
                    // Rating
                    ProgressBarRow("Rating", candidate.breakdown.ratingScore, 20.0)
                    // Reliability
                    ProgressBarRow("Keandalan", candidate.breakdown.reliabilityScore, 15.0)
                    // Availability
                    ProgressBarRow("Ketersediaan", candidate.breakdown.availabilityScore, 10.0)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Stats Row: Total Shifts & Hourly Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Total Shift: ${candidate.totalShiftsCompleted}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
                    )
                    Text(
                        candidate.lastActiveDate?.substring(0, 10) ?: "Baru",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
                    )
                }
                
                if (candidate.hourlyRateString != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            candidate.hourlyRateString!!,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
                        )
                        Text(
                            "/ jam",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF9CA3AF))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressBarRow(label: String, score: Double, maxScore: Double) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280))
            )
            Text(
                "$score / $maxScore",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6B7280))
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Progress Bar
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = Color(0xFFE5E7EB),
            shape = RoundedCornerShape(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(((score / maxScore) * 100).toInt().dp)
                    .fillMaxHeight()
                    .background(Color(0xFF13EC5B), RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun LoadingCandidatesState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = Color(0xFF13EC5B)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Memuat kandidat...",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF6B7280))
        )
    }
}

@Composable
fun EmptyCandidatesState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.GroupOff,
            contentDescription = null,
            tint = Color(0xFF9CA3AF),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Tidak ada kandidat",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813))
        )
        Text(
            "Coba ubah filter atau search",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280)),
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun BusinessCandidateListScreenPreview() {
    DailyWorkerHubTheme {
        BusinessCandidateListScreen(
            jobId = "123",
            onNavigateBack = {},
            onWorkerSelected = {}
        )
    }
}
