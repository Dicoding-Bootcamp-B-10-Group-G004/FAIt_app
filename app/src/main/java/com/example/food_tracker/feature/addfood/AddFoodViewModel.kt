package com.example.food_tracker.feature.addfood

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.food_tracker.domain.model.Food
import com.example.food_tracker.domain.model.TrackedFood
import com.example.food_tracker.domain.usecase.AddFoodUseCase
import com.example.food_tracker.domain.usecase.GetFoodResultsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddFoodViewModel(
    private val addFoodUseCase: AddFoodUseCase,
    private val getFoodResultsUseCase: GetFoodResultsUseCase
) : ViewModel() {

    var state by mutableStateOf(AddFoodState())
        private set

    private var editingFoodId: String? = null
    private var searchJob: Job? = null

    fun onEvent(event: AddFoodEvent) {
        when (event) {
            is AddFoodEvent.OnSearchQueryChange -> {
                state = state.copy(searchQuery = event.query)
                searchFood(event.query)
            }

            is AddFoodEvent.OnFoodSelected -> {
                state = state.copy(
                    selectedFood = event.food,
                    portion = event.food.portion
                )
            }

            is AddFoodEvent.OnPortionChange -> {
                state = state.copy(portion = event.portion)
            }

            is AddFoodEvent.OnMealChange -> {
                state = state.copy(mealType = event.mealType)
            }

            is AddFoodEvent.OnDateChange -> {
                state = state.copy(date = event.date)
            }

            is AddFoodEvent.OnLoadTrackedFood -> {
                loadTrackedFood(event.id)
            }
            
            is AddFoodEvent.OnReset -> {
                searchJob?.cancel()
                editingFoodId = null
                state = AddFoodState()
            }

            is AddFoodEvent.OnSave -> {
                saveFood()
            }
        }
    }

    private fun loadTrackedFood(id: String) {
        viewModelScope.launch {
            val trackedFood = addFoodUseCase.getTrackedFoodById(id)
            if (trackedFood != null) {
                editingFoodId = id
                
                // Use the search use case to find base food info
                val results = getFoodResultsUseCase(trackedFood.name)
                val baseFood = results.firstOrNull { it.name == trackedFood.name }
                
                state = state.copy(
                    searchQuery = trackedFood.name,
                    selectedFood = baseFood ?: Food(
                        name = trackedFood.name,
                        calories = trackedFood.calories / (trackedFood.portion / 100.0),
                        protein = trackedFood.protein / (trackedFood.portion / 100.0),
                        carbs = trackedFood.carbs / (trackedFood.portion / 100.0),
                        fat = trackedFood.fat / (trackedFood.portion / 100.0),
                        portion = 100.0
                    ),
                    portion = trackedFood.portion,
                    mealType = trackedFood.mealType,
                    date = trackedFood.date
                )
            }
        }
    }

    private fun saveFood() {
        val food = state.selectedFood ?: return

        viewModelScope.launch {
            val multiplier = state.portion / food.portion
            
            val trackedFood = TrackedFood(
                id = editingFoodId ?: UUID.randomUUID().toString(),
                name = food.name,
                calories = food.calories * multiplier,
                protein = food.protein * multiplier,
                carbs = food.carbs * multiplier,
                fat = food.fat * multiplier,
                portion = state.portion,
                mealType = state.mealType,
                date = state.date
            )

            if (editingFoodId != null) {
                addFoodUseCase.updateTrackedFood(trackedFood)
            } else {
                addFoodUseCase.saveFoodToHistory(trackedFood)
            }

            editingFoodId = null
            state = AddFoodState()
        }
    }

    private fun searchFood(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                state = state.copy(searchResults = emptyList(), selectedFood = null)
                return@launch
            }

            delay(300)
            
            state = state.copy(isLoading = true)
            val results = getFoodResultsUseCase(query)
            
            val exactMatch = results.find { it.name.equals(query, ignoreCase = true) }
            val autoSelected = exactMatch ?: results.firstOrNull()

            state = state.copy(
                searchResults = results,
                isLoading = false,
                selectedFood = autoSelected,
                portion = autoSelected?.portion ?: 100.0
            )
        }
    }
}
