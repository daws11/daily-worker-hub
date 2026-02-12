package com.example.dwhubfix.ui.dashboard.business

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BusinessProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Header / Cover Code (Simplified from previous screens)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF111813)) // Dark background placeholder
        ) {
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
                       text = "Nama Bisnis Anda",
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
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            // Stats Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileStat("Posts", "0")
                ProfileStat("Hires", "0")
                ProfileStat("Rating", "5.0")
            }

            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = { /* TODO */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = Color(0xFF111813)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profil")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Details
            ProfileDetailItem(Icons.Default.Business, "Kategori", "F&B")
            ProfileDetailItem(Icons.Default.LocationOn, "Alamat", "Jl. Contoh No. 123")
            ProfileDetailItem(Icons.Default.Phone, "Telepon", "08123456789")
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = { /* TODO: Logout logic */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFDC2626)),
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keluar / Logout")
                }
            }
        }
    }
}

@Composable
fun ProfileStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        Text(label, style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
    }
}

@Composable
fun ProfileDetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).background(Color(0xFFF3F4F6), CircleShape), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Color.Gray)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
            Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        }
    }
}
