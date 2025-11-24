package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloomapp.R
import com.example.bloomapp.ui.theme.black
import com.example.bloomapp.ui.theme.green
import com.example.bloomapp.ui.theme.grey
import com.example.bloomapp.ui.viewmodel.AuthViewModel
import com.example.ui.components.GoogleButton

@Composable
fun SignUpScreen(
    onSignInClick: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    onGoogleClick: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(90.dp)
        )

        Spacer(Modifier.height(30.dp))

        /** Onglets **/
        Box(
            modifier = Modifier
                .width(350.dp)
                .height(50.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                Modifier
                    .width(340.dp)
                    .height(45.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Sign In
                Button(
                    onClick = onSignInClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = grey)
                ) {
                    Text("Sign In", color = black)
                }

                // Sign Up active
                Button(
                    onClick = { /* déjà ici */ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green)
                ) {
                    Text("Sign Up", color = black)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // Affichage du message d'erreur
        if (errorMessage.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFC62828),
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = ""
            },
            label = { Text("Email Address") },
            placeholder = { Text("enter your email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            label = { Text("Password") },
            placeholder = { Text("enter your password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = ""
            },
            label = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(Modifier.height(26.dp))

        Button(
            onClick = {
                // Validation des champs
                when {
                    email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                        errorMessage = "Veuillez remplir tous les champs"
                        return@Button
                    }
                    password != confirmPassword -> {
                        errorMessage = "Les mots de passe ne correspondent pas"
                        return@Button
                    }
                    password.length < 6 -> {
                        errorMessage = "Le mot de passe doit contenir au moins 6 caractères"
                        return@Button
                    }
                }

                isLoading = true
                errorMessage = ""

                viewModel.signUp(
                    email = email,
                    password = password,
                    onSuccess = {
                        isLoading = false
                        onSignUpSuccess()
                    },
                    onError = { error ->
                        isLoading = false
                        errorMessage = error
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = green),
            shape = RoundedCornerShape(10.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = black
                )
            } else {
                Text("Sign Up", color = black)
            }
        }
        GoogleButton(
            enabled = !isLoading,
            onClick = onGoogleClick
        )
    }
}