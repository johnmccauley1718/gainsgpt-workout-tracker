@file:OptIn(ExperimentalMaterial3Api::class)

package com.john.gainsgpt.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.flowlayout.FlowRow
import com.john.gainsgpt.viewmodel.ProfileViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun TodayWorkoutScreen(
    navController: NavHostController,
    userDisplayName: String,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val today = LocalDate.now()
    val dayOfWeek = today.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val formattedDate = today.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))

    LaunchedEffect(Unit) { profileViewModel.loadProfile() }

    val profile by profileViewModel.profile.observeAsState()
    val userName = profile?.displayName?.takeIf { it.isNotBlank() } ?: userDisplayName.ifBlank { "Legend" }
    val plan = profile?.personalizedPlan

    // DEBUG: Print full plan from profile
    println("DEBUG: Full personalized plan:\n${plan ?: "EMPTY"}")

    val workoutForToday = remember(plan) {
        parseDayBlockFlexible(plan)
    }

    // DEBUG: Print parsed workout lines for today
    println("DEBUG: Parsed workout lines for today:\n${workoutForToday?.joinToString("\n") ?: "EMPTY"}")

    var selectedMood by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$dayOfWeek, $formattedDate") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Hey $userName, ready to make today count? 💪",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "How are you feeling right now?",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(4.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    mainAxisSpacing = 8.dp,
                    crossAxisSpacing = 8.dp
                ) {
                    listOf("Fired Up!", "Chill Mode", "Sore But Ready", "Need Motivation").forEach { mood ->
                        AssistChip(
                            onClick = { selectedMood = mood },
                            label = { Text(mood) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selectedMood == mood)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.defaultMinSize(minWidth = 64.dp)
                        )
                    }
                }
                selectedMood?.let {
                    Text(
                        text = when (it) {
                            "Fired Up!" -> "Love the energy! Let’s channel it into some big lifts today. 🚀"
                            "Chill Mode" -> "No worries—steady effort builds greatness. Let’s do our best!"
                            "Sore But Ready" -> "That’s dedication! Let’s warm up and listen to your body."
                            "Need Motivation" -> "Remember why you started. I’m right here with you—let’s go!"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(16.dp))

                if (workoutForToday != null && workoutForToday.isNotEmpty()) {
                    Text(
                        text = "Here’s your battle plan for $dayOfWeek, $formattedDate, $userName:",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    workoutForToday.forEach { line ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = line.trim(),
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { navController.navigate("workoutlog") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Let’s Get Started 💪")
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Remember: form over ego, and have fun out there!",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Text(
                        text = "Rest day? Or did we forget to plan your workout? Either way, your body thanks you!\n\nReady for tomorrow’s greatness, $userName? 🚀",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(top = 32.dp)
                    )
                }
            }
        }
    }
}

fun parseDayBlockFlexible(plan: String?): List<String>? {
    if (plan.isNullOrBlank()) return null

    val dayRegex = Regex(
        "(?s)Day 1:(.*?)(?=Day 2:|Nutrition|Supplements|$)",
        RegexOption.DOT_MATCHES_ALL
    )

    val match = dayRegex.find(plan) ?: return null
    val block = match.groupValues[1]

    if (block.contains("rest", true) || block.contains("cardio", true)) return null

    val lines = block.lines().map { it.trim() }.filter { it.isNotEmpty() }

    val filteredLines = if (lines.firstOrNull()?.startsWith("-") == false) {
        lines.drop(1)  // drop header line if present
    } else {
        lines
    }


    return filteredLines
}


