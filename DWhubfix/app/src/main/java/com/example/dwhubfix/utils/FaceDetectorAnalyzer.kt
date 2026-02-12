package com.example.dwhubfix.utils

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceDetectorAnalyzer(
    private val onFaceDetected: (List<Face>) -> Unit,
    private val onSimpleFeedback: (Boolean, Boolean) -> Unit // (isFaceDetected, isSmiling)
) : ImageAnalysis.Analyzer {

    // Configure Face Detector
    // High accuracy for verification, enable smile classification
    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL) // For smile
        .setMinFaceSize(0.15f)
        .build()

    private val detector = FaceDetection.getClient(options)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            detector.process(image)
                .addOnSuccessListener { faces ->
                    onFaceDetected(faces)
                    
                    if (faces.isNotEmpty()) {
                        val face = faces.first()
                        // Check attributes
                        val smileProb = face.smilingProbability ?: 0f
                        val isSmiling = smileProb > 0.5f
                        
                        onSimpleFeedback(true, isSmiling)
                    } else {
                        onSimpleFeedback(false, false)
                    }
                }
                .addOnFailureListener { e ->
                    // Handle error (optional log)
                    onSimpleFeedback(false, false)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
