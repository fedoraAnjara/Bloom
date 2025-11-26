package com.example.bloomapp.ui.view

import HomeTopBar
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bloomapp.ui.components.DrawerItem
import com.example.bloomapp.ui.components.PlantHomeCard
import com.example.bloomapp.ui.viewmodel.PlantsViewModel
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PlantsViewModel = viewModel(),
    onAddClick: () -> Unit,
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val plants = viewModel.filteredPlants()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 45.dp, bottom = 25.dp)
            ) {
                // Header du menu
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🌿 BLOOM",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFF4CAF50)
                    )
                    Divider()
                }

                // Items du menu
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DrawerItem("Mes Plantes") {
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Ajouter une Plante") {
                        onAddClick()
                        scope.launch { drawerState.close() }
                    }
                    DrawerItem("Paramètres") {
                        // TODO: gérer paramètres
                        scope.launch { drawerState.close() }
                    }

                    Spacer(Modifier.weight(1f)) // pousse le logout en bas

                    Divider()

                    DrawerItem("Déconnexion", color = Color.Red) {
                        onLogout()
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open()
                            else drawerState.close()
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = Color(0xFF4CAF50),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Ajouter",
                        tint = Color.White
                    )
                }
            },
            containerColor = Color(0xFFF5F5F5)
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(plants) { plant ->
                    PlantHomeCard(plant)
                }
            }
        }
    }
}
