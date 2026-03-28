package com.example.food_tracker.domain.usecase

import com.example.food_tracker.domain.model.Goal
import com.example.food_tracker.domain.model.UserProfile
import kotlin.math.max
import kotlin.math.pow

data class NutritionResult(
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val bmi: Double,
    val bmiStatus: String
)

class CalculateNutritionUseCase {
    operator fun invoke(profile: UserProfile): NutritionResult {
        if (profile.weight <= 0.0 || profile.height <= 0.0 || profile.age <= 0) {
            return NutritionResult(0.0, 0.0, 0.0, 0.0, 0.0, "Unknown")
        }

        // 1. Mifflin-St Jeor BMR
        val s = if (profile.isMale) 5 else -161
        val bmr = (10 * profile.weight) + (6.25 * profile.height) - (5 * profile.age) + s

        // 2. TDEE
        var tdee = bmr * profile.activityLevel

        // 3. Goal adjustment
        tdee = when (profile.goal) {
            Goal.LOSE -> tdee - 500
            Goal.MAINTAIN -> tdee
            Goal.GAIN -> tdee + 500
        }
        val finalCalories = max(1200.0, tdee) // Minimum safe calories

        // 4. Macros
        val protein = (finalCalories * 0.20) / 4
        val carbs = (finalCalories * 0.50) / 4
        val fat = (finalCalories * 0.30) / 9

        // 5. BMI
        val heightInMeters = profile.height / 100.0
        val bmi = profile.weight / heightInMeters.pow(2)
        val bmiStatus = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Normal Weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }

        return NutritionResult(
            calories = finalCalories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            bmi = bmi,
            bmiStatus = bmiStatus
        )
    }
}
