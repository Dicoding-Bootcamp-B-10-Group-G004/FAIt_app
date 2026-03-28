package com.example.food_tracker.feature.profile

import com.example.food_tracker.domain.model.Goal

data class NutritionState(
    val weight: String = "",
    val height: String = "",
    val age: String = "",
    val isMale: Boolean = true,
    val activityLevel: Double = 1.2,
    val goal: Goal = Goal.MAINTAIN,
    val calorieGoal: String = "0",
    val proteinGoal: String = "0g",
    val carbsGoal: String = "0g",
    val fatGoal: String = "0g",
    val bmi: String = "0.0",
    val bmiStatus: String = "Unknown",
    val languageCode: String = "en"
)
