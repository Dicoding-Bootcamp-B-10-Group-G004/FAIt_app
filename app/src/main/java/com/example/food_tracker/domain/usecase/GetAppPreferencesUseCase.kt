package com.example.food_tracker.domain.usecase

import com.example.food_tracker.data.repository.AppPreferencesRepository
import com.example.food_tracker.domain.model.AppPreferences
import kotlinx.coroutines.flow.Flow

class GetAppPreferencesUseCase(
    private val repository: AppPreferencesRepository
) {
    operator fun invoke(): Flow<AppPreferences> = repository.appPreferencesFlow
}
