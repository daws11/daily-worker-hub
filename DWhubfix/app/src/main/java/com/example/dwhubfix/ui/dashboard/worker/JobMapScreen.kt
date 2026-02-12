package com.example.dwhubfix.ui.dashboard.worker

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.dwhubfix.ui.theme.Primary
import com.example.dwhubfix.domain.model.Job
import com.example.dwhubfix.domain.model.JobMatchScore
import com.example.dwhubfix.domain.model.JobWithScore
import com.example.dwhubfix.utils.calculateDistance
import com.example.dwhubfix.utils.formatDistance
import com.example.dwhubfix.model.formatCurrency
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// Default user location (Bali center)
private val USER_LOCATION = GeoPoint(-8.5069, 115.2625)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobMapScreen(
    jobs: List<JobWithScore>,
    selectedJob: Job?,
    onJobSelected: (Job) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mapView: MapView? by remember { mutableStateOf(null) }
    
    // Map control state
    var isMapLoaded by remember { mutableStateOf(false) }

    // Lifecycle observer for MapView
    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onDetach()
        }
    }

    // Initialize OSM Configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, android.preference.PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Peta Pekerjaan",
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
                    IconButton(
                        onClick = {
                            scope.launch {
                                mapView?.let { map ->
                                    map.controller.animateTo(USER_LOCATION, 15.0, 1000)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.MyLocation, contentDescription = "My Location")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            if (selectedJob != null) {
                // Job Card with Score Display
                JobCardFromMap(
                    job = selectedJob,
                    score = jobs.find { it.job.id == selectedJob.id }?.score,
                    onAccept = { /* TODO */ },
                    onDismiss = { /* Clear selection */ }
                )
            } else {
                // Stats bar
                Surface(
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Work, contentDescription = null, tint = Primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "${jobs.size} Pekerjaan",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    "Dalam radius 20km",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                        Icon(
                            Icons.Default.ExpandLess,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF6F8F6),
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map View
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        
                        controller.setZoom(12.0)
                        controller.setCenter(USER_LOCATION)
                        
                        isMapLoaded = true
                    }.also { map -> mapView = map }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    // Add markers when jobs change
                    view.overlays.removeAll { it is Marker }
                    
                    // Add user location marker
                    val userMarker = Marker(view).apply {
                        position = USER_LOCATION
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Lokasi Anda"
                        setIcon(createJobMarkerIcon(null, 100)) // Primary color for user
                    }
                    view.overlays.add(userMarker)
                    
                    // Add job markers with score
                    jobs.forEach { jobWithScore ->
                        val job = jobWithScore.job
                        val score = jobWithScore.score.totalScore

                        val jobLat = job.businessLatitude
                        val jobLon = job.businessLongitude

                        if (jobLat != null && jobLon != null) {
                            val jobMarker = Marker(view).apply {
                                position = GeoPoint(jobLat, jobLon)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                title = job.title
                                snippet = "${job.businessName} - ${job.wage}"

                                // Icon color based on score
                                val iconColor = when {
                                    score >= 80 -> Color(0xFF059669) // Green
                                    score >= 60 -> Color(0xFF10B981) // Light Green
                                    score >= 40 -> Color(0xFFF59E0B) // Orange
                                    score >= 20 -> Color(0xFF6366F1) // Purple
                                    else -> Color(0xFF9CA3AF) // Gray
                                }

                                setIcon(createJobMarkerIcon(job.category, score.toInt(), iconColor))

                                // Set click listener
                                setOnMarkerClickListener { _, _ ->
                                    onJobSelected(job)
                                    true
                                }
                            }
                            view.overlays.add(jobMarker)
                        }
                    }
                    
                    view.invalidate()
                }
            )
            
            // Zoom controls
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = Color.White,
                    shape = CircleShape,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(
                        onClick = { mapView?.controller?.zoomIn() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In")
                    }
                }
                Surface(
                    color = Color.White,
                    shape = CircleShape,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(40.dp)
                ) {
                    IconButton(
                        onClick = { mapView?.controller?.zoomOut() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                    }
                }
            }
        }
    }
}

@Composable
fun JobCardFromMap(
    job: Job,
    score: JobMatchScore?,
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = job.title
    val business = job.businessName ?: "Business Name"
    val wage = job.wage ?: 0.0
    val matchScore = score?.totalScore ?: 0.0

    // Calculate distance
    val jobLat = job.businessLatitude
    val jobLon = job.businessLongitude
    val distance = if (jobLat != null && jobLon != null) {
        val distKm = calculateDistance(
            USER_LOCATION.latitude,
            USER_LOCATION.longitude,
            jobLat,
            jobLon
        )
        formatDistance(distKm)
    } else {
        "0.0 km"
    }

    Surface(
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Job Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon Box
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
                    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(business, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                        Text(" • ", color = Color.Gray)
                        Text(distance, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    // Match Score Badge (NEW)
                    if (matchScore > 0) {
                        Surface(
                            color = if (matchScore >= 70) Color(0xFFD1FAE5) else Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                "${matchScore.toInt()}% Match",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (matchScore >= 70) Color(0xFF059669) else Color(0xFF7F1D1D),
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        "Rp ${formatCurrency(wage.toInt())}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Primary)
                    )
                    Text(
                        "/ ${job.wageType ?: "shift"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF9CA3AF))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Score Breakdown (NEW)
            if (score != null) {
                Surface(
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Skor Pencocokan",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Jarak", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9CA3AF), modifier = Modifier.weight(1f))
                            Text(score.breakdown.distanceScore.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Skill", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9CA3AF), modifier = Modifier.weight(1f))
                            Text(score.breakdown.skillScore.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rating", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9CA3AF), modifier = Modifier.weight(1f))
                            Text(score.breakdown.ratingScore.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Keandalan", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9CA3AF), modifier = Modifier.weight(1f))
                            Text(score.breakdown.reliabilityScore.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Urgensi", style = MaterialTheme.typography.bodySmall, color = Color(0xFF9CA3AF), modifier = Modifier.weight(1f))
                            Text(score.breakdown.urgencyScore.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SmallTag("📍 Bali")
                SmallTag("⏰ 17:00 - 22:00")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Tutup", color = Color(0xFF6B7280))
                }
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(2f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color(0xFF102216)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Terima", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Helper function to create marker icon based on category, score, and color
fun createJobMarkerIcon(
    category: String?,
    score: Int = 0,
    color: Color = Color(0xFF13ec5b) // Default Primary color
): android.graphics.drawable.BitmapDrawable {
    val size = 64
    val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    // Draw circle background
    val paint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.parseColor(color.toString().replace("Color(", "").replace(")", "").trim())
        isAntiAlias = true
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

    // Draw white border
    val borderPaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.WHITE
        isAntiAlias = true
        strokeWidth = 4f
        style = android.graphics.Paint.Style.STROKE
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 4f, borderPaint)

    // Draw score text in center (if score > 0)
    if (score > 0) {
        val textPaint = android.graphics.Paint().apply {
            this.color = android.graphics.Color.WHITE
            isAntiAlias = true
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
        }
        canvas.drawText(
            score.toString(),
            size / 2f,
            size / 2f,
            textPaint
        )
    }

    return android.graphics.drawable.BitmapDrawable(
        android.content.res.Resources.getSystem(),
        bitmap
    )
}
