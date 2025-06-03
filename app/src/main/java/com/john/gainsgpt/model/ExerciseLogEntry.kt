package com.john.gainsgpt.model

data class ExerciseLogEntry(
    val exerciseName: String,
    val suggestedSets: String,
    val suggestedReps: String,
    val suggestedWeight: String,
    var actualSets: String = "",
    var actualReps: String = "",
    var actualWeight: String = ""
)
