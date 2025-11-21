package com.example.bloomapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    fun login(email: String, password: String,
              onSuccess: () -> Unit,
              onError: (String) -> Unit) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erreur inconnue") }
    }

    fun signUp(email: String, password: String,
               onSuccess: () -> Unit,
               onError: (String) -> Unit) {

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Erreur inconnue") }
    }
}
