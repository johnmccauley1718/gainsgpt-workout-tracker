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
            try {
                val loadedProfile = repo.sync()
                _profile.postValue(loadedProfile)
            } catch (e: Exception) {
                e.printStackTrace()
                _profile.postValue(null)
            }
        }
    }

    fun saveProfile(profile: Profile) {
        viewModelScope.launch {
            try {
                repo.saveLocalProfile(profile)
                repo.syncProfileWithFirestore(profile)
                _profile.postValue(profile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePersonalizedPlan(plan: String) {
        val current = _profile.value
        val updated = current?.copy(personalizedPlan = plan)
            ?: Profile(
                uid = "", displayName = null, email = null,
                onboardingComplete = true, personalizedPlan = plan
            )
        saveProfile(updated)
    }

    fun setDisplayName(name: String) {
        val current = _profile.value
        if (current != null) {
            saveProfile(current.copy(displayName = name))
        }
    }

    fun markOnboardingComplete() {
        val current = _profile.value
        if (current != null) {
            saveProfile(current.copy(onboardingComplete = true))
        }
    }
}
