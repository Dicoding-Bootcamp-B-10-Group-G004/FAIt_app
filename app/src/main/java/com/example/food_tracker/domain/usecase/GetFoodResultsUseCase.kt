package com.example.food_tracker.domain.usecase

import com.example.food_tracker.data.repository.FoodRepositoryImpl
import com.example.food_tracker.domain.model.Food
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetFoodResultsUseCase(
    private val repository: FoodRepositoryImpl
) {
    suspend operator fun invoke(query: String): List<Food> = withContext(Dispatchers.Default) {
        if (query.isBlank()) return@withContext emptyList()
        repository.searchFood(query)
    }
}
