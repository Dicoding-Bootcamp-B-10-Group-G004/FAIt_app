package com.example.food_tracker.domain.usecase

import android.graphics.Bitmap
import com.example.food_tracker.domain.model.Food
import com.example.food_tracker.data.repository.FoodRepositoryImpl
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class AddFoodUseCase(
    private val repository: FoodRepositoryImpl
) {
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    // Fungsi untuk simpan ke DataStore
    suspend fun saveFoodToHistory(food: Food) {
        repository.insertFoodHistory(food)
    }

    // Fungsi baru untuk search dari CSV
    fun searchFoodFromCsv(query: String): List<Food> {
        return repository.searchFood(query)
    }

    // Klasifikasi AI tetap aman di sini
    fun classifyImage(bitmap: Bitmap, onSuccess: (String) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        labeler.process(image)
            .addOnSuccessListener { labels ->
                val foodName = labels.firstOrNull()?.text ?: "Unknown Food"
                onSuccess(foodName)
            }
            .addOnFailureListener { onSuccess("Unknown Food") }
    }
}