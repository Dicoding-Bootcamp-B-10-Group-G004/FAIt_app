package com.example.food_tracker.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.food_tracker.R
import com.example.food_tracker.core.ui.components.FTCenterTopBar
import com.example.food_tracker.core.ui.components.FTOutlinedTextField
import com.example.food_tracker.core.ui.components.FTPrimaryButton
import com.example.food_tracker.domain.model.Goal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel
) {
    val state = viewModel.state
    val themeColors = MaterialTheme.colorScheme
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    var showPrefsDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = themeColors.background,
        topBar = {
            FTCenterTopBar(
                title = stringResource(R.string.profile),
                onTopBarClick = {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                actions = {
                    IconButton(onClick = { showPrefsDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.settings),
                            tint = themeColors.onSecondary
                        )
                    }
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.edit_profile),
                            tint = themeColors.onSecondary
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp)
        ) {
            item(key = "bmi_card") {
                BMICard(bmiScore = state.bmi, status = state.bmiStatus)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item(key = "nutrition_card") {
                NutritionCard(
                    calories = state.calorieGoal,
                    protein = state.proteinGoal,
                    carbs = state.carbsGoal,
                    fat = state.fatGoal
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item(key = "profile_info") {
                val activityResId = remember(state.activityLevel) {
                    viewModel.getActivityLevelResId(state.activityLevel)
                }
                val activityDesc = stringResource(activityResId)
                
                val goalLabel = when(state.goal) {
                    Goal.LOSE -> stringResource(R.string.goal_lose)
                    Goal.MAINTAIN -> stringResource(R.string.goal_maintain)
                    Goal.GAIN -> stringResource(R.string.goal_gain)
                }

                val languageLabel = if (state.languageCode == "en") {
                    stringResource(R.string.english)
                } else {
                    stringResource(R.string.indonesian)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileInfoItem(
                        Icons.Rounded.MonitorWeight, 
                        stringResource(R.string.weight), 
                        stringResource(R.string.weight_unit_format, state.weight)
                    )
                    ProfileInfoItem(
                        Icons.Rounded.Height, 
                        stringResource(R.string.height), 
                        stringResource(R.string.height_unit_format, state.height)
                    )
                    ProfileInfoItem(
                        Icons.Rounded.Cake, 
                        stringResource(R.string.age), 
                        stringResource(R.string.age_unit_format, state.age, stringResource(R.string.years))
                    )
                    ProfileInfoItem(
                        Icons.Rounded.Male, 
                        stringResource(R.string.gender), 
                        if (state.isMale) stringResource(R.string.male) else stringResource(R.string.female)
                    )
                    ProfileInfoItem(
                        Icons.Rounded.DirectionsRun, 
                        stringResource(R.string.activity_intensity), 
                        activityDesc
                    )
                    ProfileInfoItem(Icons.Rounded.Flag, stringResource(R.string.goal_label), goalLabel)
                    ProfileInfoItem(Icons.Rounded.Language, stringResource(R.string.language), languageLabel)
                }
            }
        }

        if (showEditDialog) {
            EditProfileDialog(
                state = state,
                viewModel = viewModel,
                onDismiss = { showEditDialog = false },
                onSave = {
                    viewModel.saveToLocal()
                    showEditDialog = false
                }
            )
        }

        if (showPrefsDialog) {
            EditPreferencesDialog(
                state = state,
                viewModel = viewModel,
                onDismiss = { showPrefsDialog = false },
                onSave = {
                    viewModel.savePreferences()
                    showPrefsDialog = false
                }
            )
        }
    }
}

@Composable
fun EditPreferencesDialog(
    state: NutritionState,
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_preferences), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.language),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LanguageOption(
                        text = stringResource(R.string.english),
                        isSelected = state.languageCode == "en",
                        onClick = { viewModel.onEvent(ProfileEvent.LanguageChanged("en")) },
                        modifier = Modifier.weight(1f)
                    )
                    LanguageOption(
                        text = stringResource(R.string.indonesian),
                        isSelected = state.languageCode == "in",
                        onClick = { viewModel.onEvent(ProfileEvent.LanguageChanged("in")) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text(stringResource(R.string.save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.outline)
            }
        }
    )
}

@Composable
fun LanguageOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun EditProfileDialog(
    state: NutritionState,
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val multipliers = remember { listOf(1.2, 1.375, 1.55, 1.725, 1.9) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_profile), style = MaterialTheme.typography.titleLarge) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    FTOutlinedTextField(
                        value = state.weight,
                        onValueChange = { viewModel.onEvent(ProfileEvent.WeightChanged(it)) },
                        label = stringResource(R.string.weight_kg),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                item {
                    FTOutlinedTextField(
                        value = state.height,
                        onValueChange = { viewModel.onEvent(ProfileEvent.HeightChanged(it)) },
                        label = stringResource(R.string.height_cm),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                item {
                    FTOutlinedTextField(
                        value = state.age,
                        onValueChange = { viewModel.onEvent(ProfileEvent.AgeChanged(it)) },
                        label = stringResource(R.string.age),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                item {
                    Column {
                        Text(stringResource(R.string.gender), style = MaterialTheme.typography.labelLarge)
                        Row(Modifier.selectableGroup()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = state.isMale, onClick = { viewModel.onEvent(ProfileEvent.GenderChanged(true)) })
                                Text(stringResource(R.string.male), style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = !state.isMale, onClick = { viewModel.onEvent(ProfileEvent.GenderChanged(false)) })
                                Text(stringResource(R.string.female), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                item {
                    Column {
                        Text(stringResource(R.string.goal_label), style = MaterialTheme.typography.labelLarge)
                        Goal.entries.forEach { goal ->
                            val goalLabel = when(goal) {
                                Goal.LOSE -> stringResource(R.string.goal_lose)
                                Goal.MAINTAIN -> stringResource(R.string.goal_maintain)
                                Goal.GAIN -> stringResource(R.string.goal_gain)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = state.goal == goal, onClick = { viewModel.onEvent(ProfileEvent.GoalChanged(goal)) })
                                Text(goalLabel, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                item {
                    Column {
                        Text(stringResource(R.string.activity_intensity), style = MaterialTheme.typography.labelLarge)
                        var index by remember(state.activityLevel) {
                            mutableFloatStateOf(multipliers.indexOfFirst { it >= state.activityLevel }.coerceAtLeast(0).toFloat())
                        }
                        Slider(
                            value = index,
                            onValueChange = { 
                                index = it
                                viewModel.onEvent(ProfileEvent.ActivityLevelChanged(multipliers[it.toInt()]))
                            },
                            valueRange = 0f..4f,
                            steps = 3
                        )
                        val resId = remember(index) { viewModel.getActivityLevelResId(multipliers[index.toInt()]) }
                        Text(
                            text = stringResource(resId),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) { 
                Text(stringResource(R.string.save), fontWeight = FontWeight.Bold) 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.outline) 
            }
        }
    )
}

@Composable
fun NutritionCard(calories: String, protein: String, carbs: String, fat: String) {
    val themeColors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        color = themeColors.primaryContainer,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        stringResource(R.string.daily_calorie_goal), 
                        color = themeColors.onPrimaryContainer.copy(alpha = 0.8f), 
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        stringResource(R.string.kcal_unit_format, calories),
                        color = themeColors.onPrimaryContainer, 
                        style = MaterialTheme.typography.headlineMedium, 
                        fontWeight = FontWeight.Black
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.Whatshot, 
                    contentDescription = null, 
                    tint = Color(0xFFFFCC80), 
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = themeColors.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MacroItem(stringResource(R.string.legend_protein), protein, themeColors.onPrimaryContainer)
                MacroItem(stringResource(R.string.legend_carbs), carbs, themeColors.onPrimaryContainer)
                MacroItem(stringResource(R.string.legend_fat), fat, themeColors.onPrimaryContainer)
            }
        }
    }
}

@Composable
fun MacroItem(label: String, value: String, textColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = textColor.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        Text(value, color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun BMICard(bmiScore: String, status: String) {
    val themeColors = MaterialTheme.colorScheme
    
    val statusLabel = when(status) {
        "Underweight" -> stringResource(R.string.bmi_underweight)
        "Normal Weight" -> stringResource(R.string.bmi_normal)
        "Overweight" -> stringResource(R.string.bmi_overweight)
        "Obese" -> stringResource(R.string.bmi_obese)
        else -> stringResource(R.string.bmi_unknown)
    }

    val color = remember(status) {
        when (status) {
            "Underweight" -> Color(0xFF64B5F6)
            "Normal Weight" -> Color(0xFF81C784)
            "Overweight" -> Color(0xFFFFD54F)
            else -> Color(0xFFE57373)
        }
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(180.dp).background(color.copy(alpha = 0.2f), CircleShape), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(bmiScore, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Black, color = color)
                Text(stringResource(R.string.bmi), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.7f))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = statusLabel,
            style = MaterialTheme.typography.headlineSmall, 
            fontWeight = FontWeight.Bold,
            color = themeColors.onBackground
        )
    }
}

@Composable
fun ProfileInfoItem(icon: ImageVector, label: String, value: String) {
    val themeColors = MaterialTheme.colorScheme
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = RoundedCornerShape(12.dp), 
            color = themeColors.surface, 
            shadowElevation = 2.dp, 
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) { 
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = themeColors.primary
                ) 
            }
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = themeColors.outline)
            Text(
                text = value, 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = themeColors.onSurface
            )
        }
    }
}
