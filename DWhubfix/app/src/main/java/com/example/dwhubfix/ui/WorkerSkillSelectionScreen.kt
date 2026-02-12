package com.example.dwhubfix.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dwhubfix.data.SupabaseRepository
import com.example.dwhubfix.data.WorkerCategories
import com.example.dwhubfix.ui.theme.Primary
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerSkillSelectionScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State
    val selectedSkills = remember { mutableStateListOf<String>() }
    // Map skill name to experience level: "Beginner", "Intermediate", "Expert"
    val skillExperienceLevels = remember { mutableStateMapOf<String, String>() }
    
    val maxSkills = 3
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Your Skills",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = Color.Black)
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (selectedSkills.isEmpty()) {
                            Toast.makeText(context, "Please select at least one skill", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        isSaving = true
                        scope.launch {
                             val result = SupabaseRepository.updateWorkerSkills(context, selectedSkills.toList(), skillExperienceLevels.toMap(), currentStep = "worker_experience_level")
                             if (result.isSuccess) {
                                 onNavigateNext()
                             } else {
                                 Toast.makeText(context, "Failed to save skills", Toast.LENGTH_SHORT).show()
                                 isSaving = false
                             }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary // Changed from hardcoded Color(0xFF003314)
                    ),
                    enabled = !isSaving
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp)) // Changed from hardcoded Color.Black
                    } else {
                        Text("Save & Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Page Indicators
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.width(32.dp).height(6.dp).clip(CircleShape).background(Primary))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.width(32.dp).height(6.dp).clip(CircleShape).background(Primary))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.width(8.dp).height(6.dp).clip(CircleShape).background(Color(0xFFDBE6DF)))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.width(8.dp).height(6.dp).clip(CircleShape).background(Color(0xFFDBE6DF)))
            }

            Padding(horizontal = 20.dp) {
                Text(
                    "What are your specialties?",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp)
                )
                Text(
                    "Select up to 3 main skills to get matched with the right jobs.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                // Render Categories
                WorkerCategories.categories.forEach { (category, skills) ->
                    CategorySection(
                        title = category,
                        icon = getIconForCategory(category),
                        skills = skills,
                        selectedSkills = selectedSkills,
                        onSkillSelected = { skill ->
                            if (selectedSkills.contains(skill)) {
                                selectedSkills.remove(skill)
                                skillExperienceLevels.remove(skill)
                            } else {
                                if (selectedSkills.size < maxSkills) {
                                    selectedSkills.add(skill)
                                    skillExperienceLevels[skill] = "Intermediate" // Default
                                } else {
                                    Toast.makeText(context, "You can only select up to 3 skills", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        skillExperienceLevels = skillExperienceLevels,
                        onExperienceChange = { skill, level ->
                            skillExperienceLevels[skill] = level
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            Spacer(modifier = Modifier.height(100.dp)) // Padding for bottom bar
        }
    }
}

@Composable
fun Padding(horizontal: androidx.compose.ui.unit.Dp, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = horizontal), content = content)
}

@Composable
fun CategorySection(
    title: String,
    icon: ImageVector,
    skills: List<String>,
    selectedSkills: List<String>,
    onSkillSelected: (String) -> Unit,
    skillExperienceLevels: Map<String, String>,
    onExperienceChange: (String, String) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            Icon(icon, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
           // FlowRow is experimental, using manual wrap or simple row for MVP? 
           // Since Compose 1.4 FlowRow is available but might need Accompanist if using older BOM.
           // Let's us basic FlowLayout manually or simple Column of Rows if needed.
           // Assuming standard compose setup, let's use a custom simple flow layout or library provided FlowRow.
           // For stability without external libs, I'll use a simple wrapping logic or just Row with horizontal scroll if list is long? 
           // Design shows wrapping.
           
           // Simplified Wrapping Layout
           FlowRowSimple(
               horizontalGap = 8.dp,
               verticalGap = 8.dp,
           ) {
               skills.forEach { skill ->
                   val isSelected = selectedSkills.contains(skill)
                   FilterChip(
                       selected = isSelected,
                       onClick = { onSkillSelected(skill) },
                       label = { Text(skill, fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal) },
                       leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                       } else null,
                       colors = FilterChipDefaults.filterChipColors(
                           selectedContainerColor = MaterialTheme.colorScheme.primary,
                           selectedLabelColor = Color(0xFF102216),
                           selectedLeadingIconColor = Color(0xFF102216)
                       ),
                       border = FilterChipDefaults.filterChipBorder(
                           borderColor = if(isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                           selectedBorderColor = MaterialTheme.colorScheme.primary,
                           enabled = true,
                           selected = isSelected
                       ),
                       shape = RoundedCornerShape(50),
                       enabled = true
                   )
               }
           }
        }

        // Dynamic Experience Cards for Selected Skills in this Category
        skills.filter { selectedSkills.contains(it) }.forEach { skill ->
            AnimatedVisibility(
                visible = true,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                 ExperienceCard(
                     skillName = skill,
                     currentLevel = skillExperienceLevels[skill] ?: "Intermediate",
                     onLevelSelected = { level -> onExperienceChange(skill, level) }
                 )
            }
        }
    }
}

@Composable
fun ExperienceCard(
    skillName: String,
    currentLevel: String,
    onLevelSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Surface
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Experience Level: ",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                 Text(
                    text = skillName,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Beginner", "Intermediate", "Expert").forEach { level ->
                    val isSelected = currentLevel == level
                    ExperienceLevelOption(
                        level = level,
                        isSelected = isSelected,
                        modifier = Modifier.weight(1f),
                        onClick = { onLevelSelected(level) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExperienceLevelOption(
    level: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp, 
                if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray, 
                RoundedCornerShape(12.dp)
            )
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = level,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color(0xFF102216) else Color.Gray
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Progress Bar Visual
        Row(modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Color.LightGray)) {
            val progress = when(level) {
                "Beginner" -> 0.33f
                "Intermediate" -> 0.66f
                "Expert" -> 1.0f
                else -> 0f
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .background(if (isSelected) Color(0xFF102216) else Color.Gray)
            )
        }
    }
}

@Composable
fun FlowRowSimple(
    modifier: Modifier = Modifier,
    horizontalGap: androidx.compose.ui.unit.Dp = 0.dp,
    verticalGap: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)
            if (currentRowWidth + placeable.width > constraints.maxWidth) {
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }
            currentRow.add(placeable)
            currentRowWidth += placeable.width + horizontalGap.roundToPx()
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }

        val totalHeight = rowHeights.sum() + (rows.size - 1) * verticalGap.roundToPx()

        layout(width = constraints.maxWidth, height = totalHeight.coerceAtLeast(0)) {
            var yOffset = 0
            rows.forEachIndexed { index, row ->
                var xOffset = 0
                row.forEach { placeable ->
                    placeable.placeRelative(x = xOffset, y = yOffset)
                    xOffset += placeable.width + horizontalGap.roundToPx()
                }
                yOffset += rowHeights[index] + verticalGap.roundToPx()
            }
        }
    }
}

fun getIconForCategory(category: String): ImageVector {
    return when (category) {
        "Kitchen & Culinary" -> Icons.Default.Kitchen
        "Front of House & Guest Service" -> Icons.Default.Restaurant
        "Housekeeping & Cleaning" -> Icons.Default.CleaningServices
        else -> Icons.Default.Check
    }
}

@Preview(showBackground = true)
@Composable
fun WorkerSkillSelectionScreenPreview() {
    com.example.dwhubfix.ui.theme.DailyWorkerHubTheme {
        WorkerSkillSelectionScreen({}, {})
    }
}
