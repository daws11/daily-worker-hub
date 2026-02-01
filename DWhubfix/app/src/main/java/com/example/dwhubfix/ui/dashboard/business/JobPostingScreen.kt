package com.example.dwhubfix.ui.dashboard.business

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobPostingScreen() {
    var jobTitle by remember { mutableStateOf("") }
    var jobDescription by remember { mutableStateOf("") }
    var workerCount by remember { mutableStateOf("1") }
    var wage by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    
    val isValid = jobTitle.isNotEmpty() && jobDescription.isNotEmpty() && wage.isNotEmpty() && location.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Buat Lowongan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                 Button(
                    onClick = { /* TODO: Submit to Supabase */ },
                    enabled = isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF13EC5B),
                        disabledContainerColor = Color(0xFFE5E7EB),
                        contentColor = Color(0xFF111813),
                        disabledContentColor = Color(0xFF9CA3AF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Terbitkan Lowongan",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Job Title
            OutlinedTextField(
                value = jobTitle,
                onValueChange = { jobTitle = it },
                label = { Text("Judul Pekerjaan") },
                placeholder = { Text("Contoh: Cuci Piring, Jaga Stand") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF13EC5B),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            OutlinedTextField(
                value = jobDescription,
                onValueChange = { jobDescription = it },
                label = { Text("Deskripsi Pekerjaan") },
                placeholder = { Text("Jelaskan tanggung jawab dan persyaratan...") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF13EC5B),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                ),
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Location
             OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Lokasi") },
                placeholder = { Text("Alamat lengkap tempat kerja") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF13EC5B),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Row for Date and Count
            Row(modifier = Modifier.fillMaxWidth()) {
                // Worker Count
                OutlinedTextField(
                    value = workerCount,
                    onValueChange = { if(it.all { char -> char.isDigit() }) workerCount = it },
                    label = { Text("Jumlah") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Icon(Icons.Default.Group, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF13EC5B),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Date (Simplified text for now)
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Tanggal") },
                    placeholder = { Text("Hari ini") },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF13EC5B),
                        unfocusedBorderColor = Color(0xFFE5E7EB)
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Wage
            OutlinedTextField(
                value = wage,
                onValueChange = { if(it.all { char -> char.isDigit() }) wage = it },
                label = { Text("Upah per Orang") },
                placeholder = { Text("Rp 100.000") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text("Rp", fontWeight = FontWeight.Bold) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF13EC5B),
                    unfocusedBorderColor = Color(0xFFE5E7EB)
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Upah bersih yang akan diterima pekerja.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(100.dp)) // Scroll space
        }
    }
}
