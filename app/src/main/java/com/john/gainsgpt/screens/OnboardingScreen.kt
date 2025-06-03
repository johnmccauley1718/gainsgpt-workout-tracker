package com.john.gainsgpt.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.john.gainsgpt.data.ChatGPTService
import com.john.gainsgpt.data.ProfileRepository
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    userName: String,
    profileRepository: ProfileRepository,
    onPlanCreated: (String) -> Unit
) {
    var goal by remember { mutableStateOf("Build muscle") }
    var weight by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("Beginner") }
    var days by remember { mutableStateOf(3) }
    var notes by remember { mutableStateOf("") }
    val fitnessGoals = listOf("Build muscle", "Lose fat", "Maintain")
    val experienceLevels = listOf("Beginner", "Intermediate", "Advanced")
    val daysOptions = (2..7).toList()
    val focusAreas = listOf("Chest", "Back", "Legs", "Arms", "Shoulders", "Full-body")
    var selectedFocuses by remember { mutableStateOf(setOf("Full-body")) }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 32.dp)
    ) {
        item {
            Text(
                text = "Welcome, $userName!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "Let's personalize your GainsGPT experience.",
                fontSize = 18.sp,
                color = Color.White,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
        item {
            Text(
                "Your Main Goal",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp, start = 4.dp)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fitnessGoals.forEach { g ->
                    FilterChip(
                        selected = goal == g,
                        onClick = { goal = g },
                        label = { Text(g, color = Color.White) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            OutlinedTextField(
                value = weight,
                onValueChange = { if (it.length <= 3) weight = it.filter { c -> c.isDigit() } },
                label = { Text("Current Weight (lbs)", color = Color.White) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedLabelColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            Text(
                "Experience Level",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp, start = 4.dp)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                experienceLevels.forEach { e ->
                    FilterChip(
                        selected = experience == e,
                        onClick = { experience = e },
                        label = { Text(e, color = Color.White) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            Text(
                "Workout Days / Week",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp, start = 4.dp)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                daysOptions.forEach { d ->
                    FilterChip(
                        selected = days == d,
                        onClick = { days = d },
                        label = { Text(d.toString(), color = Color.White) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            Text(
                "Primary Focus",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp, start = 4.dp)
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                focusAreas.forEach { f ->
                    val isSelected = selectedFocuses.contains(f)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedFocuses = when {
                                f == "Full-body" && !isSelected -> setOf("Full-body")
                                f == "Full-body" && isSelected -> emptySet()
                                selectedFocuses.contains("Full-body") -> setOf(f)
                                isSelected -> selectedFocuses - f
                                else -> selectedFocuses + f
                            }
                        },
                        label = { Text(f, color = Color.White) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it.take(60) },
                label = { Text("Injuries or Notes (optional)", color = Color.White) },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedLabelColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
        item {
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            Button(
                enabled = !isLoading,
                onClick = {
                    val focusString = selectedFocuses.joinToString(",")
                    if (weight.isBlank()) {
                        errorMessage = "Please enter your weight"
                        return@Button
                    }
                    errorMessage = null
                    isLoading = true
                    coroutineScope.launch {
                        val prompt =
                            "Create a fitness and nutrition plan for a user with these details: " +
                                    "Goal: $goal, Weight: $weight lbs, Experience: $experience, " +
                                    "Days: $days, Focus: $focusString, Notes: $notes"
                        val plan = ChatGPTService.buildPlan(prompt)
                        if (plan != null) {
                            profileRepository.saveOnboardingComplete(plan)
                            onPlanCreated(plan)
                        } else {
                            errorMessage = "Sorry, could not generate plan."
                        }
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Create My Plan", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
