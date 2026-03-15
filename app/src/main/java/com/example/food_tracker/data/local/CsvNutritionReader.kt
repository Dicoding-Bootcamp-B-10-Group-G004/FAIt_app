package com.example.food_tracker.data.local

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

data class FoodNutrition(
    val name: String,
    val calories: Double,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

private fun cleanNumber(value: String): Double {
    return value
        .replace("\"", "")
        .replace("g", "")
        .replace(",", ".")
        .trim()
        .toDoubleOrNull() ?: 0.0
}

class CsvNutritionReader(private val context: Context) {
    fun getNutritionData(foodName: String): FoodNutrition? {
        try {
            val inputStream = context.assets.open("food_nutrition.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))

            // Baca baris per baris
            reader.useLines { lines ->
                lines.drop(1).forEach { line ->
                    val columns = line.split(",")

                    if (columns.size < 5) return@forEach

                    val name = columns[0].trim()

                    if (name.contains(foodName.trim(), ignoreCase = true)) {
                        return FoodNutrition(
                            name = name,
                            calories = cleanNumber(columns[1]),
                            protein = cleanNumber(columns[4]),
                            carbs = cleanNumber(columns[3]),
                            fat = cleanNumber(columns[2])
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}