package com.example.bloomapp.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.bloomapp.ui.theme.black
import com.example.bloomapp.ui.theme.green
import com.example.bloomapp.ui.viewmodel.PlantsViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plantId: String,
    viewModel: PlantsViewModel,
    navController: NavController
) {
    val plant = viewModel.getPlantById(plantId)
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Dialog de confirmation de suppression
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Supprimer la plante ?") },
            text = {
                Text("Êtes-vous sûr de vouloir supprimer \"${plant?.name ?: "cette plante"}\" ? Cette action est irréversible.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deletePlant(plantId) { success ->
                            if (success) {
                                navController.popBackStack()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Découvre les détails") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Retour")
                    }
                },
                actions = {
                    // Bouton Partager
                    IconButton(
                        onClick = {
                            plant?.let { sharePlant(context, it) }
                        }
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Partager",
                            tint = black
                        )
                    }

                    // Bouton Supprimer
                    IconButton(
                        onClick = { showDeleteDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Supprimer",
                            tint = black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        if (plant == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Plante introuvable")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Image de la plante (gestion du nullable)
            plant.imageUrl?.let { base64 ->
                if (base64.isNotEmpty()) {
                        val bytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        bitmap?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = plant.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .padding(20.dp,15.dp)
                                    .clip(RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp,topStart = 15.dp, topEnd = 15.dp))
                            )
                        }
                }
            }

            plant.name?.let { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            plant.date?.let { timestamp ->
                val dateFormat = SimpleDateFormat("dd MMMM yyyy, 'à' HH'h'mm", Locale.FRENCH)
                val formattedDate = dateFormat.format(timestamp.toDate())

                Text(
                    text = "Découvert : $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Contenu
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Badge si ce n'est pas une plante
                if (!plant.isPlant) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, "Attention", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Ce n'est pas une plante",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Nom scientifique et famille
                plant.scientificName?.takeIf { it.isNotEmpty() }?.let { scientificName ->
                    DetailCard(
                        icon = Icons.Default.Science,
                        title = "Nom scientifique",
                        content = scientificName
                    )
                }

                plant.family?.takeIf { it.isNotEmpty() }?.let { family ->
                    DetailCard(
                        icon = Icons.Default.FamilyRestroom,
                        title = "Famille",
                        content = family
                    )
                }

                // Description
                plant.fullDetails?.takeIf { it.isNotEmpty() }?.let { details ->
                    DetailCard(
                        icon = Icons.Default.Info,
                        title = "Description",
                        content = details
                    )
                }

                // Besoins en lumière
                plant.sunlightNeeds?.takeIf { it.isNotEmpty() }?.let { sunlight ->
                    DetailCard(
                        icon = Icons.Default.WbSunny,
                        title = "Lumière",
                        content = sunlight
                    )
                }

                // Arrosage
                plant.wateringFrequency?.takeIf { it.isNotEmpty() }?.let { watering ->
                    DetailCard(
                        icon = Icons.Default.Water,
                        title = "Arrosage",
                        content = watering
                    )
                }

                // Instructions d'entretien
                plant.careInstructions?.takeIf { it.isNotEmpty() }?.let { care ->
                    DetailCard(
                        icon = Icons.Default.Spa,
                        title = "Entretien",
                        content = care
                    )
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = green
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = black
                )
            }
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = black
            )
        }
    }
}

// Fonction pour partager la plante
private fun sharePlant(context: android.content.Context, plant: com.example.bloomapp.data.model.Plant) {
    try {
        // Créer le texte à partager
        val shareText = buildString {
            append("🌱 ${plant.name ?: "Ma plante"}\n\n")

            plant.scientificName?.takeIf { it.isNotEmpty() }?.let {
                append("🔬 Nom scientifique: $it\n")
            }

            plant.family?.takeIf { it.isNotEmpty() }?.let {
                append("👨‍👩‍👧‍👦 Famille: $it\n\n")
            }

            plant.fullDetails?.takeIf { it.isNotEmpty() }?.let {
                append("📝 Description:\n$it\n\n")
            }

            plant.sunlightNeeds?.takeIf { it.isNotEmpty() }?.let {
                append("☀️ Lumière: $it\n")
            }

            plant.wateringFrequency?.takeIf { it.isNotEmpty() }?.let {
                append("💧 Arrosage: $it\n\n")
            }

            plant.careInstructions?.takeIf { it.isNotEmpty() }?.let {
                append("🌿 Entretien:\n$it\n")
            }

            append("\n📱 Partagé depuis BloomApp")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_TITLE, plant.name ?: "Ma plante")
        }

        //Ajouter l'image si possible
        plant.imageUrl?.let { base64 ->
            try {
                val bytes = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                // Sauvegarder temporairement l'image
                val cachePath = File(context.cacheDir, "images")
                cachePath.mkdirs()
                val file = File(cachePath, "plant_${plant.id}.jpg")

                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }

                val imageUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                shareIntent.type = "image/*"
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) {
                // Si l'image échoue, partager juste le texte
                e.printStackTrace()
            }
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Partager via")
        )

    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(
            context,
            "Erreur lors du partage",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
}