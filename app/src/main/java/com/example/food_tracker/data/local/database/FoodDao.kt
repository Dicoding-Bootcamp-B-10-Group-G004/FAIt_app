package com.example.food_tracker.data.local.database

import androidx.room.*
import com.example.food_tracker.data.local.entity.DietHistoryEntity
import com.example.food_tracker.data.local.entity.TrackedFoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    // Tracked Food
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackedFood(food: TrackedFoodEntity)

    @Delete
    suspend fun deleteTrackedFood(food: TrackedFoodEntity)

    @Query("SELECT * FROM tracked_foods WHERE date = :date")
    fun getTrackedFoodsByDate(date: String): Flow<List<TrackedFoodEntity>>

    @Query("SELECT * FROM tracked_foods WHERE id = :id")
    suspend fun getTrackedFoodById(id: String): TrackedFoodEntity?

    @Query("SELECT * FROM tracked_foods")
    fun getAllTrackedFoods(): Flow<List<TrackedFoodEntity>>

    // Diet History
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDietHistory(history: DietHistoryEntity)

    @Update
    suspend fun updateDietHistory(history: DietHistoryEntity)

    @Query("SELECT * FROM diet_histories WHERE date = :date")
    suspend fun getDietHistoryByDate(date: String): DietHistoryEntity?

    @Query("SELECT * FROM diet_histories WHERE date = :date")
    fun getDietHistoryByDateFlow(date: String): Flow<DietHistoryEntity?>

    @Query("SELECT * FROM diet_histories")
    fun getAllDietHistories(): Flow<List<DietHistoryEntity>>
}
