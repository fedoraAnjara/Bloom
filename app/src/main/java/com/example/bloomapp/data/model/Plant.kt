package com.example.bloomapp.data.model

import com.google.firebase.Timestamp

data class Plant(
    val id: String = "",  //PAS de @DocumentId car on stocke déjà l'ID
    val userId: String = "",
    val name: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val timestamp: Long = 0L,
    val date: Timestamp? = null,

    // 🌱 Détails complets pour la page de détails
    val isPlant: Boolean = true,
    val scientificName: String? = null,
    val family: String? = null,
    val fullDetails: String? = null,
    val careInstructions: String? = null,
    val wateringFrequency: String? = null,
    val sunlightNeeds: String? = null,
    val plantType: String? = null
)