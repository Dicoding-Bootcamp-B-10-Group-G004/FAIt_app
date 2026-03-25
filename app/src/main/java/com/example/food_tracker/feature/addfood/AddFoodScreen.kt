package com.example.food_tracker.feature.addfood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    viewModel: AddFoodViewModel,
    prefillName: String? = null,
    onSaveSuccess: () -> Unit = {}
) {
    val state = viewModel.state

    // Prefill nama makanan jika datang dari hasil deteksi kamera
    LaunchedEffect(prefillName) {
        if (!prefillName.isNullOrBlank()) {
            viewModel.onEvent(AddFoodEvent.OnSearchQueryChange(prefillName))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Konfirmasi Nutrisi", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.onEvent(AddFoodEvent.OnSearchQueryChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Nama Makanan") },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        val food = state.selectedFood ?: state.searchResults.firstOrNull()

        if (food != null) {
            LaunchedEffect(food) {
                viewModel.onEvent(AddFoodEvent.OnFoodSelected(food))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = food.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Porsi: ", fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = state.portion.toString(),
                            onValueChange = {
                                val p = it.toIntOrNull() ?: 0
                                viewModel.onEvent(AddFoodEvent.OnPortionChange(p))
                            },
                            modifier = Modifier.width(100.dp).padding(horizontal = 8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        Text(food.unit) // Menggunakan unit asli dari CSV (misal: gram/ml)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    // Perhitungan estimasi berdasarkan porsi input
                    val porsi = state.portion.toDouble()
                    val totalKalori = (food.calories * (porsi / 100)).toInt() // Asumsi data CSV per 100 unit

                    // FIX: Handling format koma pada string nutrisi agar tidak error saat casting
                    val protein = (food.protein.replace(",", ".").toDoubleOrNull() ?: 0.0) * (porsi / 100)
                    val karbo = (food.carbs.replace(",", ".").toDoubleOrNull() ?: 0.0) * (porsi / 100)
                    val lemak = (food.fat.replace(",", ".").toDoubleOrNull() ?: 0.0) * (porsi / 100)

                    Text("Estimasi Total Nutrisi:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🔥 Kalori: $totalKalori kkal")
                    Text("🥩 Protein: ${String.format("%.1f", protein)}g")
                    Text("🍞 Karbo: ${String.format("%.1f", karbo)}g")
                    Text("🍳 Lemak: ${String.format("%.1f", lemak)}g")

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.onEvent(AddFoodEvent.OnSave)
                            onSaveSuccess()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text("Simpan ke Diary", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else if (state.searchQuery.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), contentAlignment = Alignment.Center) {
                Text("Makanan tidak ditemukan di database CSV", color = Color.Gray)
            }
        }
    }
}