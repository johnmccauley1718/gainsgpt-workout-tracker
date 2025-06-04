package com.john.gainsgpt.data

import com.john.gainsgpt.data.Workout
import kotlinx.coroutines.flow.Flow

interface WorkoutRepository {
    suspend fun addWorkout(workout: Workout)
    suspend fun deleteWorkout(workout: Workout)
    fun getAllWorkouts(): Flow<List<Workout>>
}
