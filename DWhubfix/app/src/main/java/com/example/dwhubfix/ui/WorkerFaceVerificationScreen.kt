package com.example.dwhubfix.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.dwhubfix.data.SupabaseRepository
import com.example.dwhubfix.ui.theme.DailyWorkerHubTheme
import com.example.dwhubfix.ui.theme.Primary
import com.example.dwhubfix.utils.FaceDetectorAnalyzer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

// Define Verification Steps
enum class VerificationStep(val instruction: String, val progress: Float) {
    NEUTRAL("Look straight at the camera", 0.1f),
    BLINK("Please Blink Your Eyes", 0.3f),
    LOOK_LEFT("Turn Head Left", 0.5f),
    LOOK_RIGHT("Turn Head Right", 0.7f),
    LOOK_UP("Look Up", 0.8f),
    LOOK_DOWN("Look Down", 0.9f),
    COMPLETE("Verification Complete!", 1.0f)
}

@Composable
fun WorkerFaceVerificationScreen(
    onNavigateBack: () -> Unit,
    onVerificationSuccess: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Toast.makeText(context, "Camera permission needed for verification", Toast.LENGTH_SHORT).show()
                onNavigateBack() // Go back if denied
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Verification State
    var currentStep by remember { mutableStateOf(VerificationStep.NEUTRAL) }
    var isFaceDetected by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }

    // Use cases
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }
    
    // Auto-capture logic when COMPLETE
    LaunchedEffect(currentStep) {
        if (currentStep == VerificationStep.COMPLETE && !isUploading) {
            isUploading = true
            // Small delay to show complete state before capture
            delay(500)
            
            // Capture Image
            val photoFile = File(context.cacheDir, "face_verification_${System.currentTimeMillis()}.jpg")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e("FaceVerification", "Photo capture failed: ${exc.message}", exc)
                        isUploading = false
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        // Upload to Supabase
                        scope.launch {
                            val result = SupabaseRepository.uploadVerificationImage(context, Uri.fromFile(photoFile))
                            if (result.isSuccess) {
                                val url = result.getOrNull() as? String
                                if (url != null) {
                                    val updateResult = SupabaseRepository.updateProfile(
                                        context = context,
                                        avatarUrl = url,
                                        currentStep = "worker_address_verification"
                                    )
                                    if (updateResult.isSuccess) {
                                        withContext(Dispatchers.Main) {
                                            onVerificationSuccess()
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                            isUploading = false
                                        }
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                                    isUploading = false
                                }
                            }
                        }
                    }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF111813)
                    )
                }
                Text(
                    text = "Face Verification",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF111813)
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (hasCameraPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Context Badge
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Identity Verification",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4B5563)
                            )
                        )
                    }
                }

                // Camera Viewport
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(280.dp)
                ) {
                    // Progress Ring
                    val progress by animateFloatAsState(
                         targetValue = currentStep.progress * 360f,
                         animationSpec = tween(500, easing = FastOutSlowInEasing),
                         label = "progress"
                    )
                    
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFFE5E7EB),
                            style = Stroke(width = 4.dp.toPx())
                        )
                        drawArc(
                            color = Primary,
                            startAngle = -90f,
                            sweepAngle = progress, 
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Camera Preview
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6))
                            .border(4.dp, Color.White, CircleShape)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                val previewView = PreviewView(ctx)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = CameraPreview.Builder().build()
                                    preview.setSurfaceProvider(previewView.surfaceProvider)

                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                    
                                    imageAnalysis.setAnalyzer(executor, FaceDetectorAnalyzer(
                                        onFaceDetected = { faces ->
                                            if (faces.isNotEmpty()) {
                                                isFaceDetected = true
                                                val face = faces.first()
                                                
                                                // Liveness Logic State Machine
                                                when(currentStep) {
                                                    VerificationStep.NEUTRAL -> {
                                                        // Wait for stable face looking straight
                                                        if (face.headEulerAngleY < 10 && face.headEulerAngleY > -10 &&
                                                            face.headEulerAngleX < 10 && face.headEulerAngleX > -10) {
                                                            // Move to next step after a moment? 
                                                            // For now, immediate transition if stable
                                                            currentStep = VerificationStep.BLINK
                                                        }
                                                    }
                                                    VerificationStep.BLINK -> {
                                                        val leftEye = face.leftEyeOpenProbability ?: 1f
                                                        val rightEye = face.rightEyeOpenProbability ?: 1f
                                                        if (leftEye < 0.1f && rightEye < 0.1f) {
                                                            currentStep = VerificationStep.LOOK_LEFT
                                                        }
                                                    }
                                                    VerificationStep.LOOK_LEFT -> {
                                                        // Turning left usually corresponds to positive Y or negative Y depending on mirroring
                                                        // Commonly: -Y is Right, +Y is Left (from camera user perspective)
                                                        // But front camera is mirrored usually.
                                                        // Let's accept > 25 degrees.
                                                        if (face.headEulerAngleY > 25) { 
                                                            currentStep = VerificationStep.LOOK_RIGHT
                                                        }
                                                    }
                                                    VerificationStep.LOOK_RIGHT -> {
                                                        if (face.headEulerAngleY < -25) {
                                                            currentStep = VerificationStep.LOOK_UP
                                                        }
                                                    }
                                                    VerificationStep.LOOK_UP -> {
                                                         if (face.headEulerAngleX > 15) {
                                                             currentStep = VerificationStep.LOOK_DOWN
                                                         }
                                                    }
                                                    VerificationStep.LOOK_DOWN -> {
                                                        if (face.headEulerAngleX < -15) {
                                                            currentStep = VerificationStep.COMPLETE
                                                        }
                                                    }
                                                    VerificationStep.COMPLETE -> {
                                                        // Done
                                                    }
                                                }
                                            } else {
                                                isFaceDetected = false
                                            }
                                        },
                                        onSimpleFeedback = { _, _ -> } // Unused now
                                    ))

                                    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            cameraSelector,
                                            preview,
                                            imageAnalysis,
                                            imageCapture
                                        )
                                    } catch (e: Exception) {
                                        Log.e("FaceVerification", "Use case binding failed", e)
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                                previewView
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Face Guide Outline with Pulse
                         val pulseTransition = rememberInfiniteTransition(label = "pulse")
                         val pulseAlpha by pulseTransition.animateFloat(
                             initialValue = 0.3f,
                             targetValue = 0.7f,
                             animationSpec = infiniteRepeatable(
                                 animation = tween(1000),
                                 repeatMode = RepeatMode.Reverse
                             ),
                             label = "pulseAlpha"
                         )

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val outlineColor = if (currentStep == VerificationStep.COMPLETE) Primary else Color.White
                            
                            drawOval(
                                color = outlineColor.copy(alpha = if (isFaceDetected) 0.8f else pulseAlpha),
                                topLeft = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.15f),
                                size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.7f),
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            )
                        }
                        
                        // Scanning Animation
                        androidx.compose.animation.AnimatedVisibility(
                            visible = isFaceDetected && currentStep != VerificationStep.COMPLETE,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            val infiniteTransition = rememberInfiniteTransition(label = "scanning")
                            val scanOffset by infiniteTransition.animateFloat(
                                initialValue = -0.3f,
                                targetValue = 1.3f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(2000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                ), label = "scanOffset"
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .graphicsLayer {
                                            translationY = 260.dp.toPx() * scanOffset
                                        }
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Primary.copy(alpha = 0.5f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )
                            }
                        }

                        // Success Overlay
                         androidx.compose.animation.AnimatedVisibility(
                            visible = currentStep == VerificationStep.COMPLETE,
                            enter = scaleIn() + fadeIn(),
                            exit = fadeOut()
                         ) {
                             Box(
                                 modifier = Modifier.fillMaxSize().background(Primary.copy(alpha = 0.2f)),
                                 contentAlignment = Alignment.Center
                             ) {
                                 Icon(
                                     imageVector = Icons.Default.Check,
                                     contentDescription = "Verified",
                                     tint = Primary,
                                     modifier = Modifier
                                        .size(64.dp)
                                        .background(Color.White, CircleShape)
                                        .padding(12.dp)
                                 )
                             }
                         }

                        if (isUploading && currentStep == VerificationStep.COMPLETE) {
                             Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.3f)), contentAlignment = Alignment.Center) {
                                 CircularProgressIndicator(color = Color.White)
                             }
                        }
                    }
                    
                    // Step Badge/Instruction Overlay on Camera
                    if (currentStep != VerificationStep.COMPLETE && currentStep != VerificationStep.NEUTRAL) {
                         Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .background(Color.Black.copy(alpha=0.6f), CircleShape)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = currentStep.instruction.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Main Instructions
                AnimatedContent(
                    targetState = if (currentStep == VerificationStep.COMPLETE) "Verification Complete!" else currentStep.instruction,
                    label = "text"
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111813),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                Text(
                    text = "Follow the instructions to verify your identity.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WorkerFaceVerificationScreenPreview() {
    DailyWorkerHubTheme {
        WorkerFaceVerificationScreen({}, {})
    }
}
