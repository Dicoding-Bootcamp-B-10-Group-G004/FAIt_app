package com.example.food_tracker.feature.addfood

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_tracker.domain.model.Food
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
                state = state.copy(selectedFood = event.food)
            }

            is AddFoodEvent.OnPortionChange -> {
                state = state.copy(portion = event.portion)
            }

            is AddFoodEvent.OnMealChange -> { // ✅ FIX
                state = state.copy(mealType = event.mealType)
            }

            is AddFoodEvent.OnSave -> {
                saveFood()
            }
        }
    }

    private fun saveFood() {
        val food = state.selectedFood ?: return

        viewModelScope.launch {
            val updatedFood = food.copy(
                calories = food.calories * state.portion,
                protein = ((food.protein.toDoubleOrNull() ?: 0.0) * state.portion).toString(),
                carbs = ((food.carbs.toDoubleOrNull() ?: 0.0) * state.portion).toString(),
                fat = ((food.fat.toDoubleOrNull() ?: 0.0) * state.portion).toString()
            )

            addFoodUseCase.saveFoodToHistory(updatedFood)

            state = AddFoodState() // reset
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