package com.john.gainsgpt.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val onboardingComplete: Boolean = false,
    val personalizedPlan: String? = null
)
