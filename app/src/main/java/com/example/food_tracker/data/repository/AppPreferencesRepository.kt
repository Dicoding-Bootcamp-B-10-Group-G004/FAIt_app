package com.example.food_tracker.data.repository

import com.example.food_tracker.data.local.UserDataStore
import com.example.food_tracker.domain.model.AppPreferences
import kotlinx.coroutines.flow.Flow

class AppPreferencesRepository(
    private val userDataStore: UserDataStore
) {
    val appPreferencesFlow: Flow<AppPreferences> = userDataStore.appPreferencesFlow

    suspend fun saveLanguageCode(languageCode: String) {
        userDataStore.saveLanguageCode(languageCode)
    }
}
