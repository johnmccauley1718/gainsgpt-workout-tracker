package com.john.gainsgpt.data

import androidx.room.*

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ProfileEntity)

    @Update
    suspend fun update(profile: ProfileEntity)

    @Delete
    suspend fun delete(profile: ProfileEntity)

    @Query("SELECT * FROM profile WHERE uid = :uid LIMIT 1")
    suspend fun getProfile(uid: String): ProfileEntity?

    @Query("SELECT * FROM profile LIMIT 1")
    suspend fun getAnyProfile(): ProfileEntity?
}
