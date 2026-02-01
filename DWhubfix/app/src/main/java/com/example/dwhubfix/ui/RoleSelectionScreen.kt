package com.example.dwhubfix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.dwhubfix.ui.theme.DailyWorkerHubTheme
import com.example.dwhubfix.ui.theme.Primary

@Composable
fun RoleSelectionScreen(
    onWorkerSelected: () -> Unit,
    onBusinessSelected: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // background-light
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Brand
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Spa,
                        contentDescription = "Logo",
                        tint = Primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "BaliWork",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color(0xFF111813)
                )
            }

            // Language Selector
            Surface(
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable { },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "EN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF374151) // gray-700
                    )
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Select Language",
                        tint = Color(0xFF6B7280), // gray-500
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Headline
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp)) {
            Text(
                text = "Halo! Who are you?",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = Color(0xFF111813),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Select your profile to continue.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = Color(0xFF61896F)
            )
        }

        // Cards Section
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            RoleCard(
                title = "Saya adalah Pekerja Harian",
                subtitle = "Find flexible jobs in hotels & cafes",
                badge = "Jobs",
                badgeBackgroundColor = Primary,
                badgeTextColor = Color(0xFF111813),
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAzQSxHFEWoXgwhAfuMgIFX2MIAwTbZwQkOsZ_CTbOdVbqEWBuCunnH1cI53404uSBxKytcOZkwHxdvlZMDyyFC283cSgGr467rEDgiRqC1p-BkN1nFxtQwV4q_HRyj4aMdZRb58vDmtGEgk6AuHblUFktvfhrgJ5-J4M_IVcvFGj0lMikKG16vz3Celd1HbOZ23LQX_hO9qkegeihmQCOYK2v7TXgOJpb-2W9wIg8yyfnV_qxAOHfv4OUsWn4gbK_mfSxKoanoZmw",
                onClick = onWorkerSelected
            )

            RoleCard(
                title = "Saya adalah Pemilik Bisnis",
                subtitle = "Hire reliable staff instantly",
                badge = "Hire",
                badgeBackgroundColor = Color.White,
                badgeTextColor = Color(0xFF111813),
                imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC6IL3eQtv3JyoWbH94Ox3KdfHfOEOYFpa70rJTdGbD4UbmW1uYPeVHXxTy12YoKQzD6hNobs7NlvhbxFRC1Xfdkuca7-LTn_6QyGpNnBlNTvNkVrUCrOZLIeSqdhdWEfdS2ENdYI3U4S_R2LCXymnp7kP01VR6W804wRwQEC4HW_Or55rXPMsxXB_J-hrBkyOGbI_eJ6m9qqcLaDublGMZI1YY63Qnwsz41jEAlWW2scCHVIsjzR6sBDLMVaBC43fmvFiJ6ExJXns",
                onClick = onBusinessSelected
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF6B7280) // gray-500
                )
                Text(
                    text = "Log in",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Primary,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Text(
                    text = "Terms of Service",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = Color(0xFF9CA3AF), // gray-400
                    modifier = Modifier.clickable { /* TODO */ }
                )
                Text(
                    text = "Privacy Policy",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = Color(0xFF9CA3AF), // gray-400
                    modifier = Modifier.clickable { /* TODO */ }
                )
            }
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeBackgroundColor: Color,
    badgeTextColor: Color,
    imageUrl: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = Color.Transparent, // Transparent by default, could animate to Primary on hover/focus if needed
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Column {
            // Image Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFE5E7EB))
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Badge
                Surface(
                    color = badgeBackgroundColor,
                    contentColor = badgeTextColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp) // bottom-3 left-4 equivalent approx
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Content
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = Color(0xFF111813)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color(0xFF61896F)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background), // background-light
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF6B7280), // gray-500
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RoleSelectionScreenPreview() {
    DailyWorkerHubTheme {
        RoleSelectionScreen({}, {}, {})
    }
}
