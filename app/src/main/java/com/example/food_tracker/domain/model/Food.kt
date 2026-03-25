package com.example.food_tracker.domain.model

data class Food(
    val name: String,
    val calories: Int,
    val fat: String,
    val carbs: String,
    val protein: String,
    val unit: String, // Untuk kolom 'satuan' di CSV lo
    val mealType: String = "Lunch"
)