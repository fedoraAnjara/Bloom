package com.example.bloomapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bloomapp.ui.viewmodel.AuthViewModel
import com.example.ui.HomeScreen
import com.example.ui.LoginScreen
import com.example.ui.SignUpScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    val viewModel = ViewModelProvider(this)[AuthViewModel::class.java]

                    viewModel.signInWithGoogle(
                        idToken,
                        onSuccess = {
                            Toast.makeText(this, "Connexion Google réussie", Toast.LENGTH_SHORT).show()
                        },
                        onError = {
                            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                        }
                    )
                }

            } catch (e: Exception) {
                Toast.makeText(this, e.message ?: "Erreur Google", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 🔥 Configuration Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "login") {

                composable("login") {
                    LoginScreen(
                        onSignUpClick = { navController.navigate("signup") },
                        onLoginSuccess = {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onGoogleClick = {
                            startGoogleSignIn()
                        }
                    )
                }

                composable("signup") {
                    SignUpScreen(
                        onSignInClick = { navController.navigate("login") },
                        onSignUpSuccess = {
                            navController.navigate("home") {
                                popUpTo("signup") { inclusive = true }
                            }
                        }
                    )
                }

                composable("home") {
                    HomeScreen(
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun startGoogleSignIn() {
        launcher.launch(googleSignInClient.signInIntent)
    }
}
