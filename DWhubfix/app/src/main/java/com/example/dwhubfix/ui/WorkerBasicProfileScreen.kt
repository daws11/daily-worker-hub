package com.example.dwhubfix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dwhubfix.ui.theme.DailyWorkerHubTheme
import com.example.dwhubfix.ui.theme.Primary

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import coil.compose.AsyncImage
import com.example.dwhubfix.data.WorkerCategories
import kotlinx.coroutines.launch
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkerBasicProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var selectedSkill by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var showCategorySheet by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState()
    
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri
        }
    }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Setup Profil",
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
                    // Spacer to balance the title
                    Spacer(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (fullName.isNotEmpty()) {
                            isLoading = true
                            scope.launch {
                                var currentAvatarUrl: String? = null
                                var uploadError: String? = null

                                if (avatarUri != null) {
                                    val uploadResult = com.example.dwhubfix.data.SupabaseRepository.uploadFile(context, avatarUri!!, "avatars")
                                    if (uploadResult.isSuccess) {
                                        currentAvatarUrl = uploadResult.getOrNull()
                                    } else {
                                        uploadError = uploadResult.exceptionOrNull()?.message
                                    }
                                }

                                if (uploadError != null) {
                                    isLoading = false
                                    errorMessage = "Upload failed: $uploadError"
                                    return@launch
                                }

                                val result = com.example.dwhubfix.data.SupabaseRepository.updateProfile(
                                    context = context, 
                                    fullName = fullName, 
                                    role = "worker",
                                    avatarUrl = currentAvatarUrl,
                                    jobCategory = selectedCategory,
                                    jobRole = selectedSkill,
                                    currentStep = "worker_document_upload"
                                )
                                isLoading = false
                                if (result.isSuccess) {
                                    onNavigateNext()
                                } else {
                                    // Make sure result.exceptionOrNull() is not null before accessing message
                                    val error = result.exceptionOrNull()
                                    errorMessage = error?.message ?: "Update failed"
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(elevation = 8.dp, spotColor = Primary.copy(0.2f), shape = CircleShape),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color(0xFF003314)
                    ),
                    shape = CircleShape
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                             modifier = Modifier.size(24.dp),
                             color = Color(0xFF003314)
                        )
                    } else {
                        Text(
                            text = "Lanjut",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
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
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Page Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(8.dp)
                        .background(Primary, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFCFE7D7), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFCFE7D7), CircleShape)
                )
            }

            // Headline
            Text(
                text = "Lengkapi Profil Anda",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFF0D1B12)
                )
            )
            Text(
                text = "Agar bisnis mudah mengenali Anda saat melamar pekerjaan.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF0D1B12).copy(alpha = 0.7f),
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Avatar Upload
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(128.dp)
                        .clickable { 
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(4.dp, Color.White),
                            shadowElevation = 4.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (avatarUri != null) {
                                    AsyncImage(
                                        model = avatarUri,
                                        contentDescription = "Selected Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color(0xFF166534).copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .background(Primary, CircleShape)
                                .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Upload Photo",
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF003314)
                            )
                        }
                    }
                    Text(
                        text = "Upload Foto Profil",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D1B12)
                        ),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                    Text(
                        text = "Tap untuk mengambil foto",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        )
                    )
                }
            }

            // Form
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Name Input
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "NAMA LENGKAP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF0D1B12).copy(alpha = 0.8f)
                        )
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        placeholder = { Text("Nama sesuai KTP") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Primary,
                            focusedTextColor = Color(0xFF0D1B12),
                            unfocusedTextColor = Color(0xFF0D1B12)
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountBox, // Using AccountBox as Badge alternative
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    )
                }

                // Skill Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "KATEGORI SKILL UTAMA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF0D1B12).copy(alpha = 0.8f)
                        )
                    )
                    Surface(
                        onClick = { showCategorySheet = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedSkill.isEmpty()) "Pilih kategori" else "$selectedCategory - $selectedSkill",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = if (selectedSkill.isEmpty()) Color.Gray else Color(0xFF0D1B12),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF0D1B12)
                            )
                        }
                    }
                }

                // Quick Tags
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val suggestions = listOf("Housekeeping", "Cook Helper", "Server")
                    suggestions.forEach { suggestion ->
                        Surface(
                            shape = CircleShape,
                            color = Primary.copy(alpha = 0.1f),
                            onClick = { selectedSkill = suggestion }
                        ) {
                            Text(
                                text = suggestion,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF054019)
                                )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp)) // Space for bottom bar
        }
        
        if (showCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                sheetState = sheetState,
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Pilih Kategori Pekerjaan",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        WorkerCategories.categories.forEach { (category, roles) ->
                            item {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Primary, 
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            items(roles) { role ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCategory = category
                                            selectedSkill = role
                                            showCategorySheet = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = role,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = Color(0xFF0D1B12)
                                        )
                                    )
                                }
                                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun WorkerBasicProfileScreenPreview() {
    DailyWorkerHubTheme {
        WorkerBasicProfileScreen({}, {})
    }
}
