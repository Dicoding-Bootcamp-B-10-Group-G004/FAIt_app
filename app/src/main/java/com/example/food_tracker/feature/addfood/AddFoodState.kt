package com.example.food_tracker.feature.addfood

import com.example.food_tracker.domain.model.Food
import java.text.SimpleDateFormat
import java.util.*

data class AddFoodState(
    val searchQuery: String = "",
    val searchResults: List<Food> = emptyList(),
    val selectedFood: Food? = null,
    val portion: Double = 100.0,
    val isLoading: Boolean = false,
    val mealType: String = "Lunch",
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val unit: String = "gram"
)
