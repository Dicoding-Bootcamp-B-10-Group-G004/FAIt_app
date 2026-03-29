package com.example.food_tracker.domain.usecase


import com.example.food_tracker.domain.model.TrackedFood
import com.example.food_tracker.data.repository.FoodRepositoryImpl


class AddFoodUseCase(
    private val repository: FoodRepositoryImpl
) {

    suspend fun saveFoodToHistory(food: TrackedFood) {
        repository.insertTrackedFood(food)
    }

    suspend fun updateTrackedFood(trackedFood: TrackedFood) {
        repository.updateTrackedFood(trackedFood)
    }

    suspend fun getTrackedFoodById(id: String): TrackedFood? {
        return repository.getTrackedFoodById(id)
    }

}
