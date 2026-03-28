package com.example.food_tracker.data.repository

import android.content.Context
import com.example.food_tracker.data.local.UserDataStore
import com.example.food_tracker.data.local.database.FoodDao
import com.example.food_tracker.data.local.entity.DietHistoryEntity
import com.example.food_tracker.data.local.entity.TrackedFoodEntity
import com.example.food_tracker.domain.model.Food
import com.example.food_tracker.domain.model.TrackedFood
import com.example.food_tracker.domain.model.DietHistory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext

class FoodRepositoryImpl(
    private val context: Context,
    private val userDataStore: UserDataStore,
    private val dao: FoodDao
) {
    private var cachedFoodList: List<Food>? = null
    private var cachedFoodMap: Map<String, Food>? = null

    val trackedFoods: Flow<List<TrackedFood>> = dao.getAllTrackedFoods()
        .map { entities -> entities.map { it.toTrackedFood() } }
        .flowOn(Dispatchers.IO)

    fun getDietHistoryFlow(date: String): Flow<DietHistory> {
        return combine(
            dao.getDietHistoryByDateFlow(date),
            dao.getTrackedFoodsByDate(date)
        ) { historyEntity, trackedEntities ->
            val trackedFoods = trackedEntities.map { it.toTrackedFood() }
            
            var totalCals = 0.0
            var totalProtein = 0.0
            var totalCarbs = 0.0
            var totalFat = 0.0
            
            for (food in trackedFoods) {
                totalCals += food.calories
                totalProtein += food.protein
                totalCarbs += food.carbs
                totalFat += food.fat
            }

            val calorieGoal = historyEntity?.calorieGoal ?: 2000.0
            val proteinGoal = historyEntity?.proteinGoal ?: 96.0
            val carbsGoal = historyEntity?.carbsGoal ?: 385.0
            val fatGoal = historyEntity?.fatGoal ?: 71.0

            DietHistory(
                date = date,
                calorieGoal = calorieGoal,
                proteinGoal = proteinGoal,
                carbsGoal = carbsGoal,
                fatGoal = fatGoal,
                trackedFoods = trackedFoods,
                totalCalories = totalCals,
                totalProtein = totalProtein,
                totalCarbs = totalCarbs,
                totalFat = totalFat,
                calorieGoalReached = totalCals >= calorieGoal,
                proteinGoalReached = totalProtein >= proteinGoal,
                carbsGoalReached = totalCarbs >= carbsGoal,
                fatGoalReached = totalFat >= fatGoal
            )
        }.onStart {
            ensureDietHistoryForDate(date)
        }.flowOn(Dispatchers.IO)
    }

    fun getAllDietHistoriesFlow(): Flow<List<DietHistory>> {
        return combine(
            dao.getAllDietHistories(),
            dao.getAllTrackedFoods()
        ) { historyEntities, trackedEntities ->
            val trackedByDate = trackedEntities.groupBy { it.date }
            
            historyEntities.map { historyEntity ->
                val foods = trackedByDate[historyEntity.date] ?: emptyList()
                val trackedFoods = foods.map { it.toTrackedFood() }
                
                val totalCals = trackedFoods.sumOf { it.calories }
                val totalProtein = trackedFoods.sumOf { it.protein }
                val totalCarbs = trackedFoods.sumOf { it.carbs }
                val totalFat = trackedFoods.sumOf { it.fat }

                DietHistory(
                    date = historyEntity.date,
                    calorieGoal = historyEntity.calorieGoal,
                    proteinGoal = historyEntity.proteinGoal,
                    carbsGoal = historyEntity.carbsGoal,
                    fatGoal = historyEntity.fatGoal,
                    trackedFoods = trackedFoods,
                    totalCalories = totalCals,
                    totalProtein = totalProtein,
                    totalCarbs = totalCarbs,
                    totalFat = totalFat,
                    calorieGoalReached = totalCals >= historyEntity.calorieGoal,
                    proteinGoalReached = totalProtein >= historyEntity.proteinGoal,
                    carbsGoalReached = totalCarbs >= historyEntity.carbsGoal,
                    fatGoalReached = totalFat >= historyEntity.fatGoal
                )
            }.sortedByDescending { it.date }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun ensureDietHistoryForToday() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        ensureDietHistoryForDate(today)
    }

    suspend fun insertTrackedFood(trackedFood: TrackedFood) {
        ensureDietHistoryForDate(trackedFood.date)
        dao.insertTrackedFood(trackedFood.toEntity())
        updateDailyTotals(trackedFood.date)
    }

    suspend fun deleteTrackedFood(id: String) {
        val entity = dao.getTrackedFoodById(id)
        if (entity != null) {
            dao.deleteTrackedFood(entity)
            updateDailyTotals(entity.date)
        }
    }

    suspend fun updateTrackedFood(updatedFood: TrackedFood) {
        ensureDietHistoryForDate(updatedFood.date)
        dao.insertTrackedFood(updatedFood.toEntity())
        updateDailyTotals(updatedFood.date)
    }

    suspend fun getTrackedFoodById(id: String): TrackedFood? {
        return dao.getTrackedFoodById(id)?.toTrackedFood()
    }

    private suspend fun ensureDietHistoryForDate(date: String) {
        val existing = dao.getDietHistoryByDate(date)
        if (existing == null) {
            val calorieGoal = userDataStore.getCalorieGoal().first() ?: 2000.0
            val proteinGoal = userDataStore.getProteinGoal().first() ?: 96.0
            val carbsGoal = userDataStore.getCarbsGoal().first() ?: 385.0
            val fatGoal = userDataStore.getFatGoal().first() ?: 71.0
            
            dao.insertDietHistory(
                DietHistoryEntity(
                    date = date,
                    calorieGoal = calorieGoal,
                    proteinGoal = proteinGoal,
                    carbsGoal = carbsGoal,
                    fatGoal = fatGoal
                )
            )
        }
    }

    suspend fun updateDietHistoryGoalsForDate(date: String, calories: Double, protein: Double, carbs: Double, fat: Double) {
        val existing = dao.getDietHistoryByDate(date)
        if (existing != null) {
            dao.updateDietHistory(existing.copy(
                calorieGoal = calories,
                proteinGoal = protein,
                carbsGoal = carbs,
                fatGoal = fat
            ))
        } else {
            dao.insertDietHistory(
                DietHistoryEntity(
                    date = date,
                    calorieGoal = calories,
                    proteinGoal = protein,
                    carbsGoal = carbs,
                    fatGoal = fat
                )
            )
        }
    }

    private suspend fun updateDailyTotals(date: String) {
        val todaysFoods = dao.getTrackedFoodsByDate(date).first()
        
        var totalCals = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0
        
        for (f in todaysFoods) {
            totalCals += f.calories
            totalProtein += f.protein
            totalCarbs += f.carbs
            totalFat += f.fat
        }

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        if (date == today) {
            userDataStore.saveSuppliedCals(totalCals)
            userDataStore.saveProtein(totalProtein)
            userDataStore.saveCarbs(totalCarbs)
            userDataStore.saveFat(totalFat)
        }
    }

    suspend fun getDietHistory(date: String): DietHistory {
        return getDietHistoryFlow(date).first()
    }

    suspend fun searchFood(query: String): List<Food> = withContext(Dispatchers.Default) {
        if (query.isBlank()) return@withContext emptyList()
        val allFoods = getAllFoodFromCsv()
        allFoods.filter { it.name.contains(query, ignoreCase = true) }
    }

    suspend fun getFoodByName(name: String): Food? = withContext(Dispatchers.Default) {
        if (cachedFoodMap == null) {
            getAllFoodFromCsv()
        }
        cachedFoodMap?.get(name.lowercase())
    }

    suspend fun getAllFoodFromCsv(): List<Food> = withContext(Dispatchers.IO) {
        cachedFoodList?.let { return@withContext it }

        val foodList = ArrayList<Food>(2000) // Optimization: use ArrayList with initial capacity
        val foodMap = HashMap<String, Food>(2000)
        try {
            val inputStream = context.assets.open("hasil_gizi_100gram.csv")
            val reader = BufferedReader(InputStreamReader(inputStream), 8192) // Optimization: Buffer size
            reader.readLine() // Skip header
            
            reader.forEachLine { line ->
                if (line.isNotBlank()) {
                    val tokens = if (line.contains("\"")) {
                        line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                    } else {
                        line.split(",")
                    }

                    if (tokens.size >= 7) {
                        val name = tokens[0].trim().removeSurrounding("\"")
                        val calories = tokens[1].trim().replace(",", ".").toDoubleOrNull() ?: 0.0
                        val fat = tokens[2].trim().replace(",", ".").toDoubleOrNull() ?: 0.0
                        val carbs = tokens[3].trim().replace(",", ".").toDoubleOrNull() ?: 0.0
                        val protein = tokens[4].trim().replace(",", ".").toDoubleOrNull() ?: 0.0
                        val portion = tokens[6].trim().replace(",", ".").toDoubleOrNull() ?: 100.0
                        
                        val food = Food(name, calories, fat, carbs, protein, portion)
                        foodList.add(food)
                        foodMap[name.lowercase()] = food
                    }
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        cachedFoodList = foodList
        cachedFoodMap = foodMap
        foodList
    }

    private fun TrackedFoodEntity.toTrackedFood() = TrackedFood(
        id = id,
        name = name,
        calories = calories,
        fat = fat,
        carbs = carbs,
        protein = protein,
        portion = portion,
        mealType = mealType,
        date = date
    )

    private fun TrackedFood.toEntity() = TrackedFoodEntity(
        id = id,
        name = name,
        calories = calories,
        fat = fat,
        carbs = carbs,
        protein = protein,
        portion = portion,
        mealType = mealType,
        date = date
    )
}
