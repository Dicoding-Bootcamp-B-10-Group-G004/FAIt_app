package com.example.food_tracker.domain.model

data class UserProfile(
    val weight: Double,
    val height: Double,
    val age: Int,
    val isMale: Boolean,
    val activityLevel: Double,
    val goal: Goal = Goal.MAINTAIN
)

enum class Goal {
    LOSE, MAINTAIN, GAIN
}
