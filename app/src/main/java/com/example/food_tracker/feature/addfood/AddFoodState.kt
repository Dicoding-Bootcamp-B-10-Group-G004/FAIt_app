package com.example.food_tracker.feature.addfood

import com.example.food_tracker.domain.model.Food

data class AddFoodState(
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val isLoading: Boolean = false
)