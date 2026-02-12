package com.example.dwhubfix.ui.dashboard.worker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dwhubfix.ui.theme.Primary
import com.example.dwhubfix.data.WorkerCategories
import com.example.dwhubfix.model.JobFilters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobFilterSheet(
    onDismiss: () -> Unit,
    onApplyFilters: (JobFilters) -> Unit,
    initialFilters: JobFilters = JobFilters()
) {
    val scope = rememberCoroutineScope()
    
    // Filter State
    var selectedCategories by remember { mutableStateOf(initialFilters.categories) }
    var wageRange by remember { mutableStateOf(initialFilters.wageRange) }
    var distanceRange by remember { mutableStateOf(initialFilters.distanceRange) }
    var timeSlot by remember { mutableStateOf(initialFilters.timeSlot) }
    
    // Get all categories from WorkerCategories
    val allCategories = remember {
        WorkerCategories.categories.values.flatten().toSet()
    }
    
    val wageOptions = listOf(
        "Semua",
        "< Rp 100rb",
        "Rp 100rb - 150rb",
        "Rp 150rb - 200rb",
        "> Rp 200rb"
    )
    
    val distanceOptions = listOf(
        "Semua",
        "< 2 km",
        "2 - 5 km",
        "5 - 10 km",
        "> 10 km"
    )
    
    val timeOptions = listOf(
        "Semua",
        "Pagi (06:00 - 12:00)",
        "Siang (12:00 - 17:00)",
        "Sore (17:00 - 21:00)",
        "Malam (21:00 - 06:00)"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFE5E7EB), RoundedCornerShape(2.dp))
                )
            }
            
            // Title & Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filter Pekerjaan",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                TextButton(onClick = onDismiss) {
                    Text("Reset", color = Primary, fontWeight = FontWeight.Bold)
                }
            }
            
            // Filter Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Categories
                FilterSection(
                    title = "Kategori",
                    subtitle = "${selectedCategories.size} dipilih"
                ) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allCategories.size) { index ->
                            val category = allCategories.elementAt(index)
                            FilterChip(
                                selected = selectedCategories.contains(category),
                                onClick = {
                                    if (selectedCategories.contains(category)) {
                                        // Prevent deselecting all
                                        if (selectedCategories.size > 1) {
                                            selectedCategories -= category
                                        }
                                    } else {
                                        // Keep at least one
                                        selectedCategories += category
                                    }
                                },
                                label = { Text(category) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                // Wage Range
                FilterSection(
                    title = "Rentang Gaji",
                    subtitle = wageRange
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        wageOptions.forEach { option ->
                            FilterChip(
                                selected = wageRange == option,
                                onClick = { wageRange = option },
                                label = { Text(option) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                // Distance
                FilterSection(
                    title = "Jarak",
                    subtitle = distanceRange
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        distanceOptions.forEach { option ->
                            FilterChip(
                                selected = distanceRange == option,
                                onClick = { distanceRange = option },
                                label = { Text(option) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                // Time Slot
                FilterSection(
                    title = "Waktu Kerja",
                    subtitle = timeSlot
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        timeOptions.forEach { option ->
                            FilterChip(
                                selected = timeSlot == option,
                                onClick = { timeSlot = option },
                                label = { Text(option) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Bottom Actions
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF6B7280)
                    )
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = {
                        val filters = JobFilters(
                            categories = selectedCategories,
                            wageRange = wageRange,
                            distanceRange = distanceRange,
                            timeSlot = timeSlot
                        )
                        onApplyFilters(filters)
                        onDismiss()
                    },
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color(0xFF102216)
                    )
                ) {
                    Text("Terapkan", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF9CA3AF)
                )
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}
