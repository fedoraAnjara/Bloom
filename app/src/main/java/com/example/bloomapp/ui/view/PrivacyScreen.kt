package com.example.bloomapp.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confidentialité") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Introduction
            Text(
                text = "Politique de confidentialité",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )

            Text(
                text = "Dernière mise à jour : ${getCurrentDate()}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Text(
                text = "BLOOM respecte votre vie privée et s'engage à protéger vos données personnelles. Cette politique explique comment nous collectons, utilisons et protégeons vos informations.",
                style = MaterialTheme.typography.bodyMedium
            )

            Divider()

            // 1. Données collectées
            PrivacySection(
                title = "1. Données que nous collectons",
                content = """
                    Nous collectons les informations suivantes :
                    
                    • Informations de compte : adresse e-mail et mot de passe chiffré
                    • Photos de plantes : images que vous téléchargez dans l'application
                    • Données de plantes : noms, descriptions, dates d'ajout
                    • Préférences : langue, thème, paramètres de notification
                    
                    Toutes ces données sont stockées de manière sécurisée sur Firebase.
                """.trimIndent()
            )

            // 2. Utilisation des données
            PrivacySection(
                title = "2. Comment nous utilisons vos données",
                content = """
                    Vos données sont utilisées pour :
                    
                    • Vous fournir les fonctionnalités de l'application
                    • Analyser vos photos de plantes avec l'intelligence artificielle
                    • Sauvegarder votre collection de plantes
                    • Personnaliser votre expérience (thème, langue)
                    • Améliorer nos services
                    
                    Nous n'utilisons jamais vos données à des fins publicitaires.
                """.trimIndent()
            )

            // 3. Partage des données
            PrivacySection(
                title = "3. Partage de vos données",
                content = """
                    Vos données ne sont JAMAIS vendues à des tiers.
                    
                    Nous partageons vos données uniquement avec :
                    
                    • Firebase (Google) : pour le stockage sécurisé et l'authentification
                    • OpenAI : pour l'analyse des images de plantes (l'API ne stocke pas vos images)
                    
                    Ces services sont conformes au RGPD et protègent vos données.
                """.trimIndent()
            )

            // 4. Sécurité
            PrivacySection(
                title = "4. Sécurité de vos données",
                content = """
                    Nous prenons la sécurité au sérieux :
                    
                    • Mots de passe chiffrés avec Firebase Authentication
                    • Connexion HTTPS sécurisée
                    • Base de données protégée avec règles de sécurité Firebase
                    • Accès à vos données limité à votre compte uniquement
                    
                    Aucun employé ne peut accéder à vos données personnelles.
                """.trimIndent()
            )

            // 5. Vos droits
            PrivacySection(
                title = "5. Vos droits (RGPD)",
                content = """
                    Conformément au RGPD, vous avez le droit de :
                    
                    • Accéder à vos données personnelles
                    • Rectifier vos informations
                    • Supprimer votre compte et toutes vos données
                    • Exporter vos données (format JSON)
                    • Vous opposer au traitement de vos données
                    
                    Pour exercer ces droits, rendez-vous dans les Paramètres de l'application.
                """.trimIndent()
            )

            // 6. Conservation des données
            PrivacySection(
                title = "6. Conservation des données",
                content = """
                    • Vos données sont conservées tant que votre compte est actif
                    • À la suppression de votre compte, toutes vos données sont immédiatement et définitivement supprimées
                    • Aucune sauvegarde n'est conservée après suppression
                """.trimIndent()
            )

            // 7. Cookies
            PrivacySection(
                title = "7. Cookies et traceurs",
                content = """
                    BLOOM n'utilise AUCUN cookie de tracking ou de publicité.
                    
                    Les seules données stockées localement sont :
                    • Vos préférences (thème, langue)
                    • Votre session de connexion (pour rester connecté)
                    
                    Ces données sont stockées uniquement sur votre appareil.
                """.trimIndent()
            )

            // 8. Mineurs
            PrivacySection(
                title = "8. Protection des mineurs",
                content = """
                    BLOOM est accessible à tous.
                    
                    Si vous avez moins de 18 ans, nous vous recommandons d'utiliser l'application avec l'accord d'un parent ou tuteur légal.
                """.trimIndent()
            )

            // 9. Modifications
            PrivacySection(
                title = "9. Modifications de cette politique",
                content = """
                    Nous pouvons mettre à jour cette politique de confidentialité.
                    
                    En cas de changements importants, nous vous en informerons via l'application.
                    
                    Votre utilisation continue de BLOOM après modification signifie que vous acceptez les nouvelles conditions.
                """.trimIndent()
            )

            // 10. Contact
            PrivacySection(
                title = "10. Nous contacter",
                content = """
                    Pour toute question sur cette politique ou vos données :
                    
                    📧 Email : privacy@bloomapp.com
                    🌐 Site web : www.bloomapp.com
                    
                    Nous nous engageons à répondre dans les 48 heures.
                """.trimIndent()
            )

            Divider()

            // Résumé final
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🌿 En résumé",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "• Vos données vous appartiennent",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Nous ne vendons rien à personne",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Vous pouvez tout supprimer à tout moment",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• Vos données sont sécurisées",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun PrivacySection(
    title: String,
    content: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
        )
    }
}

fun getCurrentDate(): String {
    val calendar = java.util.Calendar.getInstance()
    val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val month = when (calendar.get(java.util.Calendar.MONTH)) {
        0 -> "janvier"
        1 -> "février"
        2 -> "mars"
        3 -> "avril"
        4 -> "mai"
        5 -> "juin"
        6 -> "juillet"
        7 -> "août"
        8 -> "septembre"
        9 -> "octobre"
        10 -> "novembre"
        11 -> "décembre"
        else -> ""
    }
    val year = calendar.get(java.util.Calendar.YEAR)
    return "$day $month $year"
}