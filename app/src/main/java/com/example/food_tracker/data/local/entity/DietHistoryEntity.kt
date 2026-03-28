package com.example.food_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diet_histories")
data class DietHistoryEntity(
    @PrimaryKey val date: String, // yyyy-MM-dd
    val calorieGoal: Double,
    val proteinGoal: Double,
    val carbsGoal: Double,
    val fatGoal: Double
)
