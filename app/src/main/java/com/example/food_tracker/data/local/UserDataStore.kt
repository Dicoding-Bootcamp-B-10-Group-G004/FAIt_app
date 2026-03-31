package com.example.food_tracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.food_tracker.domain.model.AppPreferences
import com.example.food_tracker.domain.model.Goal
import com.example.food_tracker.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

class UserDataStore(private val context: Context) {

    private companion object {
        val WEIGHT = doublePreferencesKey("weight")
        val HEIGHT = doublePreferencesKey("height")
        val AGE = intPreferencesKey("age")
        val IS_MALE = booleanPreferencesKey("is_male")
        val ACTIVITY_LEVEL = doublePreferencesKey("activity_level")
        val GOAL = stringPreferencesKey("goal")

        // Consumed totals
        val SUPPLIED_CALORIES = doublePreferencesKey("supplied_calories")
        val PROTEIN = doublePreferencesKey("protein")
        val CARBS = doublePreferencesKey("carbs")
        val FAT = doublePreferencesKey("fat")
        
        // Goals
        val CALORIES_GOAL = doublePreferencesKey("calories_goal")
        val PROTEIN_GOAL = doublePreferencesKey("protein_goal")
        val CARBS_GOAL = doublePreferencesKey("carbs_goal")
        val FAT_GOAL = doublePreferencesKey("fat_goal")

        val LAST_SUGGESTION_DATE = stringPreferencesKey("last_suggestion_date")
        val DIETARY_SUGGESTION = stringPreferencesKey("dietary_suggestion")
        
        val CACHED_MODELS = stringPreferencesKey("cached_models")

        // App Preferences
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
    }

    suspend fun saveCalorieGoal(value: Double) { context.dataStore.edit { it[CALORIES_GOAL] = value } }
    suspend fun saveProteinGoal(value: Double) { context.dataStore.edit { it[PROTEIN_GOAL] = value } }
    suspend fun saveCarbsGoal(value: Double) { context.dataStore.edit { it[CARBS_GOAL] = value } }
    suspend fun saveFatGoal(value: Double) { context.dataStore.edit { it[FAT_GOAL] = value } }

    fun getCalorieGoal(): Flow<Double?> = context.dataStore.data.map { it[CALORIES_GOAL] }
    fun getProteinGoal(): Flow<Double?> = context.dataStore.data.map { it[PROTEIN_GOAL] }
    fun getCarbsGoal(): Flow<Double?> = context.dataStore.data.map { it[CARBS_GOAL] }
    fun getFatGoal(): Flow<Double?> = context.dataStore.data.map { it[FAT_GOAL] }

    fun getSuppliedCals(): Flow<Double?> = context.dataStore.data.map { it[SUPPLIED_CALORIES] }
    fun getProtein(): Flow<Double?> = context.dataStore.data.map { it[PROTEIN] }
    fun getCarbs(): Flow<Double?> = context.dataStore.data.map { it[CARBS] }
    fun getFat(): Flow<Double?> = context.dataStore.data.map { it[FAT] }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            UserProfile(
                weight = prefs[WEIGHT] ?: 0.0,
                height = prefs[HEIGHT] ?: 0.0,
                age = prefs[AGE] ?: 0,
                isMale = prefs[IS_MALE] ?: true,
                activityLevel = prefs[ACTIVITY_LEVEL] ?: 1.2,
                goal = Goal.valueOf(prefs[ GOAL] ?: Goal.MAINTAIN.name)
            )
        }

    suspend fun saveProfile(weight: Double, height: Double, age: Int, isMale: Boolean, activityLevel: Double, goal: Goal) {
        context.dataStore.edit { prefs ->
            prefs[WEIGHT] = weight
            prefs[HEIGHT] = height
            prefs[AGE] = age
            prefs[IS_MALE] = isMale
            prefs[ACTIVITY_LEVEL] = activityLevel
            prefs[GOAL] = goal.name
        }
    }

    suspend fun saveSuppliedCals(calories: Double) { context.dataStore.edit { it[SUPPLIED_CALORIES] = calories } }
    suspend fun saveProtein(protein: Double) { context.dataStore.edit { it[PROTEIN] = protein } }
    suspend fun saveCarbs(carbs: Double) { context.dataStore.edit { it[CARBS] = carbs } }
    suspend fun saveFat(fat: Double) { context.dataStore.edit { it[FAT] = fat } }

    val lastSuggestionDateFlow: Flow<String?> = context.dataStore.data.map { it[LAST_SUGGESTION_DATE] }
    val dietarySuggestionFlow: Flow<String?> = context.dataStore.data.map { it[DIETARY_SUGGESTION] }

    suspend fun saveDietarySuggestion(suggestion: String, date: String) {
        context.dataStore.edit { prefs ->
            prefs[DIETARY_SUGGESTION] = suggestion
            prefs[LAST_SUGGESTION_DATE] = date
        }
    }

    val cachedModelsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[CACHED_MODELS]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    suspend fun saveCachedModels(models: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[CACHED_MODELS] = models.joinToString(",")
        }
    }

    val appPreferencesFlow: Flow<AppPreferences> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            AppPreferences(
                languageCode = prefs[LANGUAGE_CODE] ?: "en"
            )
        }

    suspend fun saveLanguageCode(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[LANGUAGE_CODE] = languageCode
        }
    }
}
