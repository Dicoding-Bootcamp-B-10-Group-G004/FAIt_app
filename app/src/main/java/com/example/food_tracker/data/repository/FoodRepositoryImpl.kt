package com.example.food_tracker.data.repository

import android.content.Context
import com.example.food_tracker.domain.model.Food
import java.io.BufferedReader
import java.io.InputStreamReader

class FoodRepositoryImpl(private val context: Context) {

    /**
     * Fungsi untuk membaca semua data gizi dari file CSV hasil_gizi_100gram.csv
     */
    fun getAllFoodFromCsv(): List<Food> {
        val foodList = mutableListOf<Food>()

        try {
            // Membuka file dari folder assets
            val inputStream = context.assets.open("hasil_gizi_100gram.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))

            // Skip baris pertama (header: nama makanan, kalori, dll)
            val header = reader.readLine()

            reader.forEachLine { line ->
                // Menggunakan regex untuk menangani koma di dalam tanda kutip (CSV standard)
                val tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())

                if (tokens.size >= 5) {
                    foodList.add(
                        Food(
                            name = tokens[0].trim().removeSurrounding("\""), // Contoh: AW cola
                            calories = tokens[1].trim().toIntOrNull() ?: 0,   // Contoh: 37
                            fat = tokens[2].trim().removeSurrounding("\""),      // Contoh: 0,02g
                            carbs = tokens[3].trim().removeSurrounding("\""),    // Contoh: 9,56g
                            protein = tokens[4].trim().removeSurrounding("\""),  // Contoh: 0,07g
                            unit = tokens[5].trim().removeSurrounding("\"")      // Contoh: 100 gram (g)
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