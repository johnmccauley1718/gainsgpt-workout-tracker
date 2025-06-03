package com.john.gainsgpt.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.john.gainsgpt.viewmodel.WorkoutLogViewModel
import com.john.gainsgpt.data.Workout

@Composable
fun WorkoutLogScreen(
    viewModel: WorkoutLogViewModel = hiltViewModel()
) {
    val workouts by viewModel.workouts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Log Today's Workout", style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(12.dp))

        if (workouts.isEmpty()) {
            Text("No workouts scheduled for today.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(workouts) { workout ->
                    WorkoutLogItem(
                        workout = workout,
                        onSetChange = { sets -> viewModel.updateWorkoutLog(workout.id, sets, workout.reps) },
                        onRepChange = { reps -> viewModel.updateWorkoutLog(workout.id, workout.sets, reps) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveWorkoutLogs() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Log")
            }
        }
    }
}

@Composable
fun WorkoutLogItem(
    workout: Workout,
    onSetChange: (Int?) -> Unit,
    onRepChange: (Int?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(workout.name, style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = workout.sets?.toString() ?: "",
                onValueChange = { onSetChange(it.toIntOrNull()) },
                label = { Text("Sets") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = workout.reps?.toString() ?: "",
                onValueChange = { onRepChange(it.toIntOrNull()) },
                label = { Text("Reps") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
