package com.john.gainsgpt.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.john.gainsgpt.R
import com.john.gainsgpt.data.ChatGPTService
import com.john.gainsgpt.data.Profile
import com.john.gainsgpt.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectAsState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    navController: NavHostController,
    userDisplayName: String,
    plan: String? = null,
    profileViewModel: ProfileViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    val context = LocalContext.current
    var isLoggingOut by remember { mutableStateOf(false) }
    val profile by profileViewModel.profile.collectAsState()
    var chatInput by remember { mutableStateOf("") }
    var isChatLoading by remember { mutableStateOf(false) }
    var chatError by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val nameToShow = profile?.displayName?.takeIf { it.isNotBlank() }
        ?: userDisplayName.ifBlank { "Athlete" }
    val emailToShow = profile?.email ?: "No email found—are you a secret agent?"

    val rawPlan = profile?.personalizedPlan ?: plan.orEmpty()

    val planWithDates = remember(rawPlan) {
        if (rawPlan.isBlank()) return@remember ""
        val dayRegex = Regex("(?i)Day (\\d+)")
        val startDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        val dowFormat = SimpleDateFormat("EEEE", Locale.getDefault())

        val matches = dayRegex.findAll(rawPlan).toList()
        val dayNumberToDate = matches
            .map { it.groupValues[1].toIntOrNull() ?: 1 }
            .distinct()
            .associateWith { dayNum ->
                val calendar = Calendar.getInstance()
                calendar.time = startDate
                calendar.add(Calendar.DATE, dayNum - 1)
                val dow = dowFormat.format(calendar.time)
                val pretty = dateFormat.format(calendar.time)
                "$dow, $pretty"
            }

        dayRegex.replace(rawPlan) { match ->
            val n = match.groupValues[1].toIntOrNull() ?: 1
            dayNumberToDate[n] ?: match.value
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Hey, $nameToShow! 👋", style = MaterialTheme.typography.headlineMedium)
                Text(emailToShow, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Your Personalized Game Plan 🎯", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (planWithDates.isNotBlank()) planWithDates
                            else "No plan yet! Let’s build something epic together.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            Text("Want to tweak your plan or just chat? Ask me anything!",
                style = MaterialTheme.typography.titleSmall
            )

            chatError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
            }

            OutlinedTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                label = { Text("Ask your AI Coach…") },
                placeholder = { Text("E.g., Make my leg day harder!") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Button(
                    enabled = chatInput.isNotBlank() && !isChatLoading,
                    onClick = {
                        chatError = null
                        isChatLoading = true
                        coroutineScope.launch {
                            val existingPlan = profile?.personalizedPlan ?: plan.orEmpty()
                            val onboardingStatus = if (profile?.onboardingComplete == true) "complete" else "incomplete"
                            val promptWithContext = """
                                You are an expert fitness coach. Use the following user information to update their personalized workout plan.

                                User Info:
                                - Name: $nameToShow
                                - Email: $emailToShow
                                - Onboarding Status: $onboardingStatus

                                Current Personalized Plan:
                                $existingPlan

                                User's question/request:
                                $chatInput

                                Provide a full updated personalized workout plan based on the above.
                            """.trimIndent()

                            val response = ChatGPTService.buildPlan(promptWithContext)
                            if (response != null) {
                                val updatedProfile = profile?.copy(personalizedPlan = response)
                                    ?: Profile(
                                        uid = profile?.uid ?: "",
                                        displayName = nameToShow,
                                        email = emailToShow,
                                        onboardingComplete = true,
                                        personalizedPlan = response
                                    )
                                profileViewModel.saveProfile(updatedProfile)
                                chatInput = ""
                            } else {
                                chatError = "Coach couldn’t update your plan—try again in a sec!"
                            }
                            isChatLoading = false
                        }
                    }
                ) {
                    if (isChatLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Send to Coach")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { navController.navigate("todayWorkout") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Show Me Today’s Game Plan 🏋️‍♂️")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { navController.navigate("onboarding") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Re-Run Onboarding")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                enabled = !isLoggingOut,
                onClick = {
                    isLoggingOut = true
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInClient.revokeAccess().addOnCompleteListener {
                        googleSignInClient.signOut().addOnCompleteListener {
                            isLoggingOut = false
                            navController.navigate("login") {
                                popUpTo("profile") { inclusive = true }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoggingOut) "Logging Out…" else "Logout (See you at the next workout!)")
            }
        }
    }
}
