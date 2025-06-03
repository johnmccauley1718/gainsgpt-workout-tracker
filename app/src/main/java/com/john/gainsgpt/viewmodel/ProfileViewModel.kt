package com.john.gainsgpt.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.john.gainsgpt.data.Profile
import com.john.gainsgpt.data.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ProfileRepository(application)

    private val _profile = MutableLiveData<Profile?>()
    val profile: LiveData<Profile?> get() = _profile

    fun loadProfile() {
        viewModelScope.launch {
            val loadedProfile = repo.sync()
            _profile.value = loadedProfile
        }
    }

    fun saveProfile(profile: Profile) {
        viewModelScope.launch {
            repo.saveLocalProfile(profile)
            repo.syncProfileWithFirestore(profile)
            _profile.value = profile
        }
    }

    fun updatePersonalizedPlan(plan: String) {
        val current = _profile.value
        val updatedProfile = current?.copy(personalizedPlan = plan)
            ?: Profile(
                uid = "", displayName = null, email = null,
                onboardingComplete = true, personalizedPlan = plan
            )
        saveProfile(updatedProfile)
    }

    fun setDisplayName(name: String) {
        val current = _profile.value
        if (current != null) {
            val updated = current.copy(displayName = name)
            saveProfile(updated)
        }
    }

    fun markOnboardingComplete() {
        val current = _profile.value
        if (current != null) {
            val updated = current.copy(onboardingComplete = true)
            saveProfile(updated)
        }
    }
}
