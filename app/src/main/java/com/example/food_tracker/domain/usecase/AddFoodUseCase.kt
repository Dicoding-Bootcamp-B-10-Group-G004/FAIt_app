package com.example.food_tracker.domain.usecase

import android.graphics.Bitmap
import com.example.food_tracker.domain.model.Food
import com.example.food_tracker.domain.model.TrackedFood
import com.example.food_tracker.data.repository.FoodRepositoryImpl
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class AddFoodUseCase(
    private val repository: FoodRepositoryImpl
) {
    private val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

    suspend fun saveFoodToHistory(food: TrackedFood) {
        repository.insertTrackedFood(food)
    }

    suspend fun updateTrackedFood(trackedFood: TrackedFood) {
        repository.updateTrackedFood(trackedFood)
    }

    suspend fun getTrackedFoodById(id: String): TrackedFood? {
        return repository.getTrackedFoodById(id)
    }

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
