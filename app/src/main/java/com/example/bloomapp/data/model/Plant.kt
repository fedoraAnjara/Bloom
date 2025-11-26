package com.example.bloomapp.data.model
import com.google.firebase.Timestamp

data class Plant(
    var id: String = "",
    var userId: String = "",
    var name: String = "",
    var imageUrl: String = "",
    var timestamp: Long = 0,
    var date: Timestamp? = null
)
