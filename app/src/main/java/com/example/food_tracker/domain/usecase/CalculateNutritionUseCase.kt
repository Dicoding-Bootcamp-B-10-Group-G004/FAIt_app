package com.example.food_tracker.domain.usecase

import com.example.food_tracker.domain.model.UserProfile
import kotlin.math.max

class CalculateNutritionUseCase {
    operator fun invoke(profile: UserProfile): Double {
        // PENGAMAN: Jika ada data yang 0, jangan hitung, balikin 0 aja.
        if (profile.weight <= 0.0 || profile.height <= 0.0 || profile.age <= 0) {
            return 0.0
        }

        // Rumus Mifflin-St Jeor
        val s = if (profile.isMale) 5 else -161
        val bmr = (10 * profile.weight) + (6.25 * profile.height) - (5 * profile.age) + s

        // TDEE = BMR * Faktor Aktivitas
        // Gunakan max(0.0, ...) agar hasil tidak pernah negatif
        return max(0.0, bmr * profile.activityLevel)
    }
}