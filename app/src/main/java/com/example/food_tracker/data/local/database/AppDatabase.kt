package com.example.food_tracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.food_tracker.data.local.entity.DietHistoryEntity
import com.example.food_tracker.data.local.entity.TrackedFoodEntity

@Database(
    entities = [TrackedFoodEntity::class, DietHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val foodDao: FoodDao
}
