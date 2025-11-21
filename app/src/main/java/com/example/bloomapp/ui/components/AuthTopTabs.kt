package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthTopTabs(
    isLoginSelected: Boolean,
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
    ) {
        Button(
            onClick = onLoginClick,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor =
                    if (isLoginSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
            )
        ) {
            Text("Sign In")
        }

        Button(
            onClick = onSignUpClick,
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor =
                    if (!isLoginSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface
            )
        ) {
            Text("Sign Up")
        }
    }
}
