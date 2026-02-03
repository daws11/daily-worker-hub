package com.example.dwhubfix.ui.dashboard.worker

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import com.example.dwhubfix.data.SupabaseRepository
import com.example.dwhubfix.model.UserProfile
import com.example.dwhubfix.ui.theme.Primary

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Fetch User Profile
        // Assuming SupabaseRepository.getCurrentProfile() or similar exists
        // For now using getProfile() pattern which likely checks session
        scope.launch {
            try {
                // TODO: Verify repository method name. Using placehodoer fetch if needed.
                val result = SupabaseRepository.getProfile(context)
                if (result.isSuccess) {
                    userProfile = result.getOrNull()
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                isLoading = false
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Apakah Anda yakin ingin keluar dari akun?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            SupabaseRepository.signOut(context)
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Keluar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8F6))
            .verticalScroll(rememberScrollState())
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            ProfileHeader(userProfile)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ProfileStats(userProfile)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            SettingsSection(onLogoutClick = { showLogoutDialog = true })
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileHeader(userProfile: UserProfile?) {
    val name = userProfile?.fullName ?: "Pengguna"
    val avatarUrl = userProfile?.avatarUrl
    val rating = 5.0 // Placeholder until implemented in backend

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE5E7EB)),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Surface(
            color = Color(0xFFFFF7ED),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", rating),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                )
            }
        }
    }
}

@Composable
fun ProfileStats(userProfile: UserProfile?) {
   val jobsCompleted = 0 // Placeholder
   // Placeholder data for other stats as schema might not have them yet
   // Assuming we can calculate or fetch them
   val hoursWorked = jobsCompleted * 5 // Mock calculation
   val reliability = "100%"

   Row(
       modifier = Modifier
           .fillMaxWidth()
           .padding(horizontal = 16.dp),
       horizontalArrangement = Arrangement.spacedBy(12.dp)
   ) {
       StatCard(
           label = "Pekerjaan",
           value = jobsCompleted.toString(),
           icon = Icons.Default.Work,
           modifier = Modifier.weight(1f)
       )
       StatCard(
           label = "Jam Kerja",
           value = hoursWorked.toString(),
           icon = Icons.Default.Schedule,
           modifier = Modifier.weight(1f)
       )
       StatCard(
           label = "Reliabilitas",
           value = reliability,
           icon = Icons.Default.ThumbUp,
           modifier = Modifier.weight(1f)
       )
   }
}

@Composable
fun StatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280))
        }
    }
}

@Composable
fun SettingsSection(onLogoutClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Pengaturan",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                SettingsItem(icon = Icons.Default.Person, title = "Edit Profil", onClick = onNavigateToEditProfile)
                HorizontalDivider(color = Color(0xFFF3F4F6))
                SettingsItem(icon = Icons.Default.Notifications, title = "Notifikasi", onClick = onNavigateToNotifications)
                HorizontalDivider(color = Color(0xFFF3F4F6))
                SettingsItem(icon = Icons.Default.Language, title = "Bahasa", value = "Indonesia", onClick = {})
                HorizontalDivider(color = Color(0xFFF3F4F6))
                SettingsItem(icon = Icons.Default.Help, title = "Bantuan & Dukungan", onClick = {})
                HorizontalDivider(color = Color(0xFFF3F4F6))
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp, 
                    title = "Keluar", 
                    textColor = Color.Red,
                    iconColor = Color.Red,
                    onClick = onLogoutClick
                )
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector, 
    title: String, 
    value: String? = null,
    textColor: Color = Color.Black,
    iconColor: Color = Color(0xFF6B7280),
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, color = textColor, modifier = Modifier.weight(1f))
        
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        if (value == null && title != "Keluar") {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(20.dp))
        }
    }
}
