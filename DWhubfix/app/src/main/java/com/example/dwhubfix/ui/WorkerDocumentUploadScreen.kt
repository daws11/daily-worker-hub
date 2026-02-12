package com.example.dwhubfix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.PermIdentity
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dwhubfix.ui.theme.DailyWorkerHubTheme
import com.example.dwhubfix.ui.theme.Primary

import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerDocumentUploadScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    var idCardFrontUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selfieUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val launcherIdCard = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        idCardFrontUri = uri
    }
    
    val launcherSelfie = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        selfieUri = uri
    }
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Verifikasi Identitas",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D1B12)
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF0D1B12)
                            )
                        }
                    },
                    actions = {
                        Spacer(modifier = Modifier.width(48.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF6F8F6).copy(alpha = 0.95f)
                    )
                )
                
                // Progress Bar Section
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Langkah 2 dari 3",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF0D1B12))
                        )
                        Text(
                            text = "Dokumen",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium, color = Color.Gray)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.66f)
                                .height(8.dp)
                                .clip(CircleShape)
                                .background(Primary)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .border(1.dp, Color(0xFFF3F4F6))
                    .padding(24.dp)
            ) {
                // Security Note
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF16A34A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Data Anda terenkripsi & aman",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
                
                
                Button(
                    onClick = {
                        val currentIdCardUri = idCardFrontUri
                        val currentSelfieUri = selfieUri
                        if (currentIdCardUri != null || currentSelfieUri != null) {
                            isLoading = true
                            scope.launch {
                                var hasError = false
                                
                                if (currentIdCardUri != null) {
                                    val result = com.example.dwhubfix.data.SupabaseRepository.uploadFile(context, currentIdCardUri, "id_cards")
                                    if (result.isSuccess) {
                                        com.example.dwhubfix.data.SupabaseRepository.updateProfileDocument(context, "id_card_url", result.getOrNull()!!)
                                    } else {
                                        hasError = true
                                    }
                                }
                                
                                if (currentSelfieUri != null && !hasError) {
                                     val result = com.example.dwhubfix.data.SupabaseRepository.uploadFile(context, currentSelfieUri, "selfies")
                                     if (result.isSuccess) {
                                        com.example.dwhubfix.data.SupabaseRepository.updateProfileDocument(context, "selfie_url", result.getOrNull()!!)
                                    } else {
                                        hasError = true 
                                    }
                                }
                                
                                isLoading = false
                                if (!hasError) {
                                    // Save progress
                                    com.example.dwhubfix.data.SupabaseRepository.updateProfile(
                                        context = context,
                                        currentStep = "worker_face_verification"
                                    )
                                    onNavigateNext()
                                } else {
                                    errorMessage = "Upload failed. Please try again."
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(elevation = 8.dp, spotColor = Primary.copy(0.25f), shape = CircleShape),
                    enabled = !isLoading && (idCardFrontUri != null && selfieUri != null),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color(0xFF0D1B12)
                    ),
                    shape = CircleShape
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF0D1B12))
                    } else {
                        Text(
                            text = "Kirim Dokumen",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null
                        )
                    }
                }
                if (errorMessage != null) {
                     Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFFF6F8F6)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Headline
            Text(
                text = "Upload Dokumen",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0D1B12)
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            Text(
                text = "Mohon upload dokumen asli Anda agar akun bisa diaktifkan dan Anda dapat mulai bekerja.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF4B5563),
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Section 1: KTP
            SectionHeader(icon = Icons.Outlined.Badge, title = "KTP (Kartu Tanda Penduduk)")
            

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                // Front KTP
                DocumentUploadBox(
                    modifier = Modifier.weight(1f),
                    label = "Foto Depan",
                    hasImage = idCardFrontUri != null,
                    onClick = { launcherIdCard.launch("image/*") }
                )
                // Back KTP
                DocumentUploadBox(
                    modifier = Modifier.weight(1f),
                    label = "Foto Belakang"
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Pastikan NIK dan data terlihat jelas",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6B7280))
                )
            }

            // Section 2: Selfie
            SectionHeader(icon = Icons.Outlined.Face, title = "Foto Selfie dengan KTP")
            
            Surface(
                onClick = { launcherSelfie.launch("image/*") },
                shape = RoundedCornerShape(16.dp),
                color = if (selfieUri != null) Color(0xFFF0FDF4) else Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(192.dp)
                    .border(2.dp, if (selfieUri != null) Primary else Color(0xFFD1D5DB), RoundedCornerShape(16.dp)) // dashed border simulation
                    .padding(bottom = 32.dp) // Section margin
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(bottom = 12.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.PermIdentity,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFFE5E7EB)
                        )
                        Box(
                            modifier = Modifier
                                .background(Primary, CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.Black
                            )
                        }
                    }
                    Text(
                        text = "Ambil Foto Selfie",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF374151)
                        )
                    )
                    Text(
                        text = "Wajah dan KTP harus dalam frame",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF6B7280)
                        )
                    )
                }
            }

            // Section 3: NPWP
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NPWP",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D1B12)
                        )
                    )
                }
                Surface(
                    color = Color(0xFFF3F4F6),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Opsional",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF4B5563)
                        )
                    )
                }
            }
            
            Surface(
                onClick = { /* TODO: Pick File */ },
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                   Row(verticalAlignment = Alignment.CenterVertically) {
                       Box(
                           modifier = Modifier
                               .size(40.dp)
                               .background(Primary.copy(alpha = 0.1f), CircleShape),
                           contentAlignment = Alignment.Center
                       ) {
                           Icon(
                               imageVector = Icons.Default.UploadFile,
                               contentDescription = null,
                               tint = Primary
                           )
                       }
                       Spacer(modifier = Modifier.width(12.dp))
                       Column {
                           Text(
                               text = "Upload Kartu NPWP",
                               style = MaterialTheme.typography.bodyMedium.copy(
                                   fontWeight = FontWeight.Bold,
                                   color = Color(0xFF0D1B12)
                               )
                           )
                           Text(
                               text = "Format JPG, PNG atau PDF",
                               style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6B7280))
                           )
                       }
                   }
                   Icon(
                       imageVector = Icons.Default.ChevronRight,
                       contentDescription = null,
                       tint = Color(0xFF9CA3AF)
                   )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp)) // Padding for bottom bar
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier.padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0D1B12)
            )
        )
    }
}

@Composable
private fun DocumentUploadBox(
    modifier: Modifier = Modifier,
    label: String,
    hasImage: Boolean = false,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (hasImage) Color(0xFFF0FDF4) else Color.White,
        modifier = modifier
            .aspectRatio(4f/3f)
            .border(2.dp, if (hasImage) Primary else Color(0xFFD1D5DB), RoundedCornerShape(16.dp)) 
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFF3F4F6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = null,
                    tint = Color(0xFF9CA3AF),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B7280)
                )
            )
        }
    }
}

@Preview
@Composable
fun WorkerDocumentUploadScreenPreview() {
    DailyWorkerHubTheme {
        WorkerDocumentUploadScreen({}, {})
    }
}
