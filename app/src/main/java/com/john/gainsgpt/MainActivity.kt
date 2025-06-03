package com.john.gainsgpt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.john.gainsgpt.data.ProfileRepository
import com.john.gainsgpt.ui.AppNavGraph
import com.john.gainsgpt.ui.theme.GainsGPTTheme
import com.john.gainsgpt.viewmodel.ProfileViewModel

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var profileRepository: ProfileRepository
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(this, gso)
        firebaseAuth = FirebaseAuth.getInstance()
        profileRepository = ProfileRepository(applicationContext)

        setContent {
            GainsGPTTheme {
                val firebaseUser = firebaseAuth.currentUser
                val isSignedIn = firebaseUser != null
                val userDisplayName = firebaseUser?.displayName ?: ""

                val profileState by profileViewModel.profile.observeAsState()
                val onboardingComplete = profileState?.onboardingComplete ?: false
                val profileLoaded = profileState != null

                LaunchedEffect(isSignedIn) {
                    if (isSignedIn) profileViewModel.loadProfile()
                }

                AppNavGraph(
                    startDestination = "login",
                    googleSignInClient = googleSignInClient,
                    onSignInSuccess = { name ->
                        profileViewModel.setDisplayName(name)
                    },
                    userDisplayName = userDisplayName,
                    onOnboardingComplete = {
                        profileViewModel.markOnboardingComplete()
                    },
                    onboardingComplete = onboardingComplete,
                    isSignedIn = isSignedIn,
                    profileRepository = profileRepository,
                    profileLoaded = profileLoaded
                )
            }
        }
    }
}
