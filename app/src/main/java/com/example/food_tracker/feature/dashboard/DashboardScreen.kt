package com.example.food_tracker.feature.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food_tracker.feature.home.HomeViewModel

@Composable
fun DashboardScreen(homeViewModel: HomeViewModel) {

    val result = homeViewModel.getMacroAchievement()

    val proteinReached = result.first
    val carbsReached = result.second
    val caloriesReached = result.third

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {

            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Daily Nutrition Goal",
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Protein Goal: ${if (proteinReached) "✔ Tercapai" else "❌ Belum"}")

                Spacer(modifier = Modifier.height(12.dp))

                Text("Carbs Goal: ${if (carbsReached) "✔ Tercapai" else "❌ Belum"}")

                Spacer(modifier = Modifier.height(12.dp))

                Text("Calories Goal: ${if (caloriesReached) "✔ Tercapai" else "❌ Belum"}")
            }
        }
    }
}