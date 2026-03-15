package com.example.food_tracker.feature.addfood

import com.example.food_tracker.domain.model.Food

sealed class AddFoodEvent {
    data class OnSearchQueryChange(val query: String) : AddFoodEvent()
    data class OnFoodSelected(val food: Food) : AddFoodEvent()
}