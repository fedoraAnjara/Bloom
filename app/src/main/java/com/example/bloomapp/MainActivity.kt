package com.example.bloomapp

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bloomapp.ui.viewmodel.AuthViewModel
import com.example.bloomapp.ui.view.HomeScreen
import com.example.bloomapp.ui.view.LoginScreen
import com.example.bloomapp.ui.view.PhotoScreen
import com.example.bloomapp.ui.view.SignUpScreen
import com.example.bloomapp.ui.viewmodel.PlantsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val authViewModel by lazy { ViewModelProvider(this)[AuthViewModel::class.java] }
    private var navControllerRef: NavController? = null

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    authViewModel.signInWithGoogle(
                        idToken,
                        onSuccess = {
                            Toast.makeText(this, "Connexion Google réussie", Toast.LENGTH_SHORT).show()
                            navControllerRef?.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onError = { message ->
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(this, e.message ?: "Erreur Google", Toast.LENGTH_SHORT).show()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configuration Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            val navController = rememberNavController()
            navControllerRef = navController

            // Vérifie si un utilisateur est déjà connecté
            val lastGoogleAccount = GoogleSignIn.getLastSignedInAccount(this)
            val currentUser = FirebaseAuth.getInstance().currentUser
            val startDestination = if (currentUser != null) "home" else "login"

            NavHost(navController = navController, startDestination = startDestination) {

                composable("login") {
                    LoginScreen(
                        onSignUpClick = { navController.navigate("signup") },
                        onLoginSuccess = {
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onGoogleClick = { startGoogleSignIn() }
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
                        onAddClick = { navController.navigate("photo") }, // ← Navigation vers PhotoScreen
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            }
                        }
                    )
                }

                composable("photo") {
                    val plantsViewModel: PlantsViewModel = viewModel()
                    PhotoScreen(
                        viewModel = plantsViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    private fun startGoogleSignIn() {
        val intent = googleSignInClient.signInIntent.apply {
            putExtra("prompt", "select_account")
        }
        launcher.launch(intent)
    }
}
