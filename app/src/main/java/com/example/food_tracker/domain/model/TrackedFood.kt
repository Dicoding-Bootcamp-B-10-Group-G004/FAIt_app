package com.example.food_tracker.domain.model

import java.util.UUID

data class TrackedFood(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val calories: Double,
    val fat: Double,
    val carbs: Double,
    val protein: Double,
    val portion: Double, // The actual amount consumed in grams
    val mealType: String,
    val date: String // yyyy-MM-dd
)
