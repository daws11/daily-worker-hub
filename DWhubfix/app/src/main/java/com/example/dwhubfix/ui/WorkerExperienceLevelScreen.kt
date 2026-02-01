package com.example.dwhubfix.ui

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dwhubfix.data.SupabaseRepository
import com.example.dwhubfix.ui.theme.Primary
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerExperienceLevelScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State
    var experienceYears by remember { mutableStateOf<String?>(null) }
    var workHistory by remember { mutableStateOf("") }
    var uploadedFileUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uploadedFileUri = uri
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Pengalaman Kerja",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
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
                        if (experienceYears == null) {
                             Toast.makeText(context, "Please select years of experience", Toast.LENGTH_SHORT).show()
                             return@Button
                        }
                        
                        isSaving = true
                        scope.launch {
                             // Upload file if exists
                             var documentUrl: String? = null
                             if (uploadedFileUri != null) {
                                 val uploadResult = SupabaseRepository.uploadFile(context, uploadedFileUri!!, "portfolio") // reuse uploadFile
                                 if (uploadResult.isSuccess) {
                                     documentUrl = uploadResult.getOrNull()
                                 } else {
                                     Toast.makeText(context, "Failed to upload document", Toast.LENGTH_SHORT).show()
                                     isSaving = false
                                     return@launch
                                 }
                             }
                        
                             val result = SupabaseRepository.updateWorkerExperience(
                                 context, 
                                 experienceYears!!,
                                 workHistory,
                                 documentUrl,
                                 currentStep = "worker_portfolio_upload"
                             )
                             
                             if (result.isSuccess) {
                                 onNavigateNext()
                             } else {
                                 Toast.makeText(context, "Failed to update experience", Toast.LENGTH_SHORT).show()
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
                        contentColor = Color(0xFF003314)
                    ),
                    enabled = !isSaving
                ) {
                     if (isSaving) {
                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Simpan & Lanjutkan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                .padding(horizontal = 20.dp)
        ) {
            // Progress Indicator
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFE2E8E4))) {
                 Box(modifier = Modifier.fillMaxWidth(0.66f).fillMaxHeight().background(Primary))
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Ceritakan keahlianmu",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            )
             Text(
                "Bantu bisnis mengenal keahlian dan pengalaman kerja anda di industri hospitality.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
            
            // Years Experience
            Text("TAHUN PENGALAMAN", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("< 1 Tahun", "1-3 Tahun", "3-5 Tahun", "> 5 Tahun").forEach { option ->
                    val isSelected = experienceYears == option
                    Box(
                        modifier = Modifier
                            .weight(1f) // Distribute evenly or use scrollable row? Design uses scrollable but weight is fine for 4 items
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(1.dp, if(isSelected) MaterialTheme.colorScheme.primary else Color.LightGray, RoundedCornerShape(24.dp))
                            .background(if(isSelected) MaterialTheme.colorScheme.primary else Color.White)
                            .clickable { experienceYears = option },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            option, 
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if(isSelected) Color(0xFF102216) else Color.Black
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
            
            // Work History
            Spacer(modifier = Modifier.height(24.dp))
            Text("RIWAYAT KERJA SINGKAT", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = workHistory,
                    onValueChange = { workHistory = it },
                    placeholder = { Text("Contoh: Saya pernah bekerja sebagai bartender di Beach Club selama 2 tahun...") },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.LightGray
                    )
                )
                 Icon(
                    Icons.Default.EditNote, 
                    contentDescription = null, 
                    tint = Color.Gray,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)
                )
            }
            
            // Portfolio Upload (Optional)
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Portfolio / Sertifikat", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                 Spacer(modifier = Modifier.weight(1f))
                 Text(
                    "Opsional", 
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray), 
                    modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(2.dp, Color.LightGray, RoundedCornerShape(12.dp)) // Dashed border simulated
                    .clickable { fileLauncher.launch("*/*") }
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF8FAFC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (uploadedFileUri != null) "File Selected" else "Upload Foto atau PDF", 
                        fontWeight = FontWeight.Medium
                    )
                     Text("Maks 5MB (JPG, PNG, PDF)", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                 }
            }
             Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkerExperienceLevelScreenPreview() {
    com.example.dwhubfix.ui.theme.DailyWorkerHubTheme {
        WorkerExperienceLevelScreen({}, {})
    }
}
