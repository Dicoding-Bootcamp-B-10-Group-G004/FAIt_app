package com.example.food_tracker.domain.model

data class Food(
    val name: String,
    val calories: Double, 
    val fat: Double,
    val carbs: Double,
    val protein: Double,
    val portion: Double // base portion size from CSV column 'angka_satuan'
)
