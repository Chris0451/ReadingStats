package com.project.readingstats.features.profile.ui.components

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Manager centralizzato per gestire tutte le operazioni relative agli amici e richieste di amicizia
 */
object FriendsManager {

    // Inizializzazione sicura di Firebase
    private fun getFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private fun getAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    // ==================== GESTIONE AMICI ====================

    /**
     * Carica la lista degli amici dell'utente corrente
     */
    fun loadFriends(onResult: (List<Friend>, String?) -> Unit) {
        try {
            val auth = getAuth()
            val db = getFirestore()
            val currentUser = auth?.currentUser

            if (currentUser == null) {
                onResult(emptyList(), "Utente non autenticato o Firebase non disponibile")
                return
            }

            if (db == null) {
                onResult(emptyList(), "Firebase Firestore non disponibile")
                return
            }

            db.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val friendUids = document.get("friends") as? List<String> ?: emptyList()
                    if (friendUids.isEmpty()) {
                        onResult(emptyList(), null)
                        return@addOnSuccessListener
                    }

                    val friends = mutableListOf<Friend>()
                    var completedRequests = 0

                    friendUids.forEach { uid ->
                        loadUserById(uid) { friend ->
                            friend?.let { friends.add(it) }
                            completedRequests++
                            if (completedRequests == friendUids.size) {
                                onResult(friends, null)
                            }
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    onResult(emptyList(), "Errore nel caricamento amici: ${exception.message}")
                }
        } catch (e: Exception) {
            onResult(emptyList(), "Errore generale: ${e.message}")
        }
    }

    /**
     * Carica tutti gli utenti del sistema (escluso l'utente corrente)
     */
    fun loadAllUsers(onResult: (List<Friend>, String?) -> Unit) {
        try {
            val auth = getAuth()
            val db = getFirestore()
            val currentUser = auth?.currentUser

            if (currentUser == null) {
                onResult(emptyList(), "Utente non autenticato o Firebase non disponibile")
                return
            }

            if (db == null) {
                onResult(emptyList(), "Firebase Firestore non disponibile")
                return
            }

            db.collection("users")
                .get()
                .addOnSuccessListener { snapshot ->
                    val users = snapshot.documents.mapNotNull { doc ->
                        if (doc.id != currentUser.uid) {
                            mapDocumentToFriend(doc)
                        } else null
                    }
                    onResult(users, null)
                }
                .addOnFailureListener { exception ->
                    onResult(emptyList(), "Errore nel caricamento utenti: ${exception.message}")
                }
        } catch (e: Exception) {
            onResult(emptyList(), "Errore generale: ${e.message}")
        }
    }

    /**
     * Carica un singolo utente tramite UID
     */
    fun loadUserById(uid: String, onResult: (Friend?) -> Unit) {
        try {
            val db = getFirestore()
            if (db == null) {
                onResult(null)
                return
            }

            db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        onResult(mapDocumentToFriend(document))
                    } else {
                        onResult(null)
                    }
                }
                .addOnFailureListener {
                    onResult(null)
                }
        } catch (e: Exception) {
            onResult(null)
        }
    }

    // ==================== GESTIONE RICHIESTE AMICIZIA ====================

    /**
     * Invia una richiesta di amicizia
     */
    fun sendFriendRequest(toUid: String, onResult: (Boolean, String?) -> Unit) {
        try {
            val auth = getAuth()
            val db = getFirestore()
            val currentUser = auth?.currentUser

            if (currentUser == null) {
                onResult(false, "Utente non autenticato o Firebase non disponibile")
                return
            }

            if (db == null) {
                onResult(false, "Firebase Firestore non disponibile")
                return
            }

            if (toUid.isBlank() || toUid.length <= 10 || toUid.contains("/")) {
                onResult(false, "UID destinatario non valido")
                return
            }

            val requestData = hashMapOf(
                "fromUid" to currentUser.uid,
                "toUid" to toUid,
                "status" to "pending",
                "timestamp" to Timestamp.now(),
                "message" to ""
            )

            db.collection("friend_requests")
                .add(requestData)
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { exception ->
                    onResult(false, "Errore invio richiesta: ${exception.message}")
                }
        } catch (e: Exception) {
            onResult(false, "Errore generale: ${e.message}")
        }
    }

    /**
     * Carica le richieste di amicizia ricevute
     */
    fun loadReceivedRequests(onResult: (List<FriendRequest>, String?) -> Unit) {
        try {
            val auth = getAuth()
            val db = getFirestore()
            val currentUser = auth?.currentUser

            if (currentUser == null) {
                onResult(emptyList(), "Utente non autenticato o Firebase non disponibile")
                return
            }

            if (db == null) {
                onResult(emptyList(), "Firebase Firestore non disponibile")
                return
            }

            db.collection("friend_requests")
                .whereEqualTo("toUid", currentUser.uid)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener { snapshot ->
                    val requests = snapshot.documents.mapNotNull { doc ->
                        mapDocumentToFriendRequest(doc)
                    }
                    onResult(requests, null)
                }
                .addOnFailureListener { exception ->
                    onResult(emptyList(), "Errore caricamento richieste: ${exception.message}")
                }
        } catch (e: Exception) {
            onResult(emptyList(), "Errore generale: ${e.message}")
        }
    }

    /**
     * Carica le richieste di amicizia inviate
     */
    fun loadSentRequests(onResult: (List<String>, String?) -> Unit) {
        try {
            val auth = getAuth()
            val db = getFirestore()
            val currentUser = auth?.currentUser

            if (currentUser == null) {
                onResult(emptyList(), "Utente non autenticato o Firebase non disponibile")
                return
            }

            if (db == null) {
                onResult(emptyList(), "Firebase Firestore non disponibile")
                return
            }

            db.collection("friend_requests")
                .whereEqualTo("fromUid", currentUser.uid)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener { snapshot ->
                    val sentRequests = snapshot.documents.mapNotNull { it.getString("toUid") }
                    onResult(sentRequests, null)
                }
                .addOnFailureListener { exception ->
                    onResult(emptyList(), "Errore caricamento richieste inviate: ${exception.message}")
                }
        } catch (e: Exception) {
            onResult(emptyList(), "Errore generale: ${e.message}")
        }
    }

    /**
     * Accetta una richiesta di amicizia
     */
    fun acceptFriendRequest(request: FriendRequest, onResult: (Boolean, String?) -> Unit) {
        try {
            val db = getFirestore()
            if (db == null) {
                onResult(false, "Firebase Firestore non disponibile")
                return
            }

            db.collection("friend_requests")
                .document(request.id)
                .update("status", "accepted")
                .addOnSuccessListener {
                    addFriendsReciprocally(request.fromUid, request.toUid, onResult)
                }
                .addOnFailureListener { exception ->
                    onResult(false, "Errore accettazione richiesta: ${exception.message}")
                }
        } catch (e: Exception) {
            onResult(false, "Errore generale: ${e.message}")
        }
    }

    /**
     * Rifiuta una richiesta di amicizia
     */
    fun rejectFriendRequest(request: FriendRequest, onResult: (Boolean, String?) -> Unit) {
        try {
            val db = getFirestore()
            if (db == null) {
                onResult(false, "Firebase Firestore non disponibile")
                return
            }

            db.collection("friend_requests")
                .document(request.id)
                .update("status", "rejected")
                .addOnSuccessListener { onResult(true, null) }
                .addOnFailureListener { exception ->
                    onResult(false, "Errore rifiuto richiesta: ${exception.message}")
                }
        } catch (e: Exception) {
            onResult(false, "Errore generale: ${e.message}")
        }
    }

    // ==================== UTILITY E HELPER ====================

    /**
     * Determina lo stato della relazione con un utente
     */
    fun getRelationshipStatus(
        user: Friend,
        friendsList: List<Friend>,
        sentRequestsList: List<String>
    ): UserRelationshipStatus {
        return when {
            friendsList.any { it.uid == user.uid } -> UserRelationshipStatus.IS_FRIEND
            sentRequestsList.contains(user.uid) -> UserRelationshipStatus.PENDING
            else -> UserRelationshipStatus.NOT_FRIEND
        }
    }

    /**
     * Aggiunge reciprocamente due utenti come amici
     */
    private fun addFriendsReciprocally(uid1: String, uid2: String, onResult: (Boolean, String?) -> Unit) {
        try {
            val db = getFirestore()
            if (db == null) {
                onResult(false, "Firebase Firestore non disponibile")
                return
            }

            db.collection("users").document(uid1).get()
                .addOnSuccessListener { doc1 ->
                    val friends1 = doc1.get("friends") as? MutableList<String> ?: mutableListOf()
                    if (!friends1.contains(uid2)) {
                        friends1.add(uid2)
                        doc1.reference.update("friends", friends1)
                            .addOnSuccessListener {
                                // Aggiungi reciprocamente
                                db.collection("users").document(uid2).get()
                                    .addOnSuccessListener { doc2 ->
                                        val friends2 = doc2.get("friends") as? MutableList<String> ?: mutableListOf()
                                        if (!friends2.contains(uid1)) {
                                            friends2.add(uid1)
                                            doc2.reference.update("friends", friends2)
                                                .addOnSuccessListener { onResult(true, null) }
                                                .addOnFailureListener { ex -> onResult(false, ex.message) }
                                        } else {
                                            onResult(true, null)
                                        }
                                    }
                                    .addOnFailureListener { ex -> onResult(false, ex.message) }
                            }
                            .addOnFailureListener { ex -> onResult(false, ex.message) }
                    } else {
                        onResult(true, null)
                    }
                }
                .addOnFailureListener { ex -> onResult(false, ex.message) }
        } catch (e: Exception) {
            onResult(false, "Errore generale: ${e.message}")
        }
    }

    /**
     * Mappa un documento Firestore a un oggetto Friend
     */
    private fun mapDocumentToFriend(document: com.google.firebase.firestore.DocumentSnapshot): Friend? {
        return try {
            Friend(
                uid = document.getString("uid") ?: "",
                name = document.getString("name") ?: "",
                surname = document.getString("surname") ?: "",
                username = document.getString("username") ?: "",
                email = document.getString("email") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Mappa un documento Firestore a un oggetto FriendRequest
     */
    private fun mapDocumentToFriendRequest(document: com.google.firebase.firestore.DocumentSnapshot): FriendRequest? {
        return try {
            FriendRequest(
                id = document.id,
                fromUid = document.getString("fromUid") ?: "",
                toUid = document.getString("toUid") ?: "",
                status = document.getString("status") ?: "pending",
                timestamp = document.getTimestamp("timestamp"),
                message = document.getString("message") ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }
}
