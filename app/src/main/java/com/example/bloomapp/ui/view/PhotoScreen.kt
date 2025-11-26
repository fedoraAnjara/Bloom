package com.example.bloomapp.ui.view

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.bloomapp.ui.viewmodel.PlantsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoScreen(
    viewModel: PlantsViewModel,
    onBack: () -> Unit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Launcher pour prendre une photo
    val launcherCamera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        bitmap = it
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

        TopAppBar(
            title = { Text("Nouvelle Plante") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                }
            }
        )

        // Aperçu de l'image
        bitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxWidth().height(300.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { launcherCamera.launch() }) {
                Text("Prendre une photo")
            }

            if (bitmap != null) {
                Button(onClick = {
                    viewModel.addPlantWithBitmap("Nouvelle Plante", bitmap)
                    bitmap = null
                    onBack()
                }) {
                    Text("Valider")
                }

                Button(onClick = { bitmap = null }) {
                    Text("Recommencer")
                }
            }
        }
    }
}
