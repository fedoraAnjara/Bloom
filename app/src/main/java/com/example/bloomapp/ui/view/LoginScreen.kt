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
import com.example.bloomapp.R
import com.example.bloomapp.ui.theme.black
import com.example.bloomapp.ui.theme.green
import com.example.bloomapp.ui.theme.grey

@Composable
fun LoginScreen() {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignIn by remember { mutableStateOf(true) }

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

        Box(
            modifier = Modifier
            .width(350.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp ))
            .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ){
            Row(
                Modifier
                    .width(340.dp)
                    .height(45.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Sign In
                Button(
                    onClick = { isSignIn = true },
                    modifier = Modifier
                        .weight(1f),

                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (isSignIn) green
                            else grey
                    )
                ) {
                    Text("Sign In",
                        color = black)
                }

                // Sign Up
                Button(
                    onClick = { isSignIn = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = 12.dp, bottomEnd = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor =
                            if (!isSignIn) green
                            else grey
                    )
                ) {
                    Text("Sign Up",
                        color = black
                    )
                }
            }
        }


        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            placeholder = { Text("enter your email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("enter your password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(26.dp))

        Button(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = green
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(if (isSignIn) "Sign In" else "Sign Up",
                color = black)
        }

        Spacer(Modifier.height(26.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(Modifier.weight(1f))
            Text("  OR  ")
            Divider(Modifier.weight(1f))
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = black
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.google),
                contentDescription = "Google icon",
                tint = black
            )
            Spacer(Modifier.width(8.dp))
            Text("Continue with Google")
        }
    }
}
