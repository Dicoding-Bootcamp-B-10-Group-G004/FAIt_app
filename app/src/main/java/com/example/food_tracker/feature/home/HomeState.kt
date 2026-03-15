package com.example.food_tracker.feature.home

data class HomeState(
    val suppliedCalories: Double = 0.0,
    val proteinCount: Double = 0.0,
    val carbsCount: Double = 0.0,
    val fatCount: Double = 0.0,
    val calorieGoal: Double = 2567.0,
    val isScanning: Boolean = false // Tambahan buat loading kamera nanti
)