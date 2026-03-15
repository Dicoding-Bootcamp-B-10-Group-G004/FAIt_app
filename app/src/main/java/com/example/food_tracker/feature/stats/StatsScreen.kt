package com.example.food_tracker.feature.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food_tracker.feature.home.HomeViewModel

@Composable
fun StatsScreen(homeViewModel: HomeViewModel) {

    val state = homeViewModel.state

    val softGreen = Color(0xFFB9F6CA)

    val calorieGoal = 2000
    val proteinGoal = 120
    val carbsGoal = 250
    val fatGoal = 70

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        Text(
            text = "Nutrition Stats",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        CaloriesCard(
            consumed = state.suppliedCalories.toInt(),
            goal = calorieGoal,
            color = softGreen
        )

        MacroCard(
            protein = state.proteinCount.toInt(),
            carbs = state.carbsCount.toInt(),
            fat = state.fatCount.toInt(),
            proteinGoal = proteinGoal,
            carbsGoal = carbsGoal,
            fatGoal = fatGoal,
            color = softGreen
        )

        GoalCard(homeViewModel)
    }
}

@Composable
fun CaloriesCard(consumed:Int,goal:Int,color:Color){

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ){

        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            Text("Calories Today", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Box(contentAlignment = Alignment.Center){

                CircularProgressIndicator(
                    progress = { (consumed.toFloat()/goal).coerceIn(0f,1f) },
                    strokeWidth = 12.dp,
                    color = color,
                    trackColor = color.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )

                Text("$consumed / $goal kcal", fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun MacroCard(
    protein:Int,
    carbs:Int,
    fat:Int,
    proteinGoal:Int,
    carbsGoal:Int,
    fatGoal:Int,
    color:Color
){

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ){

        Column(
            modifier = Modifier.padding(20.dp)
        ){

            Text("Macros", fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            MacroRow("Protein",protein,proteinGoal,color)

            Spacer(modifier = Modifier.height(12.dp))

            MacroRow("Carbs",carbs,carbsGoal,color)

            Spacer(modifier = Modifier.height(12.dp))

            MacroRow("Fat",fat,fatGoal,color)
        }
    }
}

@Composable
fun MacroRow(label:String,value:Int,goal:Int,color:Color){

    Column{

        Text("$label: $value / $goal g",fontSize = 12.sp)

        LinearProgressIndicator(
            progress = { (value.toFloat()/goal).coerceIn(0f,1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun GoalCard(homeViewModel: HomeViewModel){

    val result = homeViewModel.getMacroAchievement()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ){

        Column(
            modifier = Modifier.padding(20.dp)
        ){

            Text("Daily Goals",fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Protein Goal: ${if(result.first) "✔ Tercapai" else "❌ Belum"}")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Carbs Goal: ${if(result.second) "✔ Tercapai" else "❌ Belum"}")

            Spacer(modifier = Modifier.height(8.dp))

            Text("Calories Goal: ${if(result.third) "✔ Tercapai" else "❌ Belum"}")
        }
    }
}