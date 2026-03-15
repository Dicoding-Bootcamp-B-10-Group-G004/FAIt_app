package com.example.food_tracker.feature.profile

sealed class ProfileEvent {
    data class WeightChanged(val weight: String): ProfileEvent()
    data class HeightChanged(val height: String): ProfileEvent()
    data class AgeChanged(val age: String): ProfileEvent()
    data class GenderChanged(val isMale: Boolean): ProfileEvent()
    data class ActivityLevelChanged(val level: Double): ProfileEvent()
}