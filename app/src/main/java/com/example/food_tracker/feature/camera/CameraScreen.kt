package com.example.food_tracker.feature.camera

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.food_tracker.data.ml.FoodDetector
import com.example.food_tracker.feature.home.HomeViewModel
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    homeViewModel: HomeViewModel,
    onImageCaptured: (Bitmap) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lemonWhite = Color(0xFFFFFDF0)

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when (viewModel.screenState) {
            CameraScreenState.Camera, CameraScreenState.Searching -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(foodDetector = viewModel.foodDetector)

                    // Real-time overlay
                    DetectionOverlay(detections = viewModel.liveDetections)

                    // UI Overlay
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top Bar
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onClose,
                                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            ) {
                                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                            }
                            Text("Scan Food", color = Color.White, fontWeight = FontWeight.Bold)
                            IconButton(onClick = {}, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                                Icon(Icons.Rounded.FlashOn, contentDescription = "Flash", tint = Color.White)
                            }
                        }

                        // Bottom Controls
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                                .padding(bottom = 40.dp, top = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Shutter Button
                                Button(
                                    onClick = {
                                        if (viewModel.liveDetections.isNotEmpty()) {
                                            viewModel.startSnapping()
                                        } else {
                                            Toast.makeText(context, "No detections found to snap", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (viewModel.screenState == CameraScreenState.Searching)
                                            MaterialTheme.colorScheme.tertiaryContainer
                                        else lemonWhite
                                    ),
                                    modifier = Modifier.size(80.dp)
                                ) {
                                    if (viewModel.screenState == CameraScreenState.Searching) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            strokeWidth = 3.dp
                                        )
                                    } else {
                                        Icon(Icons.Rounded.PhotoCamera, contentDescription = "Capture", tint = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            CameraScreenState.Result -> {
                viewModel.snappedResult?.let { result ->
                    DetectionResultScreen(
                        snappedResult = result,
                        homeViewModel = homeViewModel,
                        onBack = { viewModel.reset() },
                        onConfirm = onImageCaptured
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPreview(foodDetector: FoodDetector) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    foodDetector.detect(imageProxy)
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}
