package com.example.food_tracker.feature.camera

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_tracker.data.ml.Detection
import com.example.food_tracker.data.ml.FoodDetector
import com.example.food_tracker.data.ml.SnappedResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class CameraScreenState {
    Camera, Searching, Result
}

class CameraViewModel(context: Context) : ViewModel() {

    private val availableModels = context.assets.list("")?.filter { it.endsWith(".tflite") } ?: emptyList()
    
    val selectedModelPath = if (availableModels.contains("yolo26n_320_int8.tflite")) "yolo26n_320_int8.tflite"
                            else availableModels.firstOrNull() ?: ""

    var screenState by mutableStateOf(CameraScreenState.Camera)
        private set

    var liveDetections by mutableStateOf<List<Detection>>(emptyList())
        private set

    var liveInferenceTime by mutableLongStateOf(0L)
        private set

    var snappedResult by mutableStateOf<SnappedResult?>(null)
        private set

    var isFlashEnabled by mutableStateOf(false)
        private set

    val foodDetector = FoodDetector(context, selectedModelPath)

    init {
        viewModelScope.launch {
            foodDetector.setup()

            // Observe real-time results
            launch {
                foodDetector.detectionResult.collectLatest { result ->
                    if (screenState != CameraScreenState.Result) {
                        liveDetections = result.detections
                        liveInferenceTime = result.inferenceTime
                    }
                }
            }

            // Observe automatic snap results
            launch {
                foodDetector.snappedResult.collectLatest { result ->
                    snappedResult = result
                    screenState = CameraScreenState.Result
                }
            }
        }
    }

    fun toggleFlash() {
        isFlashEnabled = !isFlashEnabled
    }

    fun startSnapping() {
        if (liveDetections.isNotEmpty()) {
            screenState = CameraScreenState.Searching
            foodDetector.isSnapping = true
        }
    }

    fun reset() {
        screenState = CameraScreenState.Camera
        snappedResult = null
        liveDetections = emptyList()
        foodDetector.isSnapping = false
    }

    override fun onCleared() {
        super.onCleared()
        foodDetector.close()
    }
}
