package com.example.food_tracker.feature.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HistoryScreen() {
    val softGreen = Color(0xFFB9F6CA)
    val dates = (1..31).toList() // Contoh list tanggal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF0))
            .padding(16.dp)
    ) {
        Text("March 2026", fontSize = 24.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))

        // Kalender Horizontal (Sesuai Video 0:02)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(dates) { date ->
                Column(
                    modifier = Modifier
                        .width(50.dp)
                        .background(
                            if (date == 12) softGreen else Color.White,
                            RoundedCornerShape(15.dp)
                        )
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Thu", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "$date", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Daily Summary Card (Sesuai Video 0:04)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Daily Summary", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                SummaryRow("Calories", "639 / 2567 kcal", 0.25f, Color.Red)
                SummaryRow("Proteins", "34 / 96 g", 0.35f, softGreen)
                SummaryRow("Fats", "24 / 71 g", 0.33f, Color.Yellow)
                SummaryRow("Carbs", "71 / 385 g", 0.18f, Color.Cyan)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String, progress: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}