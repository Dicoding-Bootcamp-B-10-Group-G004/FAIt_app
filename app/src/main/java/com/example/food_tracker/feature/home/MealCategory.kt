package com.example.food_tracker.feature.home

import androidx.compose.runtime.Immutable
import com.example.food_tracker.domain.model.TrackedFood

@Immutable
data class MealCategory(
    val name: String,
    val foods: List<TrackedFood>,
    val totalCalories: Int
)
