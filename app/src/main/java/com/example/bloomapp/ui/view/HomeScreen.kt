package com.example.bloomapp.ui.view

import HomeTopBar
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bloomapp.ui.components.DrawerItem
import com.example.bloomapp.ui.components.FilterDropdown
import com.example.bloomapp.ui.components.PlantHomeCard
import com.example.bloomapp.ui.viewmodel.FilterType
import com.example.bloomapp.ui.viewmodel.PlantType
import com.example.bloomapp.ui.viewmodel.PlantsViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PlantsViewModel,
    onAddClick: () -> Unit,
    onLogout: () -> Unit,
    navController: NavController? = null
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val plants by viewModel.plants.collectAsState()
    val searchQuery = viewModel.searchQuery
    val timeFilter = viewModel.timeFilter
    val plantTypeFilter = viewModel.plantTypeFilter

    val colors = MaterialTheme.colorScheme

    // Calcul des plantes filtrées
    val displayedPlants = remember(plants, timeFilter, plantTypeFilter, searchQuery) {
        var filtered = plants
        filtered = when (timeFilter) {
            FilterType.ALL -> filtered
            FilterType.TODAY -> filtered.filter {
                it.date?.toDate()?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate() == java.time.LocalDate.now()
            }
            FilterType.WEEK -> filtered.filter {
                val d = it.date?.toDate()?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()
                d?.isAfter(java.time.LocalDate.now().minusDays(7)) == true
            }
            FilterType.MONTH -> filtered.filter {
                val d = it.date?.toDate()?.toInstant()?.atZone(java.time.ZoneId.systemDefault())?.toLocalDate()
                val now = java.time.LocalDate.now()
                d?.month == now.month && d?.year == now.year
            }
        }

        if (plantTypeFilter != PlantType.ALL) {
            filtered = filtered.filter { plant ->
                val type = plant.plantType ?: "AUTRE"
                type.equals(plantTypeFilter.name, ignoreCase = true)
            }
        }

        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { plant ->
                plant.name?.contains(searchQuery, ignoreCase = true) == true ||
                        plant.scientificName?.contains(searchQuery, ignoreCase = true) == true ||
                        plant.family?.contains(searchQuery, ignoreCase = true) == true
            }
        }

        filtered
    }

    val timeFilterOptions = listOf(
        "ALL" to "Toutes les périodes",
        "TODAY" to "Aujourd'hui",
        "WEEK" to "Cette semaine",
        "MONTH" to "Ce mois"
    )

    val plantTypeOptions = listOf(
        "ALL" to "Tous les types",
        "SUCCULENTE" to "Plantes grasses",
        "TROPICALE" to "Tropicales",
        "CACTUS" to "Cactus",
        "FOUGERE" to "Fougères",
        "PALMIER" to "Palmiers",
        "AROMATE" to "Aromatiques",
        "FLEUR" to "Fleurs",
        "AUTRE" to "Autres"
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 45.dp, bottom = 25.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🌿 BLOOM",
                        style = MaterialTheme.typography.headlineMedium,
                        color = colors.primary
                    )
                    Divider(color = colors.onSurface.copy(alpha = 0.3f))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DrawerItem("Mes Plantes",
                        color =MaterialTheme.colorScheme.onSurface
                    ) { scope.launch { drawerState.close() } }
                    DrawerItem("Ajouter une Plante",
                        color =MaterialTheme.colorScheme.onSurface
                        ) {
                        onAddClick()
                        scope.launch { drawerState.close() }
                    }

                    Divider(color = colors.onSurface.copy(alpha = 0.3f))

                    Text(
                        text = "Filtres",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = colors.onSurface
                    )

                    FilterDropdown(
                        label = "Période",
                        selectedLabel = timeFilterOptions.find { it.first == timeFilter.name }?.second ?: "Toutes",
                        options = timeFilterOptions,
                        onOptionSelected = { value -> viewModel.updateTimeFilter(FilterType.valueOf(value)) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    FilterDropdown(
                        label = "Type de plante",
                        selectedLabel = plantTypeOptions.find { it.first == plantTypeFilter.name }?.second ?: "Tous",
                        options = plantTypeOptions,
                        onOptionSelected = { value -> viewModel.updatePlantTypeFilter(PlantType.valueOf(value)) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    if (timeFilter != FilterType.ALL || plantTypeFilter != PlantType.ALL) {
                        TextButton(
                            onClick = { viewModel.clearFilters() },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text("Réinitialiser les filtres", color = colors.primary)
                        }
                    }

                    Divider(color = colors.onSurface.copy(alpha = 0.3f))

                    DrawerItem("Paramètres",
                        color =MaterialTheme.colorScheme.onSurface
                        ) {
                        navController?.navigate("settings")
                        scope.launch { drawerState.close() }
                    }

                    Spacer(Modifier.weight(1f))
                    Divider(color = colors.onSurface.copy(alpha = 0.3f))

                    DrawerItem("Déconnexion", color = colors.error) { onLogout() }
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
                    },
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = colors.primary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Ajouter", tint = colors.onPrimary)
                }
            },
            containerColor = colors.background
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {

                if (timeFilter != FilterType.ALL || plantTypeFilter != PlantType.ALL || searchQuery.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (timeFilter != FilterType.ALL) {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.updateTimeFilter(FilterType.ALL) },
                                label = { Text(timeFilterOptions.find { it.first == timeFilter.name }?.second ?: "", color = colors.onPrimary) },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Supprimer", modifier = Modifier.size(16.dp), tint = colors.onPrimary) }
                            )
                        }

                        if (plantTypeFilter != PlantType.ALL) {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.updatePlantTypeFilter(PlantType.ALL) },
                                label = { Text(plantTypeOptions.find { it.first == plantTypeFilter.name }?.second ?: "", color = colors.onPrimary) },
                                trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Supprimer", modifier = Modifier.size(16.dp), tint = colors.onPrimary) }
                            )
                        }

                        if (searchQuery.isNotBlank()) {
                            Text("${displayedPlants.size} résultat(s)", style = MaterialTheme.typography.bodySmall, color = colors.onSurface)
                        }
                    }
                }

                if (displayedPlants.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🔍", style = MaterialTheme.typography.displayLarge, color = colors.onBackground)
                            Text("Aucune plante trouvée", style = MaterialTheme.typography.titleMedium, color = colors.onSurface.copy(alpha = 0.7f))
                            if (timeFilter != FilterType.ALL || plantTypeFilter != PlantType.ALL) {
                                TextButton(onClick = { viewModel.clearFilters() }) {
                                    Text("Réinitialiser les filtres", color = colors.primary)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(displayedPlants, key = { it.id }) { plant ->
                            PlantHomeCard(plant = plant, onClick = { navController?.navigate("plantDetail/${plant.id}") })
                        }
                    }
                }
            }
        }
    }
}
