package com.john.gainsgpt.data


import javax.inject.Inject
import javax.inject.Singleton
import java.time.LocalDate

@Singleton
class WorkoutRepositoryImpl @Inject constructor(
    private val dao: WorkoutDao
) : WorkoutRepository {

    override suspend fun getWorkoutsForDate(date: LocalDate): List<Workout> {
        return dao.getWorkoutsByDate(date)
    }

    override suspend fun saveWorkoutLogs(workouts: List<Workout>) {
        workouts.forEach { dao.updateWorkout(it) }
    }
}
