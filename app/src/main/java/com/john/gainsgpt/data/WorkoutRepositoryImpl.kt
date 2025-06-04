package com.john.gainsgpt.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.john.gainsgpt.data.Workout
import com.john.gainsgpt.data.WorkoutLog
import com.john.gainsgpt.data.WorkoutRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutRepositoryImpl : WorkoutRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override suspend fun addWorkout(workout: Workout) {
        val workoutRef = firestore.collection("workouts").document()
        workoutRef.set(workout).await()
    }

    override suspend fun deleteWorkout(workout: Workout) {
        val collectionRef = firestore.collection("workouts")
        val snapshot = collectionRef
            .whereEqualTo("name", workout.name)
            .get()
            .await()

        for (doc in snapshot.documents) {
            doc.reference.delete().await()
        }
    }

    override fun getAllWorkouts(): Flow<List<Workout>> = callbackFlow {
        val listener = firestore.collection("workouts")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    close(error ?: Exception("Firestore error"))
                    return@addSnapshotListener
                }

                val workouts = snapshot.documents.mapNotNull { it.toObject(Workout::class.java) }
                trySend(workouts).isSuccess
            }

        awaitClose { listener.remove() }
    }
}
