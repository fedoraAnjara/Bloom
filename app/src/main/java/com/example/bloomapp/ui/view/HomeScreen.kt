package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloomapp.R
import com.example.bloomapp.ui.theme.black
import com.example.bloomapp.ui.theme.green
import com.example.bloomapp.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userEmail = currentUser?.email ?: "Utilisateur"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp)
            )

            Spacer(Modifier.height(40.dp))

            Text(
                text = "Bienvenue !",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = black
            )

            Spacer(Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF3F4F6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Vous êtes connecté en tant que :",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = userEmail,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = black
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = green.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✓",
                        fontSize = 48.sp,
                        color = green
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Authentification réussie",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = black
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Vous avez accès à l'application",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    FirebaseAuth.getInstance().signOut()
                    onLogout()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = green),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Se déconnecter", color = black, fontSize = 16.sp)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}