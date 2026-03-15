package com.example.food_tracker.feature.profile

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_tracker.data.local.UserDataStore
import com.example.food_tracker.domain.model.UserProfile
import com.example.food_tracker.domain.usecase.CalculateNutritionUseCase
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userDataStore: UserDataStore,
    private val calculateNutritionUseCase: CalculateNutritionUseCase = CalculateNutritionUseCase()
) : ViewModel() {

    var state by mutableStateOf(NutritionState())
        private set

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            userDataStore.userProfileFlow.collect { profile ->
                // Hanya update jika berat > 0 (menandakan data sudah pernah diisi)
                if (profile.weight > 0.0) {
                    state = state.copy(
                        weight = profile.weight.toString(),
                        height = profile.height.toString(),
                        age = profile.age.toString(),
                        isMale = profile.isMale,
                        activityLevel = profile.activityLevel
                    )
                    calculateResult()
                }
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
        }
        calculateResult()
    }

    private fun calculateResult() {
        try {
            val w = state.weight.toDoubleOrNull() ?: 0.0
            val h = state.height.toDoubleOrNull() ?: 0.0
            val a = state.age.toIntOrNull() ?: 0

            // Jangan panggil usecase kalau input belum valid
            if (w <= 0.0 || h <= 0.0 || a <= 0) {
                resetNutritionResults()
                return
            }

            val profile = UserProfile(w, h, a, state.isMale, state.activityLevel)
            val totalCalories = calculateNutritionUseCase(profile)

            if (totalCalories <= 0.0 || totalCalories.isNaN() || totalCalories.isInfinite()) {
                resetNutritionResults()
                return
            }

            state = state.copy(
                result = String.format("%.0f", totalCalories),
                protein = String.format("%.0fg", (totalCalories * 0.20) / 4),
                carbs = String.format("%.0fg", (totalCalories * 0.50) / 4),
                fat = String.format("%.0fg", (totalCalories * 0.30) / 9)
            )
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Crash di calculateResult: ${e.message}")
            resetNutritionResults()
        }
    }

    private fun resetNutritionResults() {
        state = state.copy(result = "0", protein = "0g", carbs = "0g", fat = "0g")
    }

    fun saveToLocal() {
        viewModelScope.launch {
            try {
                val w = state.weight.toDoubleOrNull() ?: 0.0
                val h = state.height.toDoubleOrNull() ?: 0.0
                val a = state.age.toIntOrNull() ?: 0

                if (w > 0 && h > 0 && a > 0) {
                    userDataStore.saveProfile(w, h, a, state.isMale, state.activityLevel)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Gagal simpan: ${e.message}")
            }
        }
    }
}