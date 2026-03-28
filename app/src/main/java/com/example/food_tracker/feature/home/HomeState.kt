package com.example.food_tracker.feature.home

import androidx.compose.runtime.Immutable
import java.text.SimpleDateFormat
import java.util.*

@Immutable
data class HomeState(
    val selectedDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
    val displayDate: String = "Today",
    val isToday: Boolean = true,
    val suppliedCalories: Double = 0.0,
    val proteinCount: Double = 0.0,
    val carbsCount: Double = 0.0,
    val fatCount: Double = 0.0,
    val calorieGoal: Double = 2000.0,
    val proteinGoal: Double = 96.0,
    val carbsGoal: Double = 385.0,
    val fatGoal: Double = 71.0,
    val proteinReached: Boolean = false,
    val carbsReached: Boolean = false,
    val caloriesReached: Boolean = false,
    val categories: List<MealCategory> = emptyList(),
    val isLoading: Boolean = false
)
