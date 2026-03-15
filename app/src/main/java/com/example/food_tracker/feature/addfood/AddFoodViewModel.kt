package com.example.food_tracker.feature.addfood

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_tracker.domain.usecase.AddFoodUseCase
import kotlinx.coroutines.launch

class AddFoodViewModel(
    private val addFoodUseCase: AddFoodUseCase
) : ViewModel() {

    var state by mutableStateOf(AddFoodState())
        private set

    fun onEvent(event: AddFoodEvent) {
        when (event) {
            is AddFoodEvent.OnSearchQueryChange -> {
                state = state.copy(searchQuery = event.query)
                searchFood(event.query)
            }
            is AddFoodEvent.OnFoodSelected -> {
                viewModelScope.launch {
                    // Simpan data ke DataStore melalui UseCase
                    addFoodUseCase.saveFoodToHistory(event.food)

                    // Reset query setelah berhasil klik biar search bar bersih lagi
                    state = state.copy(searchQuery = "", searchResults = emptyList())
                }
            }
        }
    }

    fun addFoodWithPortion(food: com.example.food_tracker.domain.model.Food, portion: Int) {

        viewModelScope.launch {

            val updatedFood = food.copy(
                calories = food.calories * portion,
                protein = (food.protein.toDoubleOrNull() ?: 0.0 * portion).toString(),
                carbs = (food.carbs.toDoubleOrNull() ?: 0.0 * portion).toString(),
                fat = (food.fat.toDoubleOrNull() ?: 0.0 * portion).toString()
            )

            addFoodUseCase.saveFoodToHistory(updatedFood)
        }
    }

    private fun searchFood(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                state = state.copy(searchResults = emptyList())
                return@launch
            }

            state = state.copy(isLoading = true)
            val results = addFoodUseCase.searchFoodFromCsv(query)
            state = state.copy(
                searchResults = results,
                isLoading = false
            )
        }
    }
}