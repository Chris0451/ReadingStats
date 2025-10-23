package com.project.readingstats.features.auth.data.source

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource(
    private val auth: FirebaseAuth
) {
    suspend fun createUser(email: String, password: String): AuthResult = auth.createUserWithEmailAndPassword(email, password).await()
    suspend fun signIn(email: String, password: String): AuthResult = auth.signInWithEmailAndPassword(email, password).await()

    fun signOut() = auth.signOut()

    fun currentUid(): String? = auth.currentUser?.uid
}