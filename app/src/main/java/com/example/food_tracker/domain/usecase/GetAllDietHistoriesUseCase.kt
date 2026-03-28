package com.example.food_tracker.domain.usecase

import com.example.food_tracker.data.repository.FoodRepositoryImpl
import com.example.food_tracker.domain.model.DietHistory
import kotlinx.coroutines.flow.Flow

class GetAllDietHistoriesUseCase(
    private val repository: FoodRepositoryImpl
) {
    operator fun invoke(): Flow<List<DietHistory>> {
        return repository.getAllDietHistoriesFlow()
    }
}
