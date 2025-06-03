package com.john.gainsgpt.data

import java.time.LocalDate

data class WorkoutLog(
    val date: LocalDate = LocalDate.now(),
    val exerciseName: String = "",
    val suggestedReps: String = "",
    val suggestedWeight: String = "",
    val actualReps: String = "",
    val actualWeight: String = ""
)
