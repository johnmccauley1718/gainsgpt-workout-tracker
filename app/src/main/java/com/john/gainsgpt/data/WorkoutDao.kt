package com.john.gainsgpt.data

import androidx.room.*
import java.time.LocalDate

@Dao
interface WorkoutDao {

    @Query("SELECT * FROM workout WHERE scheduledDate = :date")
    suspend fun getWorkoutsByDate(date: LocalDate): List<Workout>

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    @Query("SELECT * FROM workout")
    suspend fun getAllWorkouts(): List<Workout>

    @Delete
    suspend fun deleteWorkout(workout: Workout)
}
