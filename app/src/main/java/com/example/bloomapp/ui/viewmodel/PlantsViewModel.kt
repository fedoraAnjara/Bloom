package com.example.bloomapp.ui.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloomapp.data.model.Plant
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.util.Base64
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.ZoneId
import org.json.JSONObject
import org.json.JSONArray
import java.net.URL
import java.net.HttpURLConnection

enum class FilterType {
    TODAY, WEEK, MONTH, ALL
}

enum class PlantType {
    ALL,
    SUCCULENTE,      // Plantes grasses
    TROPICALE,       // Monstera, Philodendron...
    CACTUS,
    FOUGERE,
    PALMIER,
    AROMATE,         // Basilic, Menthe...
    FLEUR,
    AUTRE
}

class PlantsViewModel : ViewModel() {

    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants: StateFlow<List<Plant>> = _plants.asStateFlow()

    var filter by mutableStateOf(FilterType.ALL)

    private var plantsListener: ListenerRegistration? = null
    private val USE_MOCK_DATA = false

    private val OPENAI_API_KEY = "sk-....ajouter une clée api"
    var timeFilter by mutableStateOf(FilterType.ALL)
    var plantTypeFilter by mutableStateOf(PlantType.ALL)
    var searchQuery by mutableStateOf("")
        private set

    fun updateSearchQuery(query: String) {
        searchQuery = query
        Log.d("PlantsVM", "Recherche: '$query'")
    }

    fun updateTimeFilter(filter: FilterType) {
        timeFilter = filter
        Log.d("PlantsVM", "Filtre temporel: $filter")
    }

    fun updatePlantTypeFilter(type: PlantType) {
        plantTypeFilter = type
        Log.d("PlantsVM", "Filtre type: $type")
    }

    fun clearFilters() {
        timeFilter = FilterType.ALL
        plantTypeFilter = PlantType.ALL
        searchQuery = ""
        Log.d("PlantsVM", "Filtres réinitialisés")
    }
    init {
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val user = auth.currentUser
            Log.d("PlantsVM", "AuthStateListener triggered. user=${user?.uid}")
            if (user != null) {
                loadPlants()
            } else {
                Log.d("PlantsVM", "No user -> skip loadPlants")
                _plants.value = emptyList()
            }
        }
    }


    fun loadPlants() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Log.w("PlantsVM", "loadPlants appelé SANS utilisateur connecté")
            _plants.value = emptyList()
            return
        }

        Log.d("PlantsVM", "loadPlants appelé avec uid=$uid")

        //Supprimer l'ancien listener pour éviter les doublons
        plantsListener?.remove()

        //Créer un nouveau listener
        plantsListener = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("plantes")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    Log.e("PlantsVM", "Erreur Firestore: ${error.message}")
                    return@addSnapshotListener
                }

                val docsCount = snap?.documents?.size ?: 0
                Log.d("PlantsVM", "Snapshot reçu: $docsCount document(s)")

                snap?.let { snapshot ->
                    try {
                        val plantsList = snapshot.toObjects(Plant::class.java)
                        Log.d("PlantsVM", "${plantsList.size} plante(s) parsée(s)")

                        // Log détaillé pour debug
                        plantsList.forEachIndexed { index, plant ->
                            Log.d("PlantsVM", "  [$index] ${plant.name} (id=${plant.id})")
                        }

                        _plants.value = plantsList
                    } catch (e: Exception) {
                        Log.e("PlantsVM", "Erreur parsing: ${e.message}", e)
                    }
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("PlantsVM", "ViewModel détruit, nettoyage du listener")
        plantsListener?.remove()
    }

    // ANALYSE AVEC GPT-4
    fun analyzePlantAndSave(bitmap: Bitmap, onDone: (Boolean, String?) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val plantId = db.collection("plants").document().id

        viewModelScope.launch {
            try {
                // Convertir Bitmap → JPEG + Base64
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                val bytes = baos.toByteArray()
                val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

                val jsonText = if (USE_MOCK_DATA) {
                    //MODE MOCK : Pas d'appel API, données de test
                    kotlinx.coroutines.delay(2000) // Simule le temps d'analyse
                    """
                    {
                        "isPlant": true,
                        "name": "Monstera Deliciosa",
                        "scientificName": "Monstera deliciosa",
                        "family": "Araceae",
                        "description": "Une plante tropicale populaire avec de grandes feuilles perforées",
                        "fullDetails": "Le Monstera Deliciosa, aussi appelé faux philodendron, est une plante grimpante originaire des forêts tropicales d'Amérique centrale. Ses feuilles spectaculaires peuvent atteindre 90 cm de large et développent des perforations caractéristiques avec l'âge.",
                        "careInstructions": "Arrosez régulièrement sans détremper le sol. Utilisez un terreau bien drainant. Nettoyez les feuilles avec un chiffon humide pour favoriser la photosynthèse. Taillez les feuilles mortes ou abîmées.",
                        "wateringFrequency": "1 fois par semaine en été, tous les 10-15 jours en hiver",
                        "sunlightNeeds": "Lumière indirecte vive, éviter le soleil direct",
                        "plantType": "Type parmi: SUCCULENTE, TROPICALE, CACTUS, FOUGERE, PALMIER, AROMATE, FLEUR, AUTRE"
                    }
                    """.trimIndent()
                } else {
                    // MODE API : Appeler GPT-4 Vision
                    callOpenAIAPI(base64)
                }

                // Parser le JSON
                val cleanJson = jsonText.replace("```json", "").replace("```", "").trim()
                val json = JSONObject(cleanJson)

                // Extraire les données
                val isPlant = json.optBoolean("isPlant", true)
                val name = json.optString("name", "Plante inconnue")
                val description = json.optString("description", "")

                // Créer l'objet Plant
                val plant = Plant(
                    id = plantId,
                    userId = uid,
                    name = name,
                    description = description,
                    imageUrl = base64,
                    timestamp = System.currentTimeMillis(),
                    date = Timestamp.now(),
                    isPlant = isPlant,
                    scientificName = json.optString("scientificName", ""),
                    family = json.optString("family", ""),
                    fullDetails = json.optString("fullDetails", description),
                    careInstructions = json.optString("careInstructions", ""),
                    wateringFrequency = json.optString("wateringFrequency", ""),
                    sunlightNeeds = json.optString("sunlightNeeds", ""),
                    plantType = json.optString("plantType", "AUTRE")
                )

                // Stocker dans Firestore
                db.collection("users")
                    .document(uid)
                    .collection("plantes")
                    .document(plantId)
                    .set(plant)
                    .addOnSuccessListener { onDone(true, plantId) }
                    .addOnFailureListener { onDone(false, it.message) }

            } catch (e: Exception) {
                e.printStackTrace()
                onDone(false, e.message)
            }
        }
    }


    fun migrateOldPlants() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(uid)
            .collection("plantes")
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { doc ->
                    // Si le document a un champ "plant" au lieu de "isPlant"
                    if (doc.contains("plant")) {
                        val updates = hashMapOf<String, Any>(
                            "isPlant" to (doc.getBoolean("plant") ?: true)
                        )
                        doc.reference.update(updates)
                        Log.d("Migration", "Document ${doc.id} migré")
                    }
                }
            }
    }

    fun migratePlantTypes() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        Log.d("PlantsVM", "Migration des types de plantes...")

        db.collection("users")
            .document(uid)
            .collection("plantes")
            .get()
            .addOnSuccessListener { snapshot ->
                var migratedCount = 0

                snapshot.documents.forEach { doc ->
                    // Si la plante n'a pas de type
                    if (!doc.contains("plantType") || doc.getString("plantType").isNullOrBlank()) {

                        // Essayer de deviner le type selon le nom/famille
                        val name = doc.getString("name")?.lowercase() ?: ""
                        val family = doc.getString("family")?.lowercase() ?: ""
                        val scientificName = doc.getString("scientificName")?.lowercase() ?: ""

                        val guessedType = when {
                            // Plantes grasses
                            name.contains("succulente") ||
                                    family.contains("crassulaceae") ||
                                    name.contains("echeveria") ||
                                    name.contains("sedum") -> "SUCCULENTE"

                            // Cactus
                            name.contains("cactus") ||
                                    family.contains("cactaceae") -> "CACTUS"

                            // Tropicales
                            name.contains("monstera") ||
                                    name.contains("philodendron") ||
                                    name.contains("pothos") ||
                                    family.contains("araceae") -> "TROPICALE"

                            // Fougères
                            name.contains("fougère") ||
                                    name.contains("fern") ||
                                    family.contains("polypodiaceae") -> "FOUGERE"

                            // Palmiers
                            name.contains("palmier") ||
                                    name.contains("palm") ||
                                    family.contains("arecaceae") -> "PALMIER"

                            // Aromatiques
                            name.contains("basilic") ||
                                    name.contains("menthe") ||
                                    name.contains("thym") ||
                                    name.contains("romarin") ||
                                    family.contains("lamiaceae") -> "AROMATE"

                            // Fleurs
                            name.contains("rose") ||
                                    name.contains("orchidée") ||
                                    name.contains("tulipe") ||
                                    name.contains("fleur") -> "FLEUR"

                            // Autre par défaut
                            else -> "AUTRE"
                        }

                        doc.reference.update("plantType", guessedType)
                            .addOnSuccessListener {
                                migratedCount++
                                Log.d("PlantsVM", "${doc.id}: type = $guessedType")
                            }
                            .addOnFailureListener { e ->
                                Log.e("PlantsVM", "Erreur migration ${doc.id}: ${e.message}")
                            }
                    }
                }

                Log.d("PlantsVM", "Migration terminée: $migratedCount plantes migrées")
            }
            .addOnFailureListener { e ->
                Log.e("PlantsVM", "Erreur migration: ${e.message}")
            }
    }

    private suspend fun callOpenAIAPI(imageBase64: String, retryCount: Int = 0): String {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = URL("https://api.openai.com/v1/chat/completions")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $OPENAI_API_KEY")
                connection.doOutput = true
                connection.connectTimeout = 60000
                connection.readTimeout = 60000

                val prompt = """
                    Analyse cette image en détail.
                    
                    Si c'est une plante, retourne EXACTEMENT ce JSON (sans backticks ni formatage markdown) :
                    {
                        "isPlant": true,
                        "name": "Nom commun de la plante",
                        "scientificName": "Nom scientifique latin",
                        "family": "Famille botanique",
                        "description": "Description courte en 2-3 phrases",
                        "fullDetails": "Description détaillée complète de la plante, son origine, ses caractéristiques",
                        "careInstructions": "Instructions complètes d'entretien",
                        "wateringFrequency": "Fréquence d'arrosage recommandée",
                        "sunlightNeeds": "Besoins en lumière (ex: Lumière indirecte, Plein soleil, Ombre partielle)"
                    }
                    
                    Si ce n'est PAS une plante, retourne EXACTEMENT ce JSON :
                    {
                        "isPlant": false,
                        "name": "Aucune plante détectée",
                        "description": "Cette image ne contient pas de plante. Il s'agit de [décrire ce qui est visible]."
                    }
                    
                    Important : Réponds UNIQUEMENT avec le JSON, sans texte avant ou après.
                """.trimIndent()

                // Construire le payload JSON pour GPT-4 Vision
                val requestBody = JSONObject().apply {
                    put("model", "gpt-4o-mini")  // Modèle économique avec vision
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("type", "text")
                                    put("text", prompt)
                                })
                                put(JSONObject().apply {
                                    put("type", "image_url")
                                    put("image_url", JSONObject().apply {
                                        put("url", "data:image/jpeg;base64,$imageBase64")
                                    })
                                })
                            })
                        })
                    })
                    put("max_tokens", 1000)
                }

                // Envoyer la requête
                connection.outputStream.use { os ->
                    os.write(requestBody.toString().toByteArray())
                }

                // Lire la réponse
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    jsonResponse
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                } else {
                    val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    when (responseCode) {
                        429 -> {
                            if (retryCount < 2) {
                                kotlinx.coroutines.delay(3000)
                                return@withContext callOpenAIAPI(imageBase64, retryCount + 1)
                            }
                            throw Exception("Quota OpenAI dépassé. Vérifiez votre compte sur https://platform.openai.com")
                        }
                        401 -> throw Exception("Clé API OpenAI invalide. Vérifiez sur https://platform.openai.com/api-keys")
                        else -> throw Exception("Erreur OpenAI $responseCode: $error")
                    }
                }
            } catch (e: Exception) {
                if (e.message?.contains("Quota") == true || e.message?.contains("invalide") == true) {
                    throw e
                } else if (retryCount < 2) {
                    kotlinx.coroutines.delay(2000)
                    return@withContext callOpenAIAPI(imageBase64, retryCount + 1)
                } else {
                    throw e
                }
            }
        }
    }

    // Récupérer les détails d'une plante spécifique
    fun getPlantById(plantId: String): Plant? {
        return plants.value.find { it.id == plantId }
    }

    fun deletePlant(plantId: String, onDone: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            onDone(false)
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("plantes")
            .document(plantId)
            .delete()
            .addOnSuccessListener {
                Log.d("PlantsViewModel", "Plante supprimée: $plantId")
                onDone(true)
            }
            .addOnFailureListener { e ->
                Log.e("PlantsViewModel", "Erreur suppression: ${e.message}")
                onDone(false)
            }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun filteredPlants(): List<Plant> {
        val now = LocalDate.now()
        return when (filter) {
            FilterType.TODAY -> plants.value.filter {
                it.date?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() == now
            }
            FilterType.WEEK -> plants.value.filter {
                val d = it.date?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                d?.isAfter(now.minusDays(7)) == true
            }
            FilterType.MONTH -> plants.value.filter {
                val d = it.date?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                d?.month == now.month && d?.year == now.year
            }
            FilterType.ALL -> plants.value
        }
    }
}