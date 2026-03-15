package com.example.food_tracker.feature.profile

data class NutritionState(
    val weight: String = "0",
    val height: String = "0",
    val age: String = "0",
    val isMale: Boolean = true,
    val activityLevel: Double = 1.2,
    val result: String = "0",
    val protein: String = "0g",
    val carbs: String = "0g",
    val fat: String = "0g"
)