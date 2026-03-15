package com.example.food_tracker.feature.camera

import androidx.camera.core.ImageCaptureException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import java.util.concurrent.Executor

@Composable
fun CameraScreen(
    onImageCaptured: (Bitmap) -> Unit, // FIX: Sekarang menerima Bitmap
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // Setup ImageCapture agar bisa ambil foto
    val imageCapture = remember { ImageCapture.Builder().build() }
    val lemonWhite = Color(0xFFFFFDF0)

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // 1. Camera Preview
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture // Tambahkan imageCapture di sini
                        )
                    } catch (e: Exception) {
                        Log.e("CameraScreen", "Binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. UI Overlay
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

            // Frame Fokus
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.CenterHorizontally)
                    .border(BorderStroke(2.dp, lemonWhite.copy(alpha = 0.5f)), RoundedCornerShape(24.dp))
            )

            // Bottom Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .padding(bottom = 40.dp, top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Place food inside the frame", color = lemonWhite, modifier = Modifier.padding(bottom = 16.dp))

                    // Tombol Shutter dengan Logic Ambil Foto
                    Button(
                        onClick = {
                            takePhoto(imageCapture, ContextCompat.getMainExecutor(context), onImageCaptured)
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = lemonWhite),
                        modifier = Modifier.size(80.dp)
                    ) {
                        Icon(Icons.Rounded.PhotoCamera, contentDescription = "Capture", tint = Color.Black)
                    }
                }
            }
        }
    }
}

// Fungsi helper untuk mengambil foto dan convert ke Bitmap
private fun takePhoto(
    imageCapture: ImageCapture,
    executor: Executor,
    onImageCaptured: (Bitmap) -> Unit
) {
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            image.close()
            onImageCaptured(bitmap) // Kirim bitmap ke MainActivity -> ViewModel
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraScreen", "Capture failed", exception)
        }
    })
}