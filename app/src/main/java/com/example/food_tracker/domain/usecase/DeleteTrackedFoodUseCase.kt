package com.example.food_tracker.domain.usecase

import com.example.food_tracker.data.repository.FoodRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeleteTrackedFoodUseCase(
    private val repository: FoodRepositoryImpl
) {
    suspend operator fun invoke(foodId: String) = withContext(Dispatchers.IO) {
        repository.deleteTrackedFood(foodId)
    }
}
