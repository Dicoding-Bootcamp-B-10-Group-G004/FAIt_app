package com.example.food_tracker.data.repository

import android.content.Context
import com.example.food_tracker.data.local.UserDataStore
import com.example.food_tracker.domain.model.Food
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlinx.coroutines.flow.first // Pastikan ini ada untuk ambil data snapshot

private val foodHistory = mutableListOf<Food>()
class FoodRepositoryImpl(
    private val context: Context,
    private val userDataStore: UserDataStore
) {

    /**
     * Fungsi untuk menambahkan gizi makanan yang dipilih ke total harian di DataStore
     * Menggunakan akumulasi (Nilai Lama + Nilai Baru)
     */
    suspend fun insertFoodHistory(food: Food) {
        // 1. Ambil gizi dari makanan yang baru di klik (+)
        // Konversi dari model Food (biasanya String/Int) ke Double
        val newCalories = food.calories.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
        val newProtein = food.protein.replace(",", ".").toDoubleOrNull() ?: 0.0
        val newCarbs = food.carbs.replace(",", ".").toDoubleOrNull() ?: 0.0
        val newFat = food.fat.replace(",", ".").toDoubleOrNull() ?: 0.0

        // 2. AMBIL NILAI LAMA dari DataStore (SnapShot nilai sekarang)
        // Kita panggil getter yang baru kita buat di UserDataStore
        val currentCalories = userDataStore.getSuppliedCals().first() ?: 0.0
        val currentProtein = userDataStore.getProtein().first() ?: 0.0
        val currentCarbs = userDataStore.getCarbs().first() ?: 0.0
        val currentFat = userDataStore.getFat().first() ?: 0.0

        // 3. SIMPAN HASIL PENJUMLAHAN
        saveSuppliedData(
            calories = currentCalories + newCalories,
            protein = currentProtein + newProtein,
            carbs = currentCarbs + newCarbs,
            fat = currentFat + newFat
        )

        foodHistory.add(food)
    }

    suspend fun getFoodHistory(): List<Food> {
        return foodHistory
    }

    /**
     * Fungsi dasar untuk menulis data gizi ke DataStore
     */
    suspend fun saveSuppliedData(
        calories: Double,
        protein: Double,
        carbs: Double,
        fat: Double
    ) {
        userDataStore.saveSuppliedCals(calories)
        userDataStore.saveProtein(protein)
        userDataStore.saveCarbs(carbs)
        userDataStore.saveFat(fat)
    }

    /**
     * Fungsi untuk membaca semua data gizi dari file CSV
     */
    fun getAllFoodFromCsv(): List<Food> {
        val foodList = mutableListOf<Food>()

        try {
            val inputStream = context.assets.open("hasil_gizi_100gram.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))

            reader.readLine() // Skip header

            reader.forEachLine { line ->
                val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())

                if (tokens.size >= 5) {
                    foodList.add(
                        Food(
                            name = tokens[0].trim().removeSurrounding("\""),
                            calories = tokens[1].trim().toIntOrNull() ?: 0,
                            fat = tokens[2].trim().removeSurrounding("\""),
                            carbs = tokens[3].trim().removeSurrounding("\""),
                            protein = tokens[4].trim().removeSurrounding("\""),
                            unit = if (tokens.size > 5) tokens[5].trim().removeSurrounding("\"") else "100g"
                        )
                    )
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return foodList
    }

    /**
     * Fungsi untuk mencari makanan berdasarkan input user (Search Feature)
     */
    fun searchFood(query: String): List<Food> {
        val allFood = getAllFoodFromCsv()
        return allFood.filter { it.name.contains(query, ignoreCase = true) }
    }
}