package com.john.gainsgpt.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavHostController,
    googleSignInClient: GoogleSignInClient,
    onLoginSuccess: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var loginError by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        if (authTask.isSuccessful) {
                            val user = FirebaseAuth.getInstance().currentUser
                            val displayName = user?.displayName ?: "User"
                            onLoginSuccess(displayName)
                        } else {
                            loginError = "Firebase sign-in failed: ${authTask.exception?.localizedMessage}"
                            isSigningIn = false
                        }
                    }
            } else {
                loginError = "Missing ID token from Google Sign-In"
                isSigningIn = false
            }
        } catch (e: ApiException) {
            loginError = "Google Sign-In failed: ${e.localizedMessage}"
            isSigningIn = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to GainsGPT 💪",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (loginError != null) {
                Text(
                    text = loginError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = {
                    isSigningIn = true
                    loginError = null
                    val signInIntent: Intent = googleSignInClient.signInIntent
                    signInLauncher.launch(signInIntent)
                },
                enabled = !isSigningIn,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSigningIn) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Signing in...")
                } else {
                    Text("Sign in with Google")
                }
            }
        }
    }
}
