package com.example.bloomapp.ui.viewmodel

import android.graphics.Bitmap
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bloomapp.data.model.Plant
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import kotlinx.coroutines.flow.MutableStateFlow
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.ZoneId

enum class FilterType {
    TODAY, WEEK, MONTH, ALL
}

class PlantsViewModel : ViewModel() {

    // Liste des plantes observable
    private val _plants = MutableStateFlow<List<Plant>>(emptyList())
    val plants = _plants

    var filter by mutableStateOf(FilterType.ALL)

    init {
        loadPlants()
    }

    // Charger toutes les plantes de l'utilisateur
    private fun loadPlants() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(uid)
            .collection("plantes")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val list = it.documents.mapNotNull { doc ->
                        doc.toObject(Plant::class.java)
                    }
                    plants.value = list
                }
            }
    }

    // Ajouter une nouvelle plante
    fun addPlant(name: String, bitmap: Bitmap?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return // arrêter si non connecté

        val db = FirebaseFirestore.getInstance()
        val plantId = db.collection("plants").document().id

        val baos = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        val imageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)

        val plant = Plant(
            id = plantId,
            userId = uid,
            name = name,
            imageUrl = imageBase64,
            timestamp = System.currentTimeMillis(),
            date = Timestamp.now()
        )

        db.collection("users")
            .document(uid)
            .collection("plantes")
            .document(plantId)
            .set(plant)
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos) // réduire taille
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    }

    fun addPlantWithBitmap(name: String, bitmap: Bitmap?) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (bitmap == null) return

        val db = FirebaseFirestore.getInstance()
        val plantId = db.collection("plants").document().id

        val base64 = bitmapToBase64(bitmap)

        val plant = Plant(
            id = plantId,
            userId = uid,
            name = name,
            imageUrl = base64,
            timestamp = System.currentTimeMillis(),
            date = Timestamp.now()
        )

        db.collection("users")
            .document(uid)
            .collection("plantes")
            .document(plantId)
            .set(plant)
    }



    // Charger les plantes d'un utilisateur donné (optionnel)
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadUserPlants(userId: String, onResult: (List<Plant>) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("plants")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.toObjects(Plant::class.java)
                onResult(list)
            }
    }

    // Filtrer les plantes selon la période
    @RequiresApi(Build.VERSION_CODES.O)
    fun filteredPlants(): List<Plant> {
        val now = LocalDate.now()
        return when (filter) {
            FilterType.TODAY -> plants.value.filter {
                it.date?.toDate()?.toInstant()
                    ?.atZone(ZoneId.systemDefault())
                    ?.toLocalDate() == now
            }
            FilterType.WEEK -> plants.value.filter {
                val d = it.date?.toDate()?.toInstant()
                    ?.atZone(ZoneId.systemDefault())
                    ?.toLocalDate()
                d?.isAfter(now.minusDays(7)) == true
            }
            FilterType.MONTH -> plants.value.filter {
                val d = it.date?.toDate()?.toInstant()
                    ?.atZone(ZoneId.systemDefault())
                    ?.toLocalDate()
                d?.month == now.month && d?.year == now.year
            }
            FilterType.ALL -> plants.value
        }
    }
}
