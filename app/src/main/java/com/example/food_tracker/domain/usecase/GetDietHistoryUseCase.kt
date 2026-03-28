package com.example.food_tracker.domain.usecase

import com.example.food_tracker.data.repository.FoodRepositoryImpl
import com.example.food_tracker.domain.model.DietHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class GetDietHistoryUseCase(
    private val repository: FoodRepositoryImpl
) {
    operator fun invoke(date: String): Flow<DietHistory> {
        return repository.getDietHistoryFlow(date)
            .flowOn(Dispatchers.Default)
    }
}
