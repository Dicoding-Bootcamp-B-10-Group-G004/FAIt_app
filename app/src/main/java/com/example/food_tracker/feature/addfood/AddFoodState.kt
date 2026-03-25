package com.example.food_tracker.feature.addfood

import com.example.food_tracker.domain.model.Food

data class AddFoodState(
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val selectedFood: Food? = null,
    val portion: Int = 1,
    val isLoading: Boolean = false,
    val mealType: String = "Lunch",
    val unit: String = "gram"
)