package com.example.food_tracker.feature.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_tracker.data.repository.ProfileRepository
import com.example.food_tracker.domain.model.UserProfile
import com.example.food_tracker.domain.usecase.CalculateNutritionUseCase
import com.example.food_tracker.domain.usecase.GetAppPreferencesUseCase
import com.example.food_tracker.domain.usecase.SetLanguageUseCase
import kotlinx.coroutines.launch
import java.util.Locale

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val setLanguageUseCase: SetLanguageUseCase,
    private val getAppPreferencesUseCase: GetAppPreferencesUseCase,
    private val calculateNutritionUseCase: CalculateNutritionUseCase = CalculateNutritionUseCase()
) : ViewModel() {

    var state by mutableStateOf(NutritionState())
        private set

    init {
        loadProfileData()
        loadAppPreferences()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            profileRepository.userProfileFlow.collect { profile ->
                if (profile.weight > 0.0) {
                    state = state.copy(
                        weight = profile.weight.toString(),
                        height = profile.height.toString(),
                        age = profile.age.toString(),
                        isMale = profile.isMale,
                        activityLevel = profile.activityLevel,
                        goal = profile.goal
                    )
                    calculateResult()
                }
            }
        }
    }

    private fun loadAppPreferences() {
        viewModelScope.launch {
            getAppPreferencesUseCase().collect { prefs ->
                state = state.copy(languageCode = prefs.languageCode)
            }
        }
    }

    fun onEvent(event: ProfileEvent) {
        state = when(event) {
            is ProfileEvent.WeightChanged -> state.copy(weight = event.weight)
            is ProfileEvent.HeightChanged -> state.copy(height = event.height)
            is ProfileEvent.AgeChanged -> state.copy(age = event.age)
            is ProfileEvent.GenderChanged -> state.copy(isMale = event.isMale)
            is ProfileEvent.ActivityLevelChanged -> state.copy(activityLevel = event.level)
            is ProfileEvent.GoalChanged -> state.copy(goal = event.goal)
        }
        calculateResult()
    }

    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            setLanguageUseCase(languageCode)
        }
    }

    private fun calculateResult() {
        try {
            val w = state.weight.toDoubleOrNull() ?: 0.0
            val h = state.height.toDoubleOrNull() ?: 0.0
            val a = state.age.toIntOrNull() ?: 0

            if (w <= 0.0 || h <= 0.0 || a <= 0) {
                resetNutritionResults()
                return
            }

            val profile = UserProfile(w, h, a, state.isMale, state.activityLevel, state.goal)
            val result = calculateNutritionUseCase(profile)

            state = state.copy(
                calorieGoal = String.format(Locale.getDefault(), "%.0f", result.calories),
                proteinGoal = String.format(Locale.getDefault(), "%.0fg", result.protein),
                carbsGoal = String.format(Locale.getDefault(), "%.0fg", result.carbs),
                fatGoal = String.format(Locale.getDefault(), "%.0fg", result.fat),
                bmi = String.format(Locale.getDefault(), "%.1f", result.bmi),
                bmiStatus = result.bmiStatus
            )
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Crash di calculateResult: ${e.message}")
            resetNutritionResults()
        }
    }

    private fun resetNutritionResults() {
        state = state.copy(
            calorieGoal = "0",
            proteinGoal = "0g",
            carbsGoal = "0g",
            fatGoal = "0g",
            bmi = "0.0",
            bmiStatus = "Unknown"
        )
    }

    fun saveToLocal() {
        viewModelScope.launch {
            try {
                val w = state.weight.toDoubleOrNull() ?: 0.0
                val h = state.height.toDoubleOrNull() ?: 0.0
                val a = state.age.toIntOrNull() ?: 0

                if (w > 0 && h > 0 && a > 0) {
                    profileRepository.saveProfile(
                        UserProfile(w, h, a, state.isMale, state.activityLevel, state.goal)
                    )
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Gagal simpan: ${e.message}")
            }
        }
    }
    
    fun getActivityLevelResId(level: Double): Int {
        return profileRepository.getActivityLevelResId(level)
    }
}
