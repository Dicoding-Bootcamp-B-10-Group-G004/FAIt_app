package com.example.food_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_foods")
data class TrackedFoodEntity(
    @PrimaryKey val id: String,
    val name: String,
    val calories: Double,
    val fat: Double,
    val carbs: Double,
    val protein: Double,
    val portion: Double,
    val mealType: String,
    val date: String
)
