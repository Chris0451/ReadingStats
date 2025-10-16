package com.project.readingstats.features.profile.ui.components

import com.google.firebase.Timestamp
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Rappresenta un utente amico nel sistema
 */
@Parcelize
data class Friend(
    val uid: String = "",           // UID univoco dell'utente
    val name: String = "",          // Nome dell'utente
    val surname: String = "",       // Cognome dell'utente
    val username: String = "",      // Username dell'utente
    val email: String = ""          // Email dell'utente
): Parcelable {
    /**
     * Controlla se l'amico corrisponde al termine di ricerca
     */
    fun matches(searchQuery: String): Boolean {
        return searchQuery.isBlank() ||
                name.contains(searchQuery, ignoreCase = true) ||
                surname.contains(searchQuery, ignoreCase = true) ||
                username.contains(searchQuery, ignoreCase = true)
    }

    /**
     * Restituisce il nome completo dell'amico
     */
    val fullName: String
        get() = "$name $surname".trim()
}

/**
 * Rappresenta una richiesta di amicizia nel sistema
 */
data class FriendRequest(
    val id: String = "",                    // ID del documento della richiesta
    val fromUid: String = "",               // UID del mittente
    val toUid: String = "",                 // UID del destinatario
    val status: String = "pending",         // Stato: "pending", "accepted", "rejected"
    val timestamp: Timestamp? = null,       // Data e ora di creazione
    val message: String = ""                // Messaggio opzionale
)

/**
 * Definisce lo stato della relazione tra due utenti
 */
enum class UserRelationshipStatus {
    NOT_FRIEND,     // Non sono amici
    PENDING,        // Richiesta di amicizia inviata (in attesa)
    IS_FRIEND       // Sono già amici
}
