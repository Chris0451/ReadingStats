package com.project.readingstats.features.shelves.data.repository

import androidx.compose.animation.core.snap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.project.readingstats.features.shelves.data.mapper.toUserBook
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import com.project.readingstats.features.shelves.domain.repository.ShelvesRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShelvesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
): ShelvesRepository {

    private fun uidOrNull(): String? = auth.currentUser?.uid

    private fun collectionOrNull(): CollectionReference? =
        uidOrNull()?.let { uid ->
            firestore.collection("users").document(uid).collection("books")
        }

    private fun collection() = firestore
        .collection("users")
        .document(requireNotNull(auth.currentUser?.uid) { "Utente non autenticato" })
        .collection("books")

    override fun observeBooks(status: ReadingStatus): Flow<List<UserBook>> = callbackFlow {
        val registration = collection()
            .whereEqualTo("status", status.name)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toUserBook() }.orElseEmpty()
                trySend(list)
            }
        awaitClose { registration.remove() }
    }

    /*
    *
    * Observe the status of a book
    * - if the book does not exist, return null
    * - if the book exists, return the status
    * - if the status is not valid, return null
     */
    override fun observeBookStatus(userBookId: String): Flow<ReadingStatus?> = callbackFlow {
        val registration = collection()
            .document(userBookId)
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    trySend(null)
                    return@addSnapshotListener
                }
                val statusName = snapshot?.getString("status")
                val status = statusName?.let{
                    runCatching { ReadingStatus.valueOf(it) }.getOrNull()
                }
                trySend(status)
            }
        awaitClose { registration.remove() }
    }

    override fun observeUserBook(userBookId: String): Flow<UserBook?> = callbackFlow {
        val registration = collection()
            .document(userBookId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toUserBook())
            }
        awaitClose { registration.remove() }
    }

    /*
    *
    * Setting unique status for each book
    * - if the book already exists, update it
    * - if the book does not exist, create it
    *
     */
    override suspend fun setStatus(userBookId: String, payload: UserBook?, status: ReadingStatus) {
        val docRef = collection().document(userBookId)
        val snapshot = docRef.get().await()
        val now = System.currentTimeMillis()

        val base = mapOf(
            "status" to status.name,
            "updatedAt" to now
        )

        if(snapshot.exists()){
            docRef.update(base).await()
        } else {
            val createdData: MutableMap<String, Any?> = mutableMapOf(
                "volumeId" to userBookId,
                "title" to payload?.title.orEmpty(),
                "thumbnail" to payload?.thumbnail,
                "authors" to (payload?.authors ?: emptyList()),
                "categories" to (payload?.categories ?: emptyList()),
                "pageCount" to (payload?.pageCount), //Da assegnare il valore tramite setPageCount
                "status" to status.name,
                "updatedAt" to now
            )
            docRef.set(createdData).await()
        }
    }

    override suspend fun removeBook(userBookId: String) {
        val collection = collectionOrNull() ?: throw  IllegalStateException("Utente non autenticato")
        collection.document(userBookId).delete().await()
    }

    //Assign count pages to book without a count of pages
    override suspend fun setPageCount(userBookId: String, pageCount: Int) {
        require(pageCount > 0) { "Il numero di pagine deve essere maggiore di 0" }
        val docRef = collection().document(userBookId)
        val now = System.currentTimeMillis()
        val data = mapOf(
            "volumeId" to userBookId,
            "pageCount" to pageCount,
            "updatedAt" to now
        )
        docRef.set(data, SetOptions.merge()).await()
    }

    //Assign in-reading pages for books selected by user
    override suspend fun setPageInReading(userBookId: String, pageInReading: Int) {
        require(pageInReading >= 0) { "Il numero di pagine in lettura non può essere negativo" }
        val docRef = collection().document(userBookId)
        val snapshot = docRef.get().await()
        val total = snapshot.getLong("pageCount")?.toInt() ?: 0

        if (total != 0 && pageInReading > total) throw IllegalArgumentException("Il numero di pagine in lettura non può essere maggiore del totale di pagine ($total)")

        val now = System.currentTimeMillis()
        val data = mapOf(
            "volumeId" to userBookId,
            "pageInReading" to pageInReading,
            "updatedAt" to now
        )
        docRef.set(data, SetOptions.merge()).await()
    }

    private fun <T> List<T>?.orElseEmpty(): List<T> = this ?: emptyList()
}