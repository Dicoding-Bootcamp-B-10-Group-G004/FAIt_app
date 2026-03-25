package com.example.food_tracker.feature.camera

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food_tracker.data.ml.SnappedResult
import com.example.food_tracker.domain.model.Food
import com.example.food_tracker.feature.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionResultScreen(
    snappedResult: SnappedResult,
    homeViewModel: HomeViewModel,
    onBack: () -> Unit,
    onConfirm: (Bitmap) -> Unit
) {
    val darkGreen = Color(0xFF006400)

    // Logic untuk mencocokkan hasil deteksi ML dengan data di CSV melalui ViewModel
    val detectedFoods: List<Food> = remember(snappedResult, homeViewModel) {
        val labels = snappedResult.result.detections.map { it.label }.distinct()
        labels.mapNotNull { label ->
            homeViewModel.getFoodByName(label)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasil Deteksi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().height(250.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Box {
                        Image(
                            bitmap = snappedResult.bitmap.asImageBitmap(),
                            contentDescription = "Snapped Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        DetectionOverlay(detections = snappedResult.result.detections)
                    }
                }
            }

            // Jika label dari ML tidak ketemu di CSV
            if (detectedFoods.isEmpty()) {
                item {
                    Text(
                        "Makanan tidak ditemukan di database (CSV)",
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }

            // Menampilkan list makanan yang cocok dari CSV
            items(detectedFoods) { food ->
                var portionText by remember { mutableStateOf("100") }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(food.name, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Text("${food.calories} kcal / 100g", fontSize = 14.sp, color = Color.Gray)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            // Memanggil fungsi NutrientText yang ada di bawah
                            NutrientText(value = "${food.carbs}g", label = "Carbs")
                            NutrientText(value = "${food.protein}g", label = "Protein")
                            NutrientText(value = "${food.fat}g", label = "Fat")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = portionText,
                                onValueChange = { portionText = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Porsi") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(food.unit, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val p = portionText.toIntOrNull() ?: 100
                                homeViewModel.addFoodDirect(food, p)
                                onBack()
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = darkGreen)
                        ) {
                            Icon(Icons.Rounded.Add, null)
                            Text(" Tambahkan ke Diary", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                TextButton(onClick = onBack) {
                    Text("Ambil Foto Lagi", color = Color.Gray)
                }
            }
        }
    }
}

// FIX ERROR: Fungsi ini harus ada supaya NutrientText tidak Unresolved Reference
@Composable
fun NutrientText(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}