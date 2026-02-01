package com.example.dwhubfix.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.dwhubfix.data.SupabaseRepository
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import com.example.dwhubfix.ui.theme.Primary
import androidx.compose.ui.tooling.preview.Preview
import com.example.dwhubfix.ui.theme.DailyWorkerHubTheme

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun WorkerAddressVerificationScreen(
    onNavigateBack: () -> Unit,
    onVerificationSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Initialize OSM Configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
    }
    
    // State for location
    var currentGeoPoint by remember { mutableStateOf<GeoPoint?>(null) } // Default Bali approx
    var addressText by remember { mutableStateOf("Detecting address...") }
    var mapView: MapView? by remember { mutableStateOf(null) }
    
    // State for uploads
    var photoEvidenceUri by remember { mutableStateOf<Uri?>(null) }
    var domicileDocUri by remember { mutableStateOf<Uri?>(null) }
    var isSaving by remember { mutableStateOf(false) }

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

    // Image Picker Launchers
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        photoEvidenceUri = uri
    }

    val docLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        domicileDocUri = uri
    }
    
    // Initial Location Check
    LaunchedEffect(Unit) {
         if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
             getLocation(context) { geoPoint ->
                currentGeoPoint = geoPoint
                addressText = "Lat: ${geoPoint.latitude}, Lng: ${geoPoint.longitude}"
                // MapView might not be ready yet, will be handled in AndroidView update or separate effect if needed
             }
         }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Verify Address",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                actions = { Spacer(modifier = Modifier.width(48.dp)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                         if (currentGeoPoint == null) {
                             Toast.makeText(context, "Please capture your location first", Toast.LENGTH_SHORT).show()
                             return@Button
                        }
                        
                        isSaving = true
                        scope.launch {
                             try {
                                 var photoUrl: String? = null
                                 var documentUrl: String? = null
                                 
                                 // 1. Upload photos
                                 if (photoEvidenceUri != null) {
                                     val photoResult = SupabaseRepository.uploadFile(context, photoEvidenceUri!!, "address_verification")
                                     if (photoResult.isSuccess) {
                                         photoUrl = photoResult.getOrNull()
                                     } else {
                                         val errorMsg = photoResult.exceptionOrNull()?.message ?: "Upload failed"
                                         Toast.makeText(context, "Photo upload failed: $errorMsg", Toast.LENGTH_SHORT).show()
                                         isSaving = false
                                         return@launch
                                     }
                                 }
                                 
                                 if (domicileDocUri != null) {
                                     val docResult = SupabaseRepository.uploadFile(context, domicileDocUri!!, "address_verification")
                                     if (docResult.isSuccess) {
                                         documentUrl = docResult.getOrNull()
                                     }
                                 }

                                 // 2. Update Profile
                                 val result = SupabaseRepository.updateWorkerAddress(
                                     context,
                                     address = addressText, 
                                     latitude = currentGeoPoint!!.latitude,
                                     longitude = currentGeoPoint!!.longitude,
                                     photoUrl = photoUrl,
                                     documentUrl = documentUrl,
                                     currentStep = "worker_skill_selection"
                                 )

                                 if (result.isSuccess) {
                                      onVerificationSuccess()
                                 } else {
                                     val errorMsg = result.exceptionOrNull()?.message ?: "Update failed"
                                     Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                     isSaving = false
                                 }
                             } catch (e: Exception) {
                                 Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                 isSaving = false
                             }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color(0xFF003314)
                    )
                ) {
                     if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Verify & Continue", fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Confirm Your Location",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp)
            )
            Text(
                "Please verify your current address to receive job offers nearby in Bali.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            // Map Container (OSM)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
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
                
                // Center Helper Icon (Use this if we allow dragging map to pick location)
                 Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Center",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center).size(40.dp).offset(y = (-20).dp)
                )
            }

            // Current Address Display
            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("CURRENT ADDRESS", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                    Text(
                        addressText,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Capture Location Button
            Button(
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
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Capture Current Location", fontWeight = FontWeight.Bold)
            }

            Divider(modifier = Modifier.padding(vertical = 24.dp), color = Color.LightGray)

            // Photo Evidence
            Text("Photo Evidence", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(12.dp))
            
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { galleryLauncher.launch("image/*") },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF0F2F4)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoEvidenceUri != null) {
                            AsyncImage(
                                model = photoEvidenceUri,
                                contentDescription = "Selected Image",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Color.Black)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (photoEvidenceUri != null) "Photo Selected" else "Take Photo of Location",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            "Front of the building/house",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }

            // Document Upload
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Proof of Domicile", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.width(8.dp))
                Text("(Optional)", style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
            }
            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { docLauncher.launch("application/pdf") }, 
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.LightGray),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        if (domicileDocUri != null) "Document Selected" else "Upload Surat Keterangan Domisili",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        textAlign = TextAlign.Center
                    )
                    Text("PDF, JPG or PNG", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                }
            }

            // Privacy Notice
            Spacer(modifier = Modifier.height(32.dp))
            Surface(
                color = Color(0xFFEFF6FF), 
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 100.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "We value your privacy. Your location data is encrypted and only used to match you with nearby jobs. It is never shared publicly.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF637588))
                    )
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

@Preview(showBackground = true)
@Composable
fun WorkerAddressVerificationScreenPreview() {
    DailyWorkerHubTheme {
        WorkerAddressVerificationScreen({}, {})
    }
}
