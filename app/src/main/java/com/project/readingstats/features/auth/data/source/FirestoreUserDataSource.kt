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

}