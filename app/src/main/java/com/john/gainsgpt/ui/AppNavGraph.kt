package com.john.gainsgpt.ui

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.john.gainsgpt.data.ProfileRepository
import com.john.gainsgpt.screens.*
import com.john.gainsgpt.ui.components.LoadingScreen
import com.john.gainsgpt.viewmodel.ProfileViewModel
import com.john.gainsgpt.viewmodel.WorkoutLogViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun AppNavGraph(
    startDestination: String,
    googleSignInClient: GoogleSignInClient,
    onSignInSuccess: (String) -> Unit,
    userDisplayName: String,
    onOnboardingComplete: () -> Unit,
    onboardingComplete: Boolean,
    isSignedIn: Boolean,
    profileRepository: ProfileRepository,
    profileLoaded: Boolean,
    navController: NavHostController = rememberNavController()
) {
    val profileViewModel: ProfileViewModel = viewModel()
    val profile by profileViewModel.profile.observeAsState()

    var pendingPlan by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isSignedIn, onboardingComplete, profileLoaded) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        if (!isSignedIn && currentRoute != "login") {
            navController.navigate("login") { popUpTo(0) }
        } else if (isSignedIn && profileLoaded) {
            when {
                !onboardingComplete && currentRoute != "onboarding" -> {
                    navController.navigate("onboarding") {
                        popUpTo("login") { inclusive = true }
                    }
                }
                onboardingComplete && currentRoute != "profile" && !currentRoute.orEmpty().startsWith("profile/") -> {
                    navController.navigate("profile") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        composable("login") {
            if (isSignedIn && !profileLoaded) {
                LoadingScreen("Loading your profile...")
            } else {
                LoginScreen(
                    navController = navController,
                    googleSignInClient = googleSignInClient,
                    onLoginSuccess = { name ->
                        onSignInSuccess(name)
                        navController.navigate("onboarding")
                    }
                )
            }
        }

        composable("onboarding") {
            OnboardingScreen(
                userName = userDisplayName,
                profileRepository = profileRepository,
                onPlanCreated = { plan ->
                    pendingPlan = plan
                    onOnboardingComplete()
                    navController.navigate("profile/${Uri.encode(plan)}") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("profile") {
            ProfileScreen(
                navController = navController,
                userDisplayName = userDisplayName
            )
        }

        composable(
            "profile/{plan}",
            arguments = listOf(navArgument("plan") { type = NavType.StringType })
        ) {
            ProfileScreen(
                navController = navController,
                userDisplayName = userDisplayName
            )
        }

        composable("todayWorkout") {
            TodayWorkoutScreen(
                navController = navController,
                userDisplayName = userDisplayName,
                profileViewModel = profileViewModel
            )
        }

        composable("workoutlog") {
            val workoutLogViewModel: WorkoutLogViewModel = viewModel()

            WorkoutLogScreen(
                navController = navController,
                rawPlan = profile?.personalizedPlan,
                workoutLogViewModel = workoutLogViewModel
            )
        }

        composable("chat") {
            ChatScreen(
                userName = userDisplayName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
