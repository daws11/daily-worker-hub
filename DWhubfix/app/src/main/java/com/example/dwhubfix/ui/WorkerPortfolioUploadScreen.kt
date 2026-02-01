package com.example.dwhubfix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dwhubfix.data.SupabaseRepository
import kotlinx.coroutines.launch
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkerPortfolioUploadScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State
    // List of uploads: Name, Size, Status (Uploading/Done), Uri/Url
    data class UploadItem(val name: String, val size: String, var status: String, val uri: Uri, val url: String? = null)
    val uploadedFiles = remember { mutableStateListOf<UploadItem>() }
    
    val fileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
             val timestamp = System.currentTimeMillis()
             uploadedFiles.add(UploadItem("Doc_$timestamp", "Unknown Size", "Uploading...", uri))
             
             scope.launch {
                 val result = SupabaseRepository.uploadFile(context, uri, "portfolio")
                 if (result.isSuccess) {
                     val publicUrl = result.getOrNull()
                     val index = uploadedFiles.indexOfFirst { it.uri == uri }
                     if (index != -1) {
                         uploadedFiles[index] = uploadedFiles[index].copy(status = "Completed", url = publicUrl)
                     }
                     } else {
                         val index = uploadedFiles.indexOfFirst { it.uri == uri }
                         if (index != -1) {
                             uploadedFiles[index] = uploadedFiles[index].copy(status = "Failed")
                         }
                         val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                         Toast.makeText(context, "Upload failed: $errorMsg", Toast.LENGTH_LONG).show()
                     }
             }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Portfolio & Certificates",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) }
            )
        },
        bottomBar = {
             Surface(
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                     Button(
                        onClick = {
                            if (uploadedFiles.any { it.status == "Uploading..." }) {
                                Toast.makeText(context, "Please wait for uploads to complete", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            scope.launch {
                                val validUrls = uploadedFiles.mapNotNull { it.url }
                                val result = SupabaseRepository.updateWorkerPortfolio(context, validUrls)
                                
                                if (result.isSuccess) {
                                    onNavigateNext()
                                } else {
                                    Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Update Profile", fontWeight = FontWeight.Bold, color = Color(0xFF102216))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                             scope.launch {
                                SupabaseRepository.updateProfile(context, currentStep = "worker_verification_pending")
                            }
                            onNavigateNext()
                        },
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("Skip for now", color = Color.Gray, fontWeight = FontWeight.SemiBold)
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
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Showcase Your Skills",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp)
            )
            Text(
                "Upload photos of your past work or certificates to stand out to potential employers.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
            )
            
            // Upload Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)) // Dashed if possible
                    .clickable { fileLauncher.launch(arrayOf("*/*")) }
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha=0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                         Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tap to upload", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Support: JPG, PNG, PDF (Max 5MB)", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    Spacer(modifier = Modifier.height(16.dp))
                     Button(
                        onClick = { fileLauncher.launch(arrayOf("*/*")) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text("Browse Files", color = Color(0xFF102216), fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            // Uploads List
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Your Uploads", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(
                    "${uploadedFiles.size} Files", 
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.Gray),
                    modifier = Modifier.background(Color(0xFFF1F5F9), RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            
            uploadedFiles.forEach { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                         Icon(Icons.Default.Description, contentDescription = null, tint = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(file.name, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("${file.size} • ${file.status}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                    }
                    IconButton(onClick = { uploadedFiles.remove(file) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkerPortfolioUploadScreenPreview() {
    com.example.dwhubfix.ui.theme.DailyWorkerHubTheme {
        WorkerPortfolioUploadScreen({}, {})
    }
}
