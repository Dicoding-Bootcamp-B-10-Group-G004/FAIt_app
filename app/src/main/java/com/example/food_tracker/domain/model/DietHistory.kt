package com.example.food_tracker.domain.model

data class DietHistory(
    val date: String, // yyyy-MM-dd
    val calorieGoal: Double,
    val proteinGoal: Double,
    val carbsGoal: Double,
    val fatGoal: Double,
    val trackedFoods: List<TrackedFood> = emptyList(),
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val calorieGoalReached: Boolean = false,
    val proteinGoalReached: Boolean = false,
    val carbsGoalReached: Boolean = false,
    val fatGoalReached: Boolean = false
)
