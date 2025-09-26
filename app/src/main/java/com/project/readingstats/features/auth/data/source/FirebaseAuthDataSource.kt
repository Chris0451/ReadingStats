package com.project.readingstats.features.auth.data.source

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource(
    private val auth: FirebaseAuth
) {
    suspend fun createUser(email: String, password: String) = auth.createUserWithEmailAndPassword(email, password).await()

    fun currentUid(): String? = auth.currentUser?.uid
}