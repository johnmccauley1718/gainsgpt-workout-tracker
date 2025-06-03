package com.john.gainsgpt.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class WorkoutLogRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun saveWorkoutLogs(uid: String, logs: List<WorkoutLog>) {
        val batch = firestore.batch()
        val collectionRef = firestore.collection("users")
            .document(uid)
            .collection("workoutLogs")

        logs.forEach { log ->
            // Use date + exercise name as a unique doc ID
            val docId = "${log.date}_${log.exerciseName}".replace(" ", "_")
            val docRef = collectionRef.document(docId)

            // Convert to Map with stringified date
            val data = mapOf(
                "date" to log.date.toString(),
                "exerciseName" to log.exerciseName,
                "suggestedReps" to log.suggestedReps,
                "suggestedWeight" to log.suggestedWeight,
                "actualReps" to log.actualReps,
                "actualWeight" to log.actualWeight
            )

            batch.set(docRef, data)
        }

        batch.commit().await()
    }

    suspend fun getWorkoutLogsForDate(uid: String, date: LocalDate): List<WorkoutLog> {
        val collectionRef = firestore.collection("users")
            .document(uid)
            .collection("workoutLogs")

        val snapshot = collectionRef
            .whereEqualTo("date", date.toString()) // Firestore stores it as string
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                WorkoutLog(
                    date = LocalDate.parse(doc.getString("date") ?: return@mapNotNull null),
                    exerciseName = doc.getString("exerciseName") ?: "",
                    suggestedReps = doc.getString("suggestedReps") ?: "",
                    suggestedWeight = doc.getString("suggestedWeight") ?: "",
                    actualReps = doc.getString("actualReps") ?: "",
                    actualWeight = doc.getString("actualWeight") ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
