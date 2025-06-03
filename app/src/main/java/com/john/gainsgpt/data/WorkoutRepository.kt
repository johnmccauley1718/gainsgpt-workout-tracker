package com.john.gainsgpt.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


data class Exercise(
    val name: String = "",
    val sets: Int = 0,
    val reps: Int = 0,
    val weight: Float? = null
)

data class Workout(
    val day: String = "",
    val exercises: List<Exercise> = emptyList()
)

suspend fun getWorkoutsForDate(date: LocalDate): List<Workout>
suspend fun saveWorkoutLogs(workouts: List<Workout>)

class WorkoutRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Fetch workout plan for a given day from Firestore
    suspend fun getWorkoutForDay(day: String): Workout? {
        val docSnapshot = firestore.collection("workoutPlans")
            .document(day)
            .get()
            .await()

        return if (docSnapshot.exists()) {
            docSnapshot.toObject(Workout::class.java)
        } else {
            null
        }
    }
}
