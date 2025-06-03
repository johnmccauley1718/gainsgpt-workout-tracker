package com.john.gainsgpt.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.john.gainsgpt.data.Workout
import com.john.gainsgpt.data.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = WorkoutRepository()

    private val _workout = MutableStateFlow<Workout?>(null)
    val workout: StateFlow<Workout?> get() = _workout

    fun loadWorkoutForDay(day: String) {
        viewModelScope.launch {
            try {
                val workoutForDay = repo.getWorkoutForDay(day)
                _workout.value = workoutForDay
            } catch (e: Exception) {
                _workout.value = null
                // Optionally log error or handle it
            }
        }
    }

    fun getWorkoutForDay(day: String): StateFlow<Workout?> {
        loadWorkoutForDay(day)
        return workout
    }
}
