package com.john.gainsgpt.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class WorkoutLog(
    val date: String = getTodayDate(),
    val exerciseName: String = "",
    val suggestedReps: String = "",
    val suggestedWeight: String = "",
    val actualReps: String = "",
    val actualWeight: String = ""
)

private fun getTodayDate(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return formatter.format(Calendar.getInstance().time)
}
