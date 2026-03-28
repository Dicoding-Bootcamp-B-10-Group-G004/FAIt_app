package com.example.food_tracker.domain.usecase

import com.example.food_tracker.data.repository.AppPreferencesRepository

class SetLanguageUseCase(
    private val repository: AppPreferencesRepository
) {
    suspend operator fun invoke(languageCode: String) {
        repository.saveLanguageCode(languageCode)
    }
}
