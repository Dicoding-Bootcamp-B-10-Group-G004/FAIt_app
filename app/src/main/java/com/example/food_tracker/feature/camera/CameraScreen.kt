package com.example.food_tracker.feature.camera

import android.util.Log
import android.widget.Toast
import androidx.camera.core.Camera
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
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.food_tracker.R
import com.example.food_tracker.core.ui.components.FTIconButton
import com.example.food_tracker.data.ml.FoodDetector
import com.example.food_tracker.feature.home.HomeViewModel
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    homeViewModel: HomeViewModel,
    navController: NavController,
    onNavigateToAddFood: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val themeColors = MaterialTheme.colorScheme

    // Observe result from AddFoodScreen using StateFlow or livedata via SavedStateHandle
    val foodAdded = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("food_added", false)
        ?.collectAsState()

    val foodAddedSuccessMsg = stringResource(R.string.food_added_success)
    val noObjectDetectedMsg = stringResource(R.string.no_object_detected)

    LaunchedEffect(foodAdded?.value) {
        if (foodAdded?.value == true) {
            Toast.makeText(context, foodAddedSuccessMsg, Toast.LENGTH_SHORT).show()
            navController.currentBackStackEntry?.savedStateHandle?.set("food_added", false)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when (viewModel.screenState) {
            CameraScreenState.Camera,
            CameraScreenState.Searching -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    CameraPreview(
                        foodDetector = viewModel.foodDetector,
                        isFlashEnabled = viewModel.isFlashEnabled
                    )
                    DetectionOverlay(detections = viewModel.liveDetections)

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // TOP BAR
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FTIconButton(
                                icon = Icons.Rounded.Close,
                                onClick = onClose,
                                contentDescription = stringResource(R.string.close),
                                containerColor = Color.Black.copy(alpha = 0.5f),
                                contentColor = Color.White
                            )
                            Text(
                                text = stringResource(R.string.scan_food),
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            FTIconButton(
                                icon = if (viewModel.isFlashEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                                onClick = { viewModel.toggleFlash() },
                                contentDescription = stringResource(R.string.flash),
                                containerColor = if (viewModel.isFlashEnabled) themeColors.primary.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.5f),
                                contentColor = if (viewModel.isFlashEnabled) themeColors.onPrimary else Color.White
                            )
                        }

                        // BOTTOM CONTROLS
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color.Black.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                                )
                                .padding(bottom = 40.dp, top = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    if (viewModel.liveDetections.isNotEmpty()) {
                                        viewModel.startSnapping()
                                    } else {
                                        Toast.makeText(context, noObjectDetectedMsg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (viewModel.screenState == CameraScreenState.Searching)
                                        themeColors.primary else themeColors.background
                                ),
                                modifier = Modifier.size(80.dp)
                            ) {
                                if (viewModel.screenState == CameraScreenState.Searching) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = themeColors.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.PhotoCamera,
                                        contentDescription = stringResource(R.string.capture),
                                        tint = themeColors.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
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
                        onFoodClick = { food ->
                            onNavigateToAddFood(food.name)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    foodDetector: FoodDetector,
    isFlashEnabled: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    var camera by remember { mutableStateOf<Camera?>(null) }

    LaunchedEffect(isFlashEnabled) {
        camera?.cameraControl?.enableTorch(isFlashEnabled)
    }

    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
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
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                    camera?.cameraControl?.enableTorch(isFlashEnabled)
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}
