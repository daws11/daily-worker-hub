package com.example.dwhubfix.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WorkerVerificationPendingScreen(
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Polling for verification status
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(5000) // Check every 5 seconds
            val result = com.example.dwhubfix.data.SupabaseRepository.getProfileJson(context)
            result.onSuccess { profile ->
                val status = profile.optString("verification_status")
                if (status == "approved" || status == "verified") {
                    onNavigateHome()
                }
            }
        }
    }

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // custom background light
        contentAlignment = Alignment.Center
    ) {
        // Ambient background glows
        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = (-200).dp)
                .size(320.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) // blur done by modifier usually but here simplified
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
            modifier = Modifier.padding(24.dp).widthIn(max = 400.dp)
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
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8E4), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(alpha=0.05f), Color.Transparent)))
                    )
                     Icon(
                        Icons.Default.FactCheck, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(80.dp)
                    )
                    
                    // Floating Icons
                     Icon(
                        Icons.Default.HourglassTop, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary.copy(alpha=0.4f),
                        modifier = Modifier.align(Alignment.TopEnd).padding(32.dp).size(24.dp).rotate(12f)
                    )
                     Icon(
                        Icons.Default.VerifiedUser, 
                        contentDescription = null, 
                        tint = MaterialTheme.colorScheme.primary.copy(alpha=0.4f),
                        modifier = Modifier.align(Alignment.BottomStart).padding(32.dp).size(24.dp).rotate(-12f)
                    )

                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Status Chip
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8E4)),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(12.dp)) {
                         Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFFEAB308).copy(alpha=0.3f))) // Ping animation simulated
                         Box(modifier = Modifier.align(Alignment.Center).size(8.dp).clip(CircleShape).background(Color(0xFFEAB308)))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "STATUS: DALAM PENINJAUAN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Verifikasi Dokumen Anda sedang diproses",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = Color(0xFF0F172A)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Tim kami sedang memeriksa kelengkapan dokumen Anda. Mohon tunggu 2-3 hari kerja. Kami akan memberitahu Anda segera setelah selesai.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF475569)),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            OutlinedButton(
                onClick = { /* Contact Support */ },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                border = BorderStroke(0.dp, Color.Transparent), // Ghost button
            ) {
                 Icon(Icons.Default.Help, contentDescription = null, tint = Color(0xFF475569))
                 Spacer(modifier = Modifier.width(8.dp))
                 Text("Hubungi Bantuan", color = Color(0xFF475569))
            }
            

        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkerVerificationPendingScreenPreview() {
    com.example.dwhubfix.ui.theme.DailyWorkerHubTheme {
        WorkerVerificationPendingScreen({})
    }
}
