package com.john.gainsgpt.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "workout")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val scheduledDate: LocalDate,
    val sets: Int? = null,
    val reps: Int? = null
)
