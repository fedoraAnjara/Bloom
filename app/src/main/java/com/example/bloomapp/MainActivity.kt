package com.example.bloomapp

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.bloomapp.ui.theme.BloomAppTheme
import com.example.bloomapp.ui.view.*
import com.example.bloomapp.ui.screens.PlantDetailScreen
import com.example.bloomapp.ui.viewmodel.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val authViewModel by lazy { AuthViewModel() }
    private var plantsViewModelRef: PlantsViewModel? = null

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
                            MainScope().launch {
                                delay(500)
                                plantsViewModelRef?.loadPlants()
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
            val context = LocalContext.current

            // ViewModel Settings avec factory
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(context)
            )
            val themeMode by settingsViewModel.themeModeFlow.collectAsState(initial = "system")

            val darkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            BloomAppTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                val plantsViewModel: PlantsViewModel = viewModel()
                plantsViewModelRef = plantsViewModel

                val currentUser = FirebaseAuth.getInstance().currentUser
                val startDestination = if (currentUser != null) "home" else "login"

                NavHost(navController = navController, startDestination = startDestination) {

                    composable("login") {
                        LoginScreen(
                            navController = navController,
                            onSignUpClick = { navController.navigate("signup") },
                            viewModel = authViewModel
                        )
                    }

                    composable("signup") {
                        SignUpScreen(
                            navController = navController,
                            onSignInClick = { navController.navigate("login") },
                            viewModel = authViewModel
                        )
                    }

                    composable("home") {
                        HomeScreen(
                            viewModel = plantsViewModel,
                            onAddClick = { navController.navigate("photo") },
                            onLogout = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            },
                            navController = navController
                        )
                    }

                    composable("photo") {
                        PhotoScreen(
                            viewModel = plantsViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "plantDetail/{plantId}",
                        arguments = listOf(navArgument("plantId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val plantId = backStackEntry.arguments?.getString("plantId") ?: ""
                        PlantDetailScreen(
                            plantId = plantId,
                            viewModel = plantsViewModel,
                            navController = navController
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            onBack = { navController.popBackStack() },
                            onLogout = {
                                FirebaseAuth.getInstance().signOut()
                                navController.navigate("login") {
                                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                }
                            },
                            viewModel = settingsViewModel,
                            navController = navController
                        )
                    }

                    composable("privacy") {
                        PrivacyScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("help_support") {
                        HelpSupportScreen(
                            onBack = { navController.popBackStack() },
                            sendEmail = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:support@bloomapp.com")
                                    putExtra(Intent.EXTRA_SUBJECT, "Support BloomApp")
                                }
                                navController.context.startActivity(intent)
                            }
                        )
                    }

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
