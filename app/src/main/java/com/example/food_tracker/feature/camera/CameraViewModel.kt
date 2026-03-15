package com.example.food_tracker.feature.camera

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_tracker.data.ml.FoodDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CameraViewModel(context: Context) : ViewModel() {

    private val detector = FoodDetector(context)

    fun analyzeImage(bitmap: Bitmap, onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            // Proses deteksi di background thread
            val foodName = detector.detect(bitmap)

            withContext(Dispatchers.Main) {
                // Kirim hasil kembali ke UI thread
                onResult(foodName)
            }
        }
    }
}