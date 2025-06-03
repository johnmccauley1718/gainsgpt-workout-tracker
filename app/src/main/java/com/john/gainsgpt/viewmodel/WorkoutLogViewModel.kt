package com.john.gainsgpt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.john.gainsgpt.data.Workout
import com.john.gainsgpt.data.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class WorkoutLogViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _workouts = MutableStateFlow<List<Workout>>(emptyList())
    val workouts: StateFlow<List<Workout>> = _workouts.asStateFlow()

    init {
        loadTodayWorkouts()
    }

    private fun loadTodayWorkouts() {
        viewModelScope.launch {
            val today = LocalDate.now()
            _workouts.value = repository.getWorkoutsForDate(today)
        }
    }

    fun updateWorkoutLog(id: Int, sets: Int?, reps: Int?) {
        _workouts.update { list ->
            list.map {
                if (it.id == id) it.copy(sets = sets, reps = reps) else it
            }
        }
    }

    fun saveWorkoutLogs() {
        viewModelScope.launch {
            repository.saveWorkoutLogs(_workouts.value)
        }
    }
}
