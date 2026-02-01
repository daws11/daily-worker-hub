package com.example.dwhubfix.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.dwhubfix.data.SessionManager
import com.example.dwhubfix.data.SupabaseRepository
import kotlinx.coroutines.delay

@Composable
fun VerificationProcessingScreen(
    accessToken: String?,
    refreshToken: String?,
    onVerificationSuccess: (String) -> Unit, // Returns the role
    onVerificationFailed: (String) -> Unit
) {
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf("Verifying your account...") }

    LaunchedEffect(accessToken, refreshToken) {
        if (!accessToken.isNullOrEmpty() || !refreshToken.isNullOrEmpty()) {
            val result = SupabaseRepository.handleAuthRedirect(context, accessToken, refreshToken)
            if (result.isSuccess) {
                statusMessage = "Verified! Redirecting..."
                delay(1000) // Small delay for UX
                val role = SessionManager.getPendingRole(context) ?: "worker" // Default to worker if missing
                onVerificationSuccess(role)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                statusMessage = "Verification failed: $error"
                delay(2000)
                onVerificationFailed(error)
            }
        } else {
            statusMessage = "Invalid link: No tokens found."
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = Color(0xFF102216))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = statusMessage)
        }
    }
}
