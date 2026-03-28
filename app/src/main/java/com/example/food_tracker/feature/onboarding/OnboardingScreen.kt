package com.example.food_tracker.feature.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food_tracker.R
import com.example.food_tracker.core.ui.components.FTOutlinedTextField
import com.example.food_tracker.core.ui.components.FTPrimaryButton
import com.example.food_tracker.data.repository.ProfileRepository
import com.example.food_tracker.domain.model.Goal
import com.example.food_tracker.domain.model.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun OnboardingScreen(
    profileRepository: ProfileRepository,
    onFinished: () -> Unit
) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }
    var selectedGoal by remember { mutableStateOf(Goal.MAINTAIN) }
    
    val activityLevels = listOf(
        1.2 to stringResource(R.string.sedentary),
        1.375 to stringResource(R.string.slightly_active),
        1.55 to stringResource(R.string.moderately_active),
        1.725 to stringResource(R.string.very_active),
        1.9 to stringResource(R.string.extra_active)
    )
    
    var activityLevelIndex by remember { mutableFloatStateOf(0f) }
    
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.welcome), 
            style = MaterialTheme.typography.headlineLarge, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.onboarding_subtitle), 
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        FTOutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = stringResource(R.string.weight_kg),
            placeholder = "e.g. 70",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(12.dp))

        FTOutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = stringResource(R.string.height_cm),
            placeholder = "e.g. 175",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(12.dp))

        FTOutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = stringResource(R.string.age),
            placeholder = "e.g. 25",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.biological_sex), 
            style = MaterialTheme.typography.titleMedium, 
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.fillMaxWidth().selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isMale, onClick = { isMale = true })
            Text(
                text = stringResource(R.string.male), 
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable { isMale = true },
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(24.dp))
            RadioButton(selected = !isMale, onClick = { isMale = false })
            Text(
                text = stringResource(R.string.female), 
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.clickable { isMale = false },
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.activity_intensity), 
            style = MaterialTheme.typography.titleMedium, 
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onBackground
        )
        Slider(
            value = activityLevelIndex,
            onValueChange = { activityLevelIndex = it },
            valueRange = 0f..4f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        val currentLevel = activityLevels[activityLevelIndex.toInt()]
        Text(
            text = currentLevel.second,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.your_goal), 
            style = MaterialTheme.typography.titleMedium, 
            modifier = Modifier.align(Alignment.Start),
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Goal.entries.forEach { g ->
                val goalLabel = g.name.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                FilterChip(
                    selected = selectedGoal == g,
                    onClick = { selectedGoal = g },
                    label = { Text(goalLabel, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        FTPrimaryButton(
            text = stringResource(R.string.save_and_get_started),
            onClick = {
                val w = weight.toDoubleOrNull() ?: 0.0
                val h = height.toDoubleOrNull() ?: 0.0
                val a = age.toIntOrNull() ?: 0
                val level = activityLevels[activityLevelIndex.toInt()].first

                if (w > 0 && h > 0 && a > 0) {
                    isLoading = true
                    scope.launch {
                        profileRepository.saveProfile(
                            UserProfile(
                                weight = w,
                                height = h,
                                age = a,
                                isMale = isMale,
                                activityLevel = level,
                                goal = selectedGoal
                            )
                        )
                        delay(500)
                        onFinished()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && weight.isNotEmpty() && height.isNotEmpty() && age.isNotEmpty(),
            isLoading = isLoading
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}
