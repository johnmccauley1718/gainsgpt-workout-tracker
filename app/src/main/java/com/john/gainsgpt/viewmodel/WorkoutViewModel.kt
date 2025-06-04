package com.john.gainsgpt.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.john.gainsgpt.api.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    fun fetchWorkout() {
        viewModelScope.launch {
            try {
                val response = apiService.getWorkout()
                println("Workout: $response")
            } catch (e: Exception) {
                println("Error fetching workout: ${e.message}")
            }
        }
    }
}
