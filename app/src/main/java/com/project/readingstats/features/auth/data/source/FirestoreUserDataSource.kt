package com.project.readingstats.features.auth.data.source

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.project.readingstats.features.auth.data.model.UserModelDto
import kotlinx.coroutines.tasks.await

class FirestoreUserDataSource (
    private val db: FirebaseFirestore
) {
    private val users = db.collection("users")
    private val usernames = db.collection("usernames")

    suspend fun isUsernameTaken(username: String): Boolean {
        val id = username.trim().lowercase()
        return usernames.document(id).get().await().exists()
    }

    suspend fun userExists(uid: String): Boolean {
        return users.document(uid).get().await().exists()
    }

    suspend fun createUserAtomically(
        uid: String,
        profile: Map<String, Any>,
    ) {
        db.runTransaction { transaction ->
            val username = profile["username"] as String
            val usernameRef = usernames.document(username)
            val userRef = users.document(uid)

            if(transaction.get(usernameRef).exists()) {
                throw IllegalStateException("USERNAME_TAKEN")
            }

            transaction.set(usernameRef, mapOf("uid" to uid), SetOptions.merge())
            transaction.set(userRef, profile, SetOptions.merge())
        }.await()
    }

    suspend fun getUserProfile(uid: String): UserModelDto? {
        val snapshot = users.document(uid).get().await()
        if(!snapshot.exists()) return null
        return UserModelDto(
            uid = snapshot.getString("uid") ?: uid,
            name = snapshot.getString("name") ?: "",
            surname = snapshot.getString("surname") ?: "",
            username = snapshot.getString("username") ?: "",
            email = snapshot.getString("email") ?: "",
        )
    }
    // Aggiungi questa funzione al tuo FirestoreUserDataSource.kt esistente
    suspend fun updateUserProfile(
        uid: String,
        username: String,
        name: String,
        surname: String,
        email: String
    ) {
        try {
            // Mappa con i nuovi dati da aggiornare
            val updatedData = mapOf(
                "username" to username.trim(),
                "name" to name.trim(),
                "surname" to surname.trim(),
                "email" to email.trim(),
                "updatedAt" to System.currentTimeMillis() // Timestamp dell'aggiornamento
            )

            // Aggiorna il documento utente su Firestore
            users.document(uid).update(updatedData).await()

            // Se l'username è cambiato, aggiorna anche la collezione usernames
            // (questo richiede una transazione per garantire consistenza)
            db.runTransaction { transaction ->
                val userDoc = users.document(uid)
                val currentData = transaction.get(userDoc).data
                val oldUsername = currentData?.get("username") as? String

                // Se l'username è cambiato
                if (oldUsername != null && oldUsername != username.trim()) {
                    // Rimuovi il vecchio username dalla collezione usernames
                    val oldUsernameRef = usernames.document(oldUsername.lowercase())
                    transaction.delete(oldUsernameRef)

                    // Aggiungi il nuovo username alla collezione usernames
                    val newUsernameRef = usernames.document(username.trim().lowercase())
                    if (transaction.get(newUsernameRef).exists()) {
                        throw IllegalStateException("USERNAME_TAKEN")
                    }
                    transaction.set(newUsernameRef, mapOf("uid" to uid))
                }

                // Aggiorna i dati utente
                transaction.update(userDoc, updatedData)
            }.await()

        } catch (e: Exception) {
            throw e // Rilancia l'eccezione per gestirla nel ViewModel
        }
    }


}