package com.example.bloomapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.bloomapp.R
import com.example.bloomapp.ui.theme.black

@Composable
fun GoogleButton(
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        enabled = enabled
    ) {
        Icon(
            painter = painterResource(id = R.drawable.google),
            contentDescription = "Google icon",
            tint = Color.Unspecified
        )
        Spacer(Modifier.width(8.dp))
        Text("Continue with Google", color = black)
    }
}

