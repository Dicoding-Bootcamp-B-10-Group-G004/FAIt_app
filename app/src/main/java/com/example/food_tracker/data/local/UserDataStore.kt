package com.example.food_tracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.food_tracker.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

class UserDataStore(private val context: Context) {

    private val CALORIES_GOAL = doublePreferencesKey("calories_goal")

    suspend fun saveCalorieGoal(value: Double) {
        context.dataStore.edit {
            it[CALORIES_GOAL] = value
        }
    }

    fun getCalorieGoal(): Flow<Double?> {
        return context.dataStore.data.map {
            it[CALORIES_GOAL]
        }
    }

    private companion object {
        val WEIGHT = doublePreferencesKey("weight")
        val HEIGHT = doublePreferencesKey("height")
        val AGE = intPreferencesKey("age")
        val IS_MALE = booleanPreferencesKey("is_male")
        val ACTIVITY_LEVEL = doublePreferencesKey("activity_level")

        // Keys untuk nutrisi hasil scan
        val SUPPLIED_CALORIES = doublePreferencesKey("supplied_calories")
        val PROTEIN = doublePreferencesKey("protein")
        val CARBS = doublePreferencesKey("carbs")
        val FAT = doublePreferencesKey("fat")
    }

    // --- FUNGSI UNTUK MENGAMBIL DATA (GETTER) ---
    // Tambahkan ini supaya FoodRepositoryImpl bisa baca data lama sebelum dijumlahkan
    fun getSuppliedCals(): Flow<Double?> = context.dataStore.data.map { it[SUPPLIED_CALORIES] }
    fun getProtein(): Flow<Double?> = context.dataStore.data.map { it[PROTEIN] }
    fun getCarbs(): Flow<Double?> = context.dataStore.data.map { it[CARBS] }
    fun getFat(): Flow<Double?> = context.dataStore.data.map { it[FAT] }

    // Ambil data profil
    val userProfileFlow: Flow<UserProfile> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            UserProfile(
                weight = prefs[WEIGHT] ?: 0.0,
                height = prefs[HEIGHT] ?: 0.0,
                age = prefs[AGE] ?: 0,
                isMale = prefs[IS_MALE] ?: true,
                activityLevel = prefs[ACTIVITY_LEVEL] ?: 1.2
            )
        }

    // Simpan profil
    suspend fun saveProfile(weight: Double, height: Double, age: Int, isMale: Boolean, activityLevel: Double) {
        context.dataStore.edit { prefs ->
            prefs[WEIGHT] = weight
            prefs[HEIGHT] = height
            prefs[AGE] = age
            prefs[IS_MALE] = isMale
            prefs[ACTIVITY_LEVEL] = activityLevel
        }
    }

    // Simpan nutrisi
    suspend fun saveSuppliedCals(calories: Double) { context.dataStore.edit { it[SUPPLIED_CALORIES] = calories } }
    suspend fun saveProtein(protein: Double) { context.dataStore.edit { it[PROTEIN] = protein } }
    suspend fun saveCarbs(carbs: Double) { context.dataStore.edit { it[CARBS] = carbs } }
    suspend fun saveFat(fat: Double) { context.dataStore.edit { it[FAT] = fat } }
}