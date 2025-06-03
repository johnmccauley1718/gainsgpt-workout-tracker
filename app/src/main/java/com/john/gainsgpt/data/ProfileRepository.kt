package com.john.gainsgpt.data

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Profile(
    val uid: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val onboardingComplete: Boolean = false,
    val personalizedPlan: String? = null
)

// Extension functions to convert between Entity and Model
fun ProfileEntity.toModel() = Profile(
    uid = uid,
    displayName = displayName,
    email = email,
    onboardingComplete = onboardingComplete,
    personalizedPlan = personalizedPlan
)

fun Profile.toEntity() = ProfileEntity(
    uid = uid,
    displayName = displayName,
    email = email,
    onboardingComplete = onboardingComplete,
    personalizedPlan = personalizedPlan
)

class ProfileRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val profileDao = db.profileDao()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun getLocalProfile(): Profile? {
        val uid = auth.currentUser?.uid ?: return null
        val entity = profileDao.getProfile(uid) ?: return null  // using getProfile here
        return entity.toModel()
    }

    suspend fun saveLocalProfile(profile: Profile) {
        profileDao.insert(profile.toEntity())  // insert with REPLACE strategy
    }

    suspend fun syncProfileWithFirestore(profile: Profile) {
        val uid = profile.uid
        firestore.collection("profiles").document(uid).set(profile).await()
    }

    suspend fun fetchProfileFromFirestore(): Profile? {
        val uid = auth.currentUser?.uid ?: return null
        val doc = firestore.collection("profiles").document(uid).get().await()
        println("DEBUG: Raw Firestore data = ${doc.data}")
        val profile = doc.toObject(Profile::class.java)
        println("DEBUG: Parsed Profile from Firestore = $profile")
        return profile
    }


    suspend fun sync(): Profile? {
        val uid = auth.currentUser?.uid ?: return null
        val remote = fetchProfileFromFirestore()
        return if (remote != null) {
            saveLocalProfile(remote)
            remote
        } else {
            val local = getLocalProfile()
            if (local != null) syncProfileWithFirestore(local)
            local
        }
    }

    suspend fun saveOnboardingComplete(plan: String) {
        val uid = auth.currentUser?.uid ?: return
        val displayName = auth.currentUser?.displayName
        val email = auth.currentUser?.email

        val updatedProfile = Profile(
            uid = uid,
            displayName = displayName,
            email = email,
            onboardingComplete = true,
            personalizedPlan = plan
        )

        saveLocalProfile(updatedProfile)
        syncProfileWithFirestore(updatedProfile)
    }

    suspend fun isOnboardingComplete(): Boolean {
        val profile = sync()
        return profile?.onboardingComplete ?: false
    }

    suspend fun getPersonalizedPlan(): String? {
        val profile = sync()
        return profile?.personalizedPlan
    }
}
