package com.example.dwhubfix.ui.dashboard.worker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dwhubfix.data.SessionManager
import com.example.dwhubfix.data.SupabaseRepository
import com.example.dwhubfix.data.WorkerCategories
import com.example.dwhubfix.ui.theme.Primary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    
    var userProfile by remember { mutableStateOf<com.example.dwhubfix.model.UserProfile?>(null) }
    var isLoadingProfile by remember { mutableStateOf(true) }
    
    // Form State
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedSkill by remember { mutableStateOf("") }
    var experienceLevel by remember { mutableStateOf("") }
    var isAvailable by remember { mutableStateOf(true) }
    
    // Password State
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswordFields by remember { mutableStateOf(false) }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    // Avatar Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            avatarUri = uri
        }
    }

    // Load Profile
    LaunchedEffect(Unit) {
        val result = SupabaseRepository.getProfile(context)
        result.onSuccess { profile ->
            userProfile = profile
            fullName = profile.fullName ?: ""
            phoneNumber = profile.phoneNumber ?: ""
            selectedCategory = profile.workerProfile?.jobCategory ?: ""
            selectedSkill = profile.workerProfile?.jobRole ?: ""
            experienceLevel = profile.workerProfile?.yearsExperience ?: ""
            isAvailable = profile.onboardingStatus != "inactive"
            isLoadingProfile = false
        }.onFailure {
            isLoadingProfile = false
        }
    }

    // Show Success Message
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            successMessage = null
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profil",
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
                    TextButton(
                        onClick = {
                            scope.launch {
                                if (fullName.isBlank()) {
                                    errorMessage = "Nama wajib diisi"
                                    return@launch
                                }
                                
                                isSaving = true
                                errorMessage = null
                                
                                try {
                                    // Upload avatar if changed
                                    var uploadedAvatarUrl: String? = null
                                    if (avatarUri != null) {
                                        val uploadResult = SupabaseRepository.uploadFile(context, avatarUri!!, "avatars")
                                        if (uploadResult.isSuccess) {
                                            uploadedAvatarUrl = uploadResult.getOrNull()
                                        } else {
                                            throw Exception(uploadResult.exceptionOrNull()?.message ?: "Upload gagal")
                                        }
                                    }
                                    
                                    // Update basic profile
                                    val result = SupabaseRepository.updateProfile(
                                        context = context,
                                        fullName = fullName,
                                        avatarUrl = uploadedAvatarUrl,
                                        currentStep = null // Don't change onboarding step
                                    )
                                    
                                    if (result.isSuccess) {
                                        // Update phone if changed
                                        if (phoneNumber != userProfile?.phoneNumber) {
                                            SessionManager.savePhoneNumber(context, phoneNumber)
                                        }
                                        
                                        // Update worker profile
                                        if (selectedSkill.isNotEmpty()) {
                                            // Find category from WorkerCategories
                                            WorkerCategories.categories.forEach { (cat, skills) ->
                                                if (skills.contains(selectedSkill)) {
                                                    selectedCategory = cat
                                                }
                                            }
                                            
                                            val workerResult = SupabaseRepository.updateProfile(
                                                context = context,
                                                jobCategory = selectedCategory,
                                                jobRole = selectedSkill,
                                                currentStep = null
                                            )
                                            
                                            if (workerResult.isFailure) {
                                                throw Exception(workerResult.exceptionOrNull()?.message)
                                            }
                                        }
                                        
                                        // Update experience if changed
                                        if (experienceLevel.isNotEmpty() && experienceLevel != userProfile?.workerProfile?.yearsExperience) {
                                            val expResult = SupabaseRepository.updateWorkerExperience(
                                                context = context,
                                                experienceLevel = experienceLevel,
                                                currentStep = null
                                            )
                                            
                                            if (expResult.isFailure) {
                                                throw Exception(expResult.exceptionOrNull()?.message)
                                            }
                                        }
                                        
                                        // Update password if provided
                                        if (showPasswordFields && currentPassword.isNotBlank() && newPassword.isNotBlank()) {
                                            // Password update logic would go here
                                            // For now, just show message
                                        }
                                        
                                        successMessage = "Profil berhasil diupdate!"
                                        isSaving = false
                                        onNavigateBack()
                                    } else {
                                        throw Exception(result.exceptionOrNull()?.message ?: "Update gagal")
                                    }
                                } catch (e: Exception) {
                                    errorMessage = e.message
                                    isSaving = false
                                }
                            }
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                "Simpan",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF6F8F6)
    ) { paddingValues ->
        if (isLoadingProfile) {
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
                
                // Avatar Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(4.dp, Color.White, CircleShape)
                            .clickable { 
                                photoPickerLauncher.launch(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            }
                    ) {
                        if (avatarUri != null) {
                            AsyncImage(
                                model = avatarUri,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else if (userProfile?.avatarUrl != null) {
                            AsyncImage(
                                model = userProfile!!.avatarUrl,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "Ganti Foto",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        ),
                        modifier = Modifier.clickable { 
                            photoPickerLauncher.launch(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Availability Toggle
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Status Ketersediaan",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                if (isAvailable) "Anda menerima tawaran job" else "Anda tidak menerima job baru",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280)
                            )
                        }
                        Switch(
                            checked = isAvailable,
                            onCheckedChange = { isAvailable = it },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Primary,
                                checkedThumbColor = Color(0xFF102216)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Personal Info Section
                Text(
                    "Informasi Pribadi",
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Full Name
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Nama Lengkap") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = Color.Gray)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Phone Number
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Nomor Telepon") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Work Info Section
                Text(
                    "Informasi Pekerjaan",
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Job Category & Role (Combined selector for simplicity)
                        var showCategorySheet by remember { mutableStateOf(false) }
                        
                        Text(
                            "Kategori & Posisi",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280)
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Surface(
                            onClick = { showCategorySheet = true },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF9FAFB),
                            border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (selectedSkill.isEmpty()) "Pilih kategori & posisi" 
                                    else "$selectedCategory - $selectedSkill",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (selectedSkill.isEmpty()) Color.Gray else Color(0xFF102216)
                                )
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        }
                        
                        // Category Sheet
                        if (showCategorySheet) {
                            androidx.compose.material3.ModalBottomSheet(
                                onDismissRequest = { showCategorySheet = false },
                                containerColor = Color.White
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .padding(bottom = 24.dp)
                                ) {
                                    Text(
                                        "Pilih Posisi",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(bottom = 16.dp)
                                    )
                                    
                                    androidx.compose.foundation.lazy.LazyColumn {
                                        WorkerCategories.categories.forEach { (category, roles) ->
                                            item {
                                                Text(
                                                    category,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        color = Primary,
                                                        fontWeight = FontWeight.Bold
                                                    ),
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                            }
                                            items(roles) { role ->
                                                androidx.compose.material3.ListItem(
                                                    headlineContent = { Text(role) },
                                                    trailingContent = {
                                                        if (selectedSkill == role) {
                                                            Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = "Selected",
                                                                tint = Primary
                                                            )
                                                        }
                                                    },
                                                    colors = androidx.compose.material3.ListItemDefaults.colors(
                                                        containerColor = if (selectedSkill == role) 
                                                            Primary.copy(alpha = 0.1f) 
                                                        else Color.Transparent
                                                    ),
                                                    modifier = Modifier.clickable {
                                                        selectedCategory = category
                                                        selectedSkill = role
                                                        showCategorySheet = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Experience Level
                        Text(
                            "Pengalaman",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B7280)
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("< 1 Tahun", "1-3 Tahun", "3-5 Tahun", "> 5 Tahun").forEach { level ->
                                val isSelected = experienceLevel == level
                                androidx.compose.material3.FilterChip(
                                    selected = isSelected,
                                    onClick = { experienceLevel = level },
                                    label = { Text(level, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Password Section
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Ganti Password",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        if (showPasswordFields) "Sembunyikan" else "Tampilkan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                            }
                            IconButton(onClick = { showPasswordFields = !showPasswordFields }) {
                                Icon(
                                    if (showPasswordFields) Icons.Default.KeyboardArrowUp 
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                        }
                        
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showPasswordFields,
                            enter = androidx.compose.animation.expandVertically(),
                            exit = androidx.compose.animation.shrinkVertically()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = currentPassword,
                                    onValueChange = { currentPassword = it },
                                    label = { Text("Password Saat Ini") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = if (currentPasswordVisible) 
                                        VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { currentPasswordVisible = !currentPasswordVisible }) {
                                            Icon(
                                                if (currentPasswordVisible) Icons.Default.Visibility 
                                                else Icons.Default.VisibilityOff,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text("Password Baru") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = if (newPasswordVisible) 
                                        VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                            Icon(
                                                if (newPasswordVisible) Icons.Default.Visibility 
                                                else Icons.Default.VisibilityOff,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedTextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    label = { Text("Konfirmasi Password Baru") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    visualTransformation = if (confirmPasswordVisible) 
                                        VisualTransformation.None else PasswordVisualTransformation(),
                                    isError = confirmPassword.isNotBlank() && confirmPassword != newPassword,
                                    supportingText = if (confirmPassword.isNotBlank() && confirmPassword != newPassword) {
                                        { Text("Password tidak cocok") }
                                    } else null,
                                    trailingIcon = {
                                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                            Icon(
                                                if (confirmPasswordVisible) Icons.Default.Visibility 
                                                else Icons.Default.VisibilityOff,
                                                contentDescription = null
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Error Message
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
