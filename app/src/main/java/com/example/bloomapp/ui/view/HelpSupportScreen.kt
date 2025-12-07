package com.example.bloomapp.ui.view

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    sendEmail: () -> Unit // fonction pour lancer l'intent mail
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Aide & Support") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "FAQ",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Liste des FAQ
            val faqs = listOf(
                "Comment ajouter une nouvelle plante ?" to "Allez sur l'écran principal et cliquez sur le bouton + pour ajouter une nouvelle plante.",
                "Comment supprimer mon compte ?" to "Rendez-vous dans Paramètres > Compte > Supprimer le compte. Suivez les instructions.",
                "Comment activer les notifications ?" to "Dans Paramètres > Notifications, activez le switch pour recevoir les rappels d'arrosage."
            )

            faqs.forEach { (question, answer) ->
                FaqItem(question = question, answer = answer)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Contactez le support",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = sendEmail,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Envoyer un email au support", color = Color.White)
            }
        }
    }
}

@Composable
fun FaqItem(
    question: String,
    answer: String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded }
            .animateContentSize()
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black
        )
        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = answer,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}
