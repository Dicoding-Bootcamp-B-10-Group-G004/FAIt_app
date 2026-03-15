package com.example.food_tracker.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.food_tracker.data.local.UserDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    userDataStore: UserDataStore,
    onFinished: () -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // State untuk loading biar user gak klik berkali-kali
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Selamat Datang!", style = MaterialTheme.typography.headlineMedium)
        Text("Lengkapi data dirimu dulu ya", modifier = Modifier.padding(bottom = 32.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) weight = it },
            label = { Text("Berat Badan (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { if (it.all { char -> char.isDigit() }) height = it },
            label = { Text("Tinggi Badan (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
            label = { Text("Umur") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val w = weight.toDoubleOrNull() ?: 0.0
                val h = height.toDoubleOrNull() ?: 0.0
                val a = age.toIntOrNull() ?: 0

                if (w > 0 && h > 0 && a > 0) {
                    isLoading = true
                    scope.launch {
                        // 1. Simpan data dan TUNGGU (Suspend)
                        userDataStore.saveProfile(
                            weight = w,
                            height = h,
                            age = a,
                            isMale = true,
                            activityLevel = 1.2
                        )

                        // 2. Kasih delay dikit biar DataStore beneran rampung nulis ke disk
                        delay(500)

                        // 3. Baru pindah ke screen utama
                        onFinished()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && weight.isNotEmpty() && height.isNotEmpty() && age.isNotEmpty()
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Simpan & Lanjutkan")
            }
        }
    }
}