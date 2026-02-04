package com.example.dwhubfix.ui

import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Message
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.clickable
import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.foundation.Image
import coil.compose.rememberAsyncImagePainter
import org.json.JSONObject
import com.example.dwhubfix.data.SessionManager
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Badge
import coil.compose.AsyncImage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dwhubfix.ui.theme.DailyWorkerHubTheme
import com.example.dwhubfix.ui.theme.Primary
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.blur
import androidx.compose.material3.SwitchDefaults
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Switch
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Shield
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.example.dwhubfix.data.SupabaseRepository
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.filled.CheckCircle
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

// OSM Imports
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


@Composable
fun BusinessWelcomeScreen(
    onNavigateToRegistration: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // background-light
    ) {
        val heroHeight = maxHeight * 0.55f

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight) // Responsive height
                    .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
            ) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuC6IL3eQtv3JyoWbH94Ox3KdfHfOEOYFpa70rJTdGbD4UbmW1uYPeVHXxTy12YoKQzD6hNobs7NlvhbxFRC1Xfdkuca7-LTn_6QyGpNnBlNTvNkVrUCrOZLIeSqdhdWEfdS2ENdYI3U4S_R2LCXymnp7kP01VR6W804wRwQEC4HW_Or55rXPMsxXB_J-hrBkyOGbI_eJ6m9qqcLaDublGMZI1YY63Qnwsz41jEAlWW2scCHVIsjzR6sBDLMVaBC43fmvFiJ6ExJXns",
                    contentDescription = "Business Hero",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.1f)
                                )
                            )
                        )
                )

                // Top Header Overlay
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo Area
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Language Toggle
                    Surface(
                        color = Color.Black.copy(alpha = 0.2f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.White,
                                shape = CircleShape,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = "ID",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFF102216),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Text(
                                text = "EN",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }

            // Content Section
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Temukan staf andalan untuk bisnismu",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp,
                            lineHeight = 40.sp
                        ),
                        color = Color(0xFF111813),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Rekrut pekerja harian berkualitas dengan cepat dan mudah. Kelola shift dan pembayaran dalam satu aplikasi.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF4B5563), // gray-600
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Primary Button
                    Button(
                        onClick = onNavigateToRegistration,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF111813), // Darker for business
                            contentColor = Color.White
                        ),
                        shape = CircleShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(elevation = 8.dp, spotColor = Color(0xFF111813).copy(alpha = 0.5f), shape = CircleShape)
                    ) {
                        Text(
                            text = "Mulai Rekrut",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Secondary Link
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Sudah punya akun? ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF6B7280),
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "Masuk",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF111813),
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable { onNavigateToLogin() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BusinessRegistrationScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: (String) -> Unit // Now passes Email
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),

            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 1.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier.size(40.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Back",
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(20.dp),
                        tint = Color(0xFF111813)
                    )
                }
            }
            Text(
                text = "Step 1 of 5",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(end = 4.dp),
                color = Color.Transparent
            )
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Daftarkan Bisnis Anda",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 36.sp
                ),
                color = Color(0xFF111813)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Buat akun untuk mulai merekrut pekerja harian.",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                color = Color(0xFF4B5563)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Input
            Text(
                text = "Email",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF374151),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("nama@bisnis.com", color = Color(0xFF9CA3AF)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF111813),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White // or transparent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF9CA3AF))
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password Input
            Text(
                text = "Password",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF374151),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Minimal 6 karakter", color = Color(0xFF9CA3AF)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF111813),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else
                        Icons.Filled.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF9CA3AF))
                }
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (email.isNotBlank() && password.length >= 6) {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            try {
                                com.example.dwhubfix.data.SessionManager.savePendingRole(context, "business")
                                val result = com.example.dwhubfix.data.SupabaseRepository.signUpWithEmail(context, email, password)
                                isLoading = false
                                result.onSuccess {
                                    onNavigateNext(email)
                                }.onFailure {
                                    errorMessage = it.message ?: "Registration failed"
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                errorMessage = e.message ?: "An error occurred"
                            }
                        }
                    } else {
                        errorMessage = "Please enter a valid email and password (min 6 chars)"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(elevation = 8.dp, spotColor = Color(0xFF111813).copy(alpha = 0.2f), shape = CircleShape),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111813),
                    contentColor = Color.White
                ),
                enabled = !isLoading
            ) {
                 if (isLoading) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Buat Akun Bisnis",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Terms Text
            Text(
                text = "Dengan melanjutkan, Anda menyetujui Ketentuan Layanan dan Kebijakan Privasi kami.",
                style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BusinessBasicProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var businessName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val logoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> logoUri = uri }

    val businessCategories = listOf(
        "Restaurant",
        "Cafe",
        "Hotel",
        "Villa",
        "Event Organizer",
        "Catering Service",
        "Spa & Wellness",
        "Cleaning Service",
        "Security Agency",
        "Logistics & Transport",
        "Retail",
        "Other"
    )

    fun handleNext() {
        if (businessName.isBlank() || selectedCategory.isBlank()) {
            android.widget.Toast.makeText(context, "Mohon lengkapi data", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        scope.launch {
            var uploadedLogoUrl: String? = null
            
            if (logoUri != null) {
                val uploadResult = SupabaseRepository.uploadFile(context, logoUri!!, "avatars")
                if (uploadResult.isSuccess) {
                    uploadedLogoUrl = uploadResult.getOrNull()
                } else {
                    android.widget.Toast.makeText(context, "Gagal upload logo: ${uploadResult.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@launch
                }
            }

            val result = SupabaseRepository.updateBusinessBasicProfile(
                context = context,
                businessName = businessName,
                category = selectedCategory,
                logoUrl = uploadedLogoUrl,
                currentStep = "business_document_upload"
            )
            
            isLoading = false
            if (result.isSuccess) {
                onNavigateNext()
            } else {
                android.widget.Toast.makeText(context, "Gagal menyimpan: ${result.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profil Bisnis",
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
                    onClick = { handleNext() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(elevation = 8.dp, spotColor = Color(0xFF111813).copy(0.2f), shape = CircleShape),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF111813),
                        contentColor = Color.White
                    ),
                    shape = CircleShape
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
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
            // Page Indicators (Step 2 of 5 approx, counting logic varying)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 // Active
                Box(modifier = Modifier.width(32.dp).height(8.dp).background(Color(0xFF111813), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFE5E7EB), CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.size(8.dp).background(Color(0xFFE5E7EB), CircleShape))
            }

            Text(
                text = "Informasi Bisnis",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = Color(0xFF0D1B12)
                )
            )
            Text(
                text = "Lengkapi detail bisnis Anda agar pekerja tertarik untuk melamar.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF0D1B12).copy(alpha = 0.7f),
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )

            // Logo Upload
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier
                        .size(128.dp)
                        .clickable { 
                            logoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(4.dp, Color.White),
                            shadowElevation = 4.dp,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (logoUri != null) {
                                    AsyncImage(
                                        model = logoUri,
                                        contentDescription = "Selected Logo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Store,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = Color(0xFF111813).copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .background(Color(0xFF111813), CircleShape)
                                .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Upload Logo",
                                modifier = Modifier.size(20.dp),
                                tint = Color.White
                            )
                        }
                    }
                    Text(
                        text = "Upload Logo Bisnis",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0D1B12)
                        ),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            // Form
            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                // Name Input
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "NAMA BISNIS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF0D1B12).copy(alpha = 0.8f)
                        )
                    )
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        placeholder = { Text("Contoh: Kopi Kenangan") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color.White,
                            focusedContainerColor = Color.White,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xFF111813)
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Business,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    )
                }

                // Category Selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "KATEGORI BISNIS",
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
                                text = if (selectedCategory.isEmpty()) "Pilih kategori (misal: Cafe)" else selectedCategory,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = if (selectedCategory.isEmpty()) Color.Gray else Color(0xFF0D1B12),
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
                    businessCategories.take(6).forEach { suggestion ->
                        Surface(
                            shape = CircleShape,
                            color = if (selectedCategory == suggestion) Color(0xFF111813) else Color(0xFFE5E7EB),
                            contentColor = if (selectedCategory == suggestion) Color.White else Color(0xFF4B5563),
                            onClick = { selectedCategory = suggestion }
                        ) {
                            Text(
                                text = suggestion,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
        
        if (showCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Pilih Kategori Bisnis",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(businessCategories) { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCategory = category
                                        showCategorySheet = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = Color(0xFF0D1B12),
                                        fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                                if (selectedCategory == category) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color(0xFF16A34A)
                                    )
                                }
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessDocumentUploadScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    var nibUri by remember { mutableStateOf<Uri?>(null) }
    var frontUri by remember { mutableStateOf<Uri?>(null) }
    var insideUri by remember { mutableStateOf<Uri?>(null) }

    val nibLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        nibUri = uri
    }
    val frontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        frontUri = uri
    }
    val insideLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        insideUri = uri
    }

    fun handleNext() {
        if (nibUri == null || frontUri == null || insideUri == null) {
            Toast.makeText(context, "Mohon upload semua dokumen", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        scope.launch {
            try {
                // Upload files
                val nibUpload = SupabaseRepository.uploadFile(context, nibUri!!, "documents")
                val frontUpload = SupabaseRepository.uploadFile(context, frontUri!!, "verification")
                val insideUpload = SupabaseRepository.uploadFile(context, insideUri!!, "verification")

                if (nibUpload.isFailure || frontUpload.isFailure || insideUpload.isFailure) {
                    throw Exception("Gagal mengupload dokumen")
                }

                val result = SupabaseRepository.updateBusinessDocuments(
                    context = context,
                    nibUrl = nibUpload.getOrNull(),
                    locationFrontUrl = frontUpload.getOrNull(),
                    locationInsideUrl = insideUpload.getOrNull(),
                    currentStep = "business_location_verification"
                )

                if (result.isSuccess) {
                    isLoading = false
                    onNavigateNext()
                } else {
                    throw Exception(result.exceptionOrNull()?.message)
                }
            } catch (e: Exception) {
                isLoading = false
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "Verifikasi Legalitas",
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
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
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
                                .background(Color(0xFF111813))
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
                    onClick = { handleNext() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(elevation = 8.dp, spotColor = Color(0xFF111813).copy(0.25f), shape = CircleShape),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF111813),
                        contentColor = Color.White
                    ),
                    shape = CircleShape
                ) {
                    if (isLoading) {
                         androidx.compose.material3.CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
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
            // Headline
            Text(
                text = "Upload Dokumen Usaha",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0D1B12)
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
            Text(
                text = "Mohon upload dokumen legalitas agar bisnis Anda terverifikasi.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF4B5563),
                    lineHeight = 20.sp
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Section 1: NIB
            SectionHeader(icon = Icons.Outlined.Description, title = "NIB (Nomor Induk Berusaha)")
            
            Surface(
                onClick = { nibLauncher.launch("application/pdf") }, // Prefer PDF
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, if (nibUri != null) Color(0xFF16A34A) else Color(0xFFE5E7EB)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
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
                               .background(Color(0xFF111813).copy(alpha = 0.1f), CircleShape),
                           contentAlignment = Alignment.Center
                       ) {
                           Icon(
                               imageVector = Icons.Default.UploadFile,
                               contentDescription = null,
                               tint = Color(0xFF111813)
                           )
                       }
                       Spacer(modifier = Modifier.width(12.dp))
                       Column {
                           Text(
                               text = if (nibUri != null) "Dokumen Terpilih" else "Upload Dokumen NIB",
                               style = MaterialTheme.typography.bodyMedium.copy(
                                   fontWeight = FontWeight.Bold,
                                   color = Color(0xFF0D1B12)
                               )
                           )
                           Text(
                               text = if (nibUri != null) "Siap diupload" else "PDF atau JPG (Max 5MB)",
                               style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6B7280))
                           )
                       }
                   }
                   Icon(
                       imageVector = if (nibUri != null) Icons.Default.CheckCircle else Icons.Default.ChevronRight,
                       contentDescription = null,
                       tint = if (nibUri != null) Color(0xFF16A34A) else Color(0xFF9CA3AF)
                   )
                }
            }

            // Section 2: Foto Lokasi
            SectionHeader(icon = Icons.Default.Store, title = "Foto Lokasi Usaha")
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                // Front
                DocumentUploadBox(
                    modifier = Modifier.weight(1f),
                    label = "Tampak Depan",
                    isActive = frontUri != null,
                    onClick = { frontLauncher.launch("image/*") }
                )
                // Inside
                DocumentUploadBox(
                    modifier = Modifier.weight(1f),
                    label = "Tampak Dalam",
                     isActive = insideUri != null,
                    onClick = { insideLauncher.launch("image/*") }
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
                    text = "Pastikan papan nama usaha terlihat",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF6B7280))
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DocumentUploadBox(
    modifier: Modifier = Modifier,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) Color(0xFF16A34A) else Color(0xFFE5E7EB)),
        modifier = modifier.aspectRatio(1f)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.AddAPhoto,
                contentDescription = null,
                tint = if (isActive) Color(0xFF16A34A) else Color(0xFF9CA3AF),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4B5563)
                )
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessLocationVerificationScreen(
    onNavigateBack: () -> Unit,
     onVerificationSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Initialize OSM Configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // State for location
    var currentGeoPoint by remember { mutableStateOf<GeoPoint?>(null) } // Default null
    var addressText by remember { mutableStateOf("Detecting address...") }
    var mapView: MapView? by remember { mutableStateOf(null) }
    var photoEvidenceUri by remember { mutableStateOf<Uri?>(null) }
    
    var isLoading by remember { mutableStateOf(false) }
    
    // Lifecycle observer for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onDetach()
        }
    }

    // Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            if (isGranted) {
                getLocation(context) { geoPoint ->
                    currentGeoPoint = geoPoint
                    addressText = "Lat: ${geoPoint.latitude}, Lng: ${geoPoint.longitude}"
                    mapView?.controller?.animateTo(geoPoint)
                    mapView?.controller?.setZoom(18.0)
                    updateMarker(mapView, geoPoint)
                }
            } else {
                Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> photoEvidenceUri = uri }
    )

    // Initial Location Check
    LaunchedEffect(Unit) {
         if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
             getLocation(context) { geoPoint ->
                currentGeoPoint = geoPoint
                addressText = "Lat: ${geoPoint.latitude}, Lng: ${geoPoint.longitude}"
             }
         } else {
             locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
         }
    }

    fun handleVerification() {
        if (currentGeoPoint == null) {
            Toast.makeText(context, "Please pinpoint your location first", Toast.LENGTH_SHORT).show()
            return
        }
        if (photoEvidenceUri == null) {
            Toast.makeText(context, "Please provide a storefront photo", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        scope.launch {
            val photoUploadResult = SupabaseRepository.uploadVerificationImage(context, photoEvidenceUri!!)
            val photoUrl = photoUploadResult.getOrNull()

            if (photoUrl == null) {
                isLoading = false
                Toast.makeText(context, "Failed to upload photo", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val result = SupabaseRepository.updateBusinessLocation(
                context = context,
                address = addressText,
                latitude = currentGeoPoint!!.latitude,
                longitude = currentGeoPoint!!.longitude,
                photoUrl = photoUrl,
                currentStep = "business_details"
            )

            isLoading = false
            if (result.isSuccess) {
                onVerificationSuccess()
            } else {
                android.widget.Toast.makeText(context, "Gagal verifikasi lokasi: ${result.exceptionOrNull()?.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Verify Location",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111813)
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
                            tint = Color(0xFF111813)
                        )
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(24.dp)
                    .shadow(elevation = 16.dp, spotColor = Color(0xFF000000).copy(0.05f))
            ) {
                Button(
                    onClick = { handleVerification() },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(elevation = 8.dp, spotColor = Color(0xFF13EC5B).copy(0.2f), shape = RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF13EC5B), // Primary Green
                        contentColor = Color(0xFF111813)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = Color(0xFF111813),
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Confirm & Verify",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF111813)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Progress Bar
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Step 3 of 4",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                    )
                    Text(
                        text = "75% Completed",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF13EC5B)
                        )
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
                            .fillMaxWidth(0.75f)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF13EC5B))
                    )
                }
            }

            // Map Section
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Pinpoint your location",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111813)
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Drag the map to pinpoint the exact entrance.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(256.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .shadow(4.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
                ) {
                   AndroidView(
                        factory = { ctx ->
                            MapView(ctx).apply {
                                setTileSource(TileSourceFactory.MAPNIK)
                                setMultiTouchControls(true)
                                controller.setZoom(10.0)
                                controller.setCenter(GeoPoint(-8.650000, 115.216667)) // Default Bali
                                mapView = this
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { view ->
                            currentGeoPoint?.let { point ->
                                 view.controller.setCenter(point)
                                 if (view.zoomLevelDouble < 15.0) view.controller.setZoom(18.0)
                                 updateMarker(view, point)
                            }
                        }
                    )
                    
                    // Recenter Button
                    Surface(
                        onClick = {
                             if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                  getLocation(context) { geoPoint ->
                                     currentGeoPoint = geoPoint
                                     addressText = "Lat: ${geoPoint.latitude}, Lng: ${geoPoint.longitude}"
                                     mapView?.controller?.animateTo(geoPoint)
                                     mapView?.controller?.setZoom(18.0)
                                     updateMarker(mapView, geoPoint)
                                  }
                             } else {
                                 locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                             }
                        },
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Recenter",
                                tint = Color(0xFF374151)
                            )
                        }
                    }
                }

                // Address Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF3F4F6)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = Color(0xFF13EC5B),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "DETECTED ADDRESS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF13EC5B),
                                    letterSpacing = 0.5.sp
                                )
                            )
                            Text(
                                text = if (currentGeoPoint != null) addressText else "Locating...",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111813)
                                ),
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Photo Verification
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Storefront Verification",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111813)
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Take a clear photo of your business sign and entrance.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280)
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Surface(
                    onClick = { photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF9FAFB),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD1D5DB)), // Dashed equivalent needed
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f)
                ) {
                    if (photoEvidenceUri != null) {
                        Box {
                            AsyncImage(
                                model = photoEvidenceUri,
                                contentDescription = "Selected Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Change button overlay
                             Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = "Tap to change",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF13EC5B).copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                tint = Color(0xFF13EC5B)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tap to take photo",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4B5563)
                            )
                        )
                    }
                }
            }
        }

            // Trust Note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 32.dp)
                    .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFDBEAFE), RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info, // Policy equivalent
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Government Database Check",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                        )
                        Text(
                            text = "For security, we cross-reference this location and photo with the Bali Tourism Permit Database.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF1D4ED8),
                                lineHeight = 16.sp
                            ),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun getLocation(context: Context, callback: (GeoPoint) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            callback(GeoPoint(location.latitude, location.longitude))
        } else {
            Toast.makeText(context, "Location not found, ensure GPS is on", Toast.LENGTH_SHORT).show()
        }
    }
}

private fun updateMarker(mapView: MapView?, geoPoint: GeoPoint) {
    mapView?.let { map ->
        map.overlays.removeAll { it is Marker }
        val marker = Marker(map)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Detected Location"
        map.overlays.add(marker)
        map.invalidate()
    }
}

@Preview
@Composable
fun BusinessScreensPreview() {
    DailyWorkerHubTheme {
        Column {
             BusinessWelcomeScreen({}, {}, {})
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFE5E7EB), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF111813),
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFF111813)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BusinessDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    var slogan by remember { mutableStateOf("") } // Kept for state but unused in new design if strict to HTML
    var openTime by remember { mutableStateOf("09:00") }
    var closeTime by remember { mutableStateOf("22:00") }
    var description by remember { mutableStateOf("") }

    // State for Location Photos
    var locationPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    // Image Picker
    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isUploadingPhoto = true
            scope.launch {
                val result = SupabaseRepository.uploadVerificationImage(context, uri)
                if (result.isSuccess) {
                    val url = result.getOrNull()
                    if (url != null) {
                        locationPhotos = locationPhotos + url
                    }
                } else {
                    Toast.makeText(context, "Gagal upload foto", Toast.LENGTH_SHORT).show()
                }
                isUploadingPhoto = false
            }
        }
    }
    
    val facilities = listOf(
        "WiFi Gratis" to Icons.Default.SignalCellularAlt, // Using available icons
        "Makan Siang" to Icons.Default.Store,
        "Parkir Staff" to Icons.Default.LocationOn,
        "Ruang AC" to Icons.Default.AcUnit, // Need to check if available, else fallback
        "Seragam" to Icons.Default.Checkroom, 
        "Ruang Istirahat" to Icons.Default.Chair,
        "Uang Tips" to Icons.Default.AttachMoney
    )
    val selectedFacilities = remember { androidx.compose.runtime.mutableStateListOf("WiFi Gratis", "Makan Siang") }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detail Bisnis",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111813)
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
                             tint = Color(0xFF111813)
                        )
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF6F8F6))
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha=0.95f))
                    .padding(16.dp)
                    .shadow(16.dp)
            ) {
                Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            // Update Details
                            val detailsResult = SupabaseRepository.updateBusinessDetails(
                                context = context,
                                openTime = openTime,
                                closeTime = closeTime,
                                description = description,
                                facilities = selectedFacilities.toList(),
                                currentStep = "worker_preferences"
                            )

                            // Update Photo (if exists)
                            var photoResult = Result.success(Unit)
                            if (locationPhotos.isNotEmpty()) {
                                photoResult = SupabaseRepository.updateBusinessDocuments(
                                    context = context,
                                    nibUrl = null,
                                    locationFrontUrl = locationPhotos.firstOrNull(),
                                    locationInsideUrl = locationPhotos.getOrNull(1),
                                    currentStep = null 
                                )
                            }

                            isLoading = false
                            if (detailsResult.isSuccess && photoResult.isSuccess) {
                                onNavigateNext()
                            } else {
                                val errorMsg = if (detailsResult.isFailure) detailsResult.exceptionOrNull()?.message else photoResult.exceptionOrNull()?.message
                                Toast.makeText(context, "Gagal menyimpan data: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(elevation = 8.dp, spotColor = Color(0xFF13EC5B).copy(0.2f), shape = RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF13EC5B),
                        contentColor = Color(0xFF111813)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF111813)
                        )
                    } else {
                        Text(
                            text = "Simpan & Lanjutkan",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF111813)
                            )
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF6F8F6)
    ) { paddingValues ->
        if (isLoading) {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator(color = Color(0xFF13EC5B))
             }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Operating Hours
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Waktu Operasional",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Open Time
                    Column(modifier = Modifier.weight(1f)) {
                         Text(
                            text = "Jam Buka",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = openTime,
                            onValueChange = { openTime = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color(0xFFDBE6DF)
                            ),
                             singleLine = true
                        )
                    }
                    // Close Time
                    Column(modifier = Modifier.weight(1f)) {
                         Text(
                            text = "Jam Tutup",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = closeTime,
                            onValueChange = { closeTime = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color(0xFFDBE6DF)
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E7EB))

            // Description
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Tentang Bisnis",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = "Deskripsi Lengkap",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Ceritakan tentang restoran/hotel Anda, suasana, jenis masakan, dan apa yang diharapkan dari pekerja harian...") },
                    modifier = Modifier.fillMaxWidth().height(144.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFDBE6DF)
                    )
                )
                Text(
                    text = "${description.length}/500 karakter",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = TextAlign.End
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E7EB))
            
            // Facilities
            Column(modifier = Modifier.padding(24.dp)) {
                 Text(
                    text = "Fasilitas yang Tersedia",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Pilih fasilitas yang akan didapatkan pekerja.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    facilities.forEach { (name, icon) ->
                        val isSelected = selectedFacilities.contains(name)
                        Surface(
                            onClick = { 
                                if (isSelected) selectedFacilities.remove(name) else selectedFacilities.add(name)
                            },
                            shape = CircleShape,
                            color = if (isSelected) Color(0xFF13EC5B).copy(alpha = 0.1f) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF13EC5B) else Color(0xFFDBE6DF))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = icon, 
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSelected) Color(0xFF102216) else Color(0xFF111813)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF102216) else Color(0xFF111813)
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E7EB))

            // Photos
             Column(modifier = Modifier.padding(24.dp)) {
                 Row(
                     modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                     horizontalArrangement = Arrangement.SpaceBetween,
                     verticalAlignment = Alignment.Bottom
                 ) {
                     Text(
                        text = "Foto Lokasi",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                     Text(
                        text = "Edit",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF13EC5B), fontWeight = FontWeight.Bold)
                    )
                 }
                 
                 Row(
                     horizontalArrangement = Arrangement.spacedBy(12.dp),
                     modifier = Modifier.horizontalScroll(rememberScrollState())
                 ) {
                     // Add Button
                     Surface(
                         onClick = { 
                             photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                         },
                         shape = RoundedCornerShape(12.dp),
                         color = Color.White,
                         border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFDBE6DF)), // Dashed
                         modifier = Modifier.size(112.dp)
                     ) {
                         if (isUploadingPhoto) {
                             Box(contentAlignment = Alignment.Center) {
                                 CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF13EC5B))
                             }
                         } else {
                             Column(
                                 horizontalAlignment = Alignment.CenterHorizontally,
                                 verticalArrangement = Arrangement.Center
                             ) {
                                 Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color(0xFF13EC5B))
                                 Text("Tambah", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium))
                             }
                         }
                     }
                     
                     // Real Photos
                     locationPhotos.forEach { url ->
                         AsyncImage(
                             model = url,
                             contentDescription = null,
                             modifier = Modifier.size(112.dp).clip(RoundedCornerShape(12.dp)),
                             contentScale = ContentScale.Crop
                         )
                     }
                 }
             }
             
             Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BusinessWorkerPreferenceScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val skills = listOf(
        "Waiter" to Icons.Default.Restaurant,
        "Barista" to Icons.Default.LocalCafe,
        "Housekeeping" to Icons.Default.Bed,
        "Cook" to Icons.Default.Kitchen,
        "Dishwasher" to Icons.Default.CleaningServices,
        "Security" to Icons.Default.Security,
        "Villa Attendant" to Icons.Default.Home
    )
    val selectedSkills = remember { androidx.compose.runtime.mutableStateListOf("Waiter", "Cook") }

    val experienceLevels = listOf("< 1 Tahun", "1-3 Tahun", "> 3 Tahun")
    var selectedExperience by remember { mutableStateOf("1-3 Tahun") }
    
    val languages = listOf(
         "Bahasa Indonesia" to "🇮🇩",
         "English" to "🇬🇧",
         "Mandarin" to "🇨🇳"
    )
    val selectedLanguages = remember { androidx.compose.runtime.mutableStateListOf("Bahasa Indonesia") }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var priorityHiring by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Preferensi Pekerja",
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
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha=0.9f))
                    .padding(16.dp)
            ) {
                 Button(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            val success = SupabaseRepository.updateBusinessPreferences(
                                context = context,
                                selectedSkills = selectedSkills.toList(),
                                experienceLevel = selectedExperience,
                                languages = selectedLanguages.toList(),
                                priorityHiring = priorityHiring,
                                currentStep = "final_review"
                            )
                            isLoading = false
                            if (success.isSuccess) {
                                onNavigateNext()
                            } else {
                                val errorMsg = success.exceptionOrNull()?.message ?: "Unknown error"
                                Toast.makeText(context, "Gagal menyimpan preferensi: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                   modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(elevation = 8.dp, spotColor = Color(0xFF13EC5B).copy(0.2f), shape = RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF13EC5B),
                        contentColor = Color(0xFF111813)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                         CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF111813)
                        )
                    } else {
                        Text(
                            text = "Simpan Preferensi",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
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
            // Skills
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                   modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                   horizontalArrangement = Arrangement.SpaceBetween,
                   verticalAlignment = Alignment.CenterVertically
               ) {
                   Text(
                       text = "Skill yang sering dibutuhkan",
                       style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                   )
                   Text(
                       text = "Edit",
                       style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFF13EC5B), fontWeight = FontWeight.Medium)
                   )
               }
               
               FlowRow(
                   horizontalArrangement = Arrangement.spacedBy(10.dp),
                   verticalArrangement = Arrangement.spacedBy(10.dp)
               ) {
                   skills.forEach { (name, icon) ->
                       val isSelected = selectedSkills.contains(name)
                       Surface(
                           shape = CircleShape,
                           color = if (isSelected) Color(0xFF13EC5B).copy(alpha=0.2f) else Color.White,
                           border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF13EC5B) else Color.Transparent),
                           shadowElevation = if (isSelected) 0.dp else 1.dp,
                           onClick = {
                               if (isSelected) selectedSkills.remove(name) else selectedSkills.add(name)
                           }
                       ) {
                           Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null, 
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) Color(0xFF14532D) else Color.Gray
                                )
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF14532D) else Color(0xFF111813)
                                    )
                                )
                            }
                       }
                   }
               }
               
                Text(
                    text = "+ Tambah skill lainnya",
                    style = MaterialTheme.typography.labelMedium.copy(color = Color.Gray),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E7EB))

            // Experience
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Tingkat pengalaman",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3F4F6), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    experienceLevels.forEach { level ->
                        val isSelected = selectedExperience == level
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .shadow(if (isSelected) 2.dp else 0.dp, RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { selectedExperience = level },
                            contentAlignment = Alignment.Center
                        ) {
                             Text(
                                    text = level,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF111813) else Color(0xFF6B7280)
                                    )
                                )
                        }
                    }
                }
                 Text(
                    text = "Pilih tingkat pengalaman minimum yang diharapkan.",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray),
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                )
            }
            
            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E7EB))

            // Languages
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Bahasa yang dibutuhkan",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    languages.forEach { (lang, flag) ->
                         val isSelected = selectedLanguages.contains(lang)
                         Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFF13EC5B) else Color(0xFFE5E7EB)),
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                if (isSelected) selectedLanguages.remove(lang) else selectedLanguages.add(lang)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = flag, fontSize = 24.sp, modifier = Modifier.background(Color(0xFFF3F4F6), CircleShape).padding(8.dp))
                                    Column {
                                        Text(text = lang, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                        Text(text = "Native / Fluent", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                    }
                                }
                                
                                // Checkbox replacement
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(if (isSelected) Color(0xFF13EC5B) else Color(0xFFE5E7EB), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check, 
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFE5E7EB))

            // Priority
            Column(modifier = Modifier.padding(24.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                             Box(
                                modifier = Modifier.background(Color(0xFFDBEAFE), CircleShape).padding(8.dp)
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(text = "Butuh Cepat?", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text(text = "Prioritaskan pekerja terdekat", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                            }
                        }
                        Switch(
                            checked = priorityHiring, 
                            onCheckedChange = { priorityHiring = it }, 
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF13EC5B))
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessFinalReviewScreen(
    onNavigateBack: () -> Unit,
    onSubmit: () -> Unit
) {
    var isAgreed by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var profileData by remember { mutableStateOf<Map<String, Any?>?>(null) }

    // Fetch Profile Data
    LaunchedEffect(Unit) {
        val result = SupabaseRepository.getProfileJson(context)
        if (result.isSuccess) {
            profileData = result.getOrNull()
        } else {
            Toast.makeText(context, "Gagal memuat profil: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
        }
        isLoading = false
    }

    val businessName = profileData?.get("business_name") as? String ?: "Nama Bisnis"
    val businessAddress = profileData?.get("address") as? String ?: "Alamat belum diatur"
    val businessDescription = profileData?.get("business_description") as? String ?: "Deskripsi belum diatur"
    val businessCategory = profileData?.get("job_category") as? String ?: "-"
    val businessPhone = SessionManager.getPhoneNumber(context) ?: "-"
    val openTime = profileData?.get("operating_hours_open") as? String ?: "09:00"
    val closeTime = profileData?.get("operating_hours_close") as? String ?: "22:00"
    val coverUrl = profileData?.get("location_photo_front_url") as? String

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Review Profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111813)
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
                            tint = Color(0xFF111813)
                        )
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .shadow(16.dp)
                    .padding(16.dp)
            ) {
                 Button(
                    onClick = {
                        if (!isAgreed) {
                            Toast.makeText(context, "Mohon setujui syarat dan ketentuan", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        scope.launch {
                            val success = SupabaseRepository.completeBusinessRegistration(context)
                            isLoading = false
                            if (success.isSuccess) {
                                onSubmit()
                            } else {
                                val errorMsg = success.exceptionOrNull()?.message ?: "Unknown error"
                                Toast.makeText(context, "Gagal: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isLoading && profileData != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(elevation = 8.dp, spotColor = Color(0xFF13EC5B).copy(0.2f), shape = RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF13EC5B),
                        contentColor = Color(0xFF111813)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF111813)
                        )
                    } else {
                        Text(
                            text = "Selesaikan Pendaftaran",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Pastikan data yang Anda masukkan sudah benar sebelum mengirim pendaftaran.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF6B7280), textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (isLoading && profileData == null) {
             Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF13EC5B))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                     val painter = if (!coverUrl.isNullOrEmpty()) {
                         rememberAsyncImagePainter(coverUrl)
                     } else {
                         rememberAsyncImagePainter("https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80") // Fallback
                     }
                     
                     Image(
                        painter = painter,
                        contentDescription = "Business Cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha=0.8f)),
                                    startY = 200f
                                )
                            )
                    )
                    
                     Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(24.dp)
                    ) {
                         Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = businessName,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Verified",
                                tint = Color(0xFF13EC5B),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFFD1D5DB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = businessAddress,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFE5E7EB)),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Description
                 Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Tentang Bisnis",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = businessDescription,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF4B5563), lineHeight = 24.sp)
                    )
                 }
                 
                 HorizontalDivider(thickness = 8.dp, color = Color(0xFFF9FAFB))
                 
                 // Details List
                 Column(modifier = Modifier.padding(24.dp)) {
                     Text(
                        text = "Detail Bisnis",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF111813)),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    DetailRow(label = "Kategori Industri", value = businessCategory, icon = Icons.Default.Business)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF3F4F6))
                    
                    DetailRow(label = "Nomor Telepon", value = businessPhone, icon = Icons.Default.Phone)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFF3F4F6))

                    DetailRow(label = "Jam Operasional", value = "$openTime - $closeTime", icon = Icons.Outlined.Timer)
                 }
                 
                 HorizontalDivider(thickness = 8.dp, color = Color(0xFFF9FAFB))

                 // Legal and Consent
                  Column(modifier = Modifier.padding(24.dp)) {
                      Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0FDF4), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFDCFCE7), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                            .clickable { isAgreed = !isAgreed }
                    ) {
                        // Checkbox custom
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(if(isAgreed) Color(0xFF16A34A) else Color.White, RoundedCornerShape(6.dp))
                                .border(1.dp, if(isAgreed) Color.Transparent else Color(0xFFD1D5DB), RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if(isAgreed) Icon(Icons.Default.Check, contentDescription=null, tint=Color.White, modifier=Modifier.size(16.dp))
                        }
                        
                        Column {
                            Text(
                                text = "Persetujuan Legal",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                            )
                             Text(
                                text = "Dengan membuat akun, saya menyetujui Syarat Layanan, Kebijakan Privasi, dan Pedoman Perlindungan Pekerja.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF15803D), lineHeight = 18.sp),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                  }
                  
                  Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
             Box(
                modifier = Modifier.background(Color(0xFFF3F4F6), CircleShape).padding(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = label, style = MaterialTheme.typography.labelMedium.copy(color = Color(0xFF9CA3AF)))
                Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = Color(0xFF111813)))
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Edit", tint = Color(0xFF9CA3AF))
    }
}


@Composable
fun BusinessVerificationPendingScreen(
    onNavigateHome: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Polling for verification status
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000) // Check every 5 seconds
            val result = com.example.dwhubfix.data.SupabaseRepository.getProfileJson(context)
            result.onSuccess { profile ->
                val status = profile["verification_status"] as? String
                if (status == "approved" || status == "verified") {
                    onNavigateHome()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glows
        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = (-200).dp)
                .size(320.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        )
         Box(
            modifier = Modifier
                .offset(x = (-100).dp, y = 200.dp)
                .size(250.dp)
                .clip(CircleShape)
                .background(Color.Blue.copy(alpha = 0.1f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 400.dp)
        ) {
            // Central Illustration
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulsing Ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(scale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                )
                
                // Icon Container
                Surface(
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8E4)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha=0.05f), Color.Transparent))),
                        contentAlignment = Alignment.Center
                    ) {
                         Icon(
                            Icons.Default.Business, // Using Business icon for context
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(80.dp)
                        )
                        
                        // Floating Icons
                         Icon(
                            Icons.Default.HourglassTop, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary.copy(alpha=0.4f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(32.dp)
                                .size(24.dp)
                                .rotate(12f)
                        )
                         Icon(
                            Icons.Default.FactCheck, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary.copy(alpha=0.4f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(32.dp)
                                .size(24.dp)
                                .rotate(-12f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Status Chip
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8E4)),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(12.dp)) {
                         Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFFEAB308).copy(alpha=0.3f)))
                         Box(modifier = Modifier.align(Alignment.Center).size(8.dp).clip(CircleShape).background(Color(0xFFEAB308)))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "STATUS: MENUNGGU VERIFIKASI",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Pendaftaran Bisnis Sedang Ditinjau",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = Color(0xFF0F172A)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Tim kami akan memverifikasi dokumen legalitas dan detail bisnis Anda secara manual. Proses ini biasanya memakan waktu 1-2 hari kerja. Kami akan menghubungi Anda via WhatsApp jika diperlukan info tambahan.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF475569)),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            

            
            // Footer
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .alpha(0.6f)
                    .padding(bottom = 24.dp)
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF13EC5B), modifier = Modifier.size(18.dp))
                Text(
                    text = "Powered by BaliWork Security",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF61896F)
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun BusinessRegistrationScreenPreview() {
    DailyWorkerHubTheme {
        BusinessRegistrationScreen({}, {})
    }
}

@Preview
@Composable
fun BusinessBasicProfileScreenPreview() {
    DailyWorkerHubTheme {
        BusinessBasicProfileScreen({}, {})
    }
}

@Preview
@Composable
fun BusinessDocumentUploadScreenPreview() {
    DailyWorkerHubTheme {
        BusinessDocumentUploadScreen({}, {})
    }
}

@Preview
@Composable
fun BusinessLocationVerificationScreenPreview() {
    DailyWorkerHubTheme {
        BusinessLocationVerificationScreen({}, {})
    }
}

@Preview
@Composable
fun BusinessDetailsScreenPreview() {
    DailyWorkerHubTheme {
        BusinessDetailsScreen({}, {})
    }
}

@Preview
@Composable
fun BusinessWorkerPreferenceScreenPreview() {
    DailyWorkerHubTheme {
        BusinessWorkerPreferenceScreen({}, {})
    }
}

@Preview
@Composable
fun BusinessFinalReviewScreenPreview() {
    DailyWorkerHubTheme {
        BusinessFinalReviewScreen({}, {})
    }
}

@Preview
@Composable
fun BusinessVerificationPendingScreenPreview() {
    DailyWorkerHubTheme {
        BusinessVerificationPendingScreen({})
    }
}
