package com.example.dwhubfix.ui.dashboard.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Load Profile State
    var isLoading by remember { mutableStateOf(true) }
    
    // Notification Preferences State
    var pushEnabled by remember { mutableStateOf(true) }
    var jobAlertsEnabled by remember { mutableStateOf(true) }
    var applicationUpdatesEnabled by remember { mutableStateOf(true) }
    var promotionalEnabled by remember { mutableStateOf(false) }
    
    // Job Alert Options
    var alertDistance by remember { mutableStateOf("10 km") }
    var alertCategories by remember { mutableStateOf(setOf("Semua")) }
    
    var isSaving by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    val distanceOptions = listOf("5 km", "10 km", "20 km", "50 km")
    val categoryOptions = listOf(
        "Semua", "Kitchen", "Server", "Housekeeping", 
        "Driver", "Security", "Catering", "Other"
    )

    // Load Notification Preferences from Supabase
    LaunchedEffect(Unit) {
        isLoading = true
        val result = SupabaseRepository.getProfile(context)
        result.onSuccess { profile ->
            profile.notificationPreferences?.let { prefs ->
                pushEnabled = prefs.pushEnabled
                jobAlertsEnabled = prefs.jobAlertsEnabled
                applicationUpdatesEnabled = prefs.applicationUpdatesEnabled
                promotionalEnabled = prefs.promotionalEnabled
                alertDistance = prefs.alertDistance
                alertCategories = prefs.alertCategories.toSet()
            }
            isLoading = false
        }.onFailure {
            isLoading = false
            // Use default values if failed to load
        }
    }

    // Auto-save when preferences change
    LaunchedEffect(pushEnabled, jobAlertsEnabled, applicationUpdatesEnabled, promotionalEnabled, alertDistance, alertCategories) {
        if (!isLoading) {
            isSaving = true
            val result = SupabaseRepository.updateNotificationPreferences(
                context = context,
                pushEnabled = pushEnabled,
                jobAlertsEnabled = jobAlertsEnabled,
                applicationUpdatesEnabled = applicationUpdatesEnabled,
                promotionalEnabled = promotionalEnabled,
                alertDistance = alertDistance,
                alertCategories = alertCategories.toList()
            )
            isSaving = false
            result.onFailure { error ->
                errorMessage = error.message
            }
        }
    }

    // Show Success Message
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            successMessage = null
        }
    }
    
    // Show Error Message
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifikasi",
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
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 16.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF6F8F6)
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Push Notifications
            Text(
                "Notifikasi Push",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column {
                    NotificationToggleItem(
                        icon = Icons.Default.Notifications,
                        title = "Notifikasi Push",
                        description = "Terima notifikasi langsung ke perangkat Anda",
                        checked = pushEnabled,
                        onCheckedChange = { pushEnabled = it }
                    )
                    
                    HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 16.dp))
                    
                    NotificationToggleItem(
                        icon = Icons.Default.Work,
                        title = "Alert Pekerjaan Baru",
                        description = "Notifikasi saat ada job yang cocok",
                        checked = jobAlertsEnabled,
                        onCheckedChange = { jobAlertsEnabled = it }
                    )
                    
                    HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 16.dp))
                    
                    NotificationToggleItem(
                        icon = Icons.Default.CheckCircle,
                        title = "Update Lamaran",
                        description = "Notifikasi status lamaran (diterima/ditolak)",
                        checked = applicationUpdatesEnabled,
                        onCheckedChange = { applicationUpdatesEnabled = it }
                    )
                    
                    HorizontalDivider(color = Color(0xFFF3F4F6), modifier = Modifier.padding(horizontal = 16.dp))
                    
                    NotificationToggleItem(
                        icon = Icons.Default.Campaign,
                        title = "Promosi & Tips",
                        description = "Tips karir dan promo dari Daily Worker Hub",
                        checked = promotionalEnabled,
                        onCheckedChange = { promotionalEnabled = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Job Alert Preferences
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Preferensi Alert Pekerjaan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (!jobAlertsEnabled) {
                        Surface(
                            color = Color(0xFFF3F4F6),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Dimatikan",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color(0xFFE5E7EB))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Distance
                    Text(
                        "Jarak Maksimum",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280)
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        distanceOptions.forEach { distance ->
                            androidx.compose.material3.FilterChip(
                                selected = alertDistance == distance,
                                onClick = { alertDistance = distance },
                                label = { Text(distance) },
                                modifier = Modifier.weight(1f),
                                enabled = jobAlertsEnabled
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Categories
                    Text(
                        "Kategori Pekerjaan",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B7280)
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categoryOptions.size) { index ->
                            val category = categoryOptions[index]
                            val isSelected = alertCategories.contains(category)
                            
                            androidx.compose.material3.FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    if (isSelected) {
                                        alertCategories -= category
                                        // Prevent deselecting all
                                        if (alertCategories.isEmpty()) {
                                            alertCategories = setOf(category)
                                        }
                                    } else {
                                        // Clear "Semua" if selecting a specific category
                                        if (category != "Semua") {
                                            alertCategories -= "Semua"
                                        }
                                        alertCategories += category
                                    }
                                },
                                label = { Text(category) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = jobAlertsEnabled,
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Email Notifications Info
            Surface(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Email Notification",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF1E40AF)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Anda akan tetap menerima email penting seperti notifikasi pembayaran dan konfirmasi job meskipun notifikasi push dimatikan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF637588),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun NotificationToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF6B7280),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9CA3AF)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = Color(0xFF102216)
            )
        )
    }
}
