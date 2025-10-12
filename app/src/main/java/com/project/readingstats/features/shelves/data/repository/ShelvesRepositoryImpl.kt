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
        val existing = snapshot.data ?: emptyMap<String, Any?>()
        fun has(key: String) = existing.containsKey(key)

        val now = System.currentTimeMillis()

        // base sempre scritto
        val patch = buildMap<String, Any?> {
            put("status", status.name)
            put("updatedAt", now)
            put("volumeId", userBookId)

            // candidati da payload: aggiunti SOLO se mancanti nel doc e il valore è non-null/non-vuoto
            val candidates: Map<String, Any?> = mapOf(
                "title"      to payload?.title?.ifBlank { null },
                "thumbnail"  to payload?.thumbnail,
                "authors"    to (payload?.authors ?: emptyList<String>()),
                "categories" to (payload?.categories ?: emptyList<String>()),
                "isbn13"     to payload?.isbn13,
                "isbn10"     to payload?.isbn10
            )

            candidates
                .filter { (k, v) -> !has(k) && v != null }
                .forEach { (k, v) -> put(k, v) }

            // regola speciale per pageCount: integra solo se assente/≤0 e il payload porta un valore > 0
            val existingPageCount = (existing["pageCount"] as? Number)?.toInt() ?: 0
            val incomingPageCount = payload?.pageCount ?: 0
            if (incomingPageCount > 0 && existingPageCount <= 0) {
                put("pageCount", incomingPageCount)
            }
        }
        docRef.set(patch, SetOptions.merge()).await()
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

    override suspend fun upsertStatusBook(
        userBook: UserBook,
        payload: UserBook?,
        status: ReadingStatus,
        pageCount: Int?,
        pageInReading: Int?
    ){
        val docRef = collection().document(userBook.volumeId)
        val snapshot = docRef.get().await()
        val existing = snapshot.data ?: emptyMap<String, Any?>()
        fun has(key: String) = existing.containsKey(key)

        val now = System.currentTimeMillis()

        val patch = buildMap<String, Any?> {
            put("volumeId", userBook.volumeId)
            put("status", status.name)
            put("updatedAt", now)

            val candidates: Map<String, Any?> = mapOf(
                "title" to payload?.title?.ifBlank { null },
                "thumbnail" to payload?.thumbnail,
                "description" to payload?.description,
                "authors" to (payload?.authors ?: emptyList<String>()),
                "categories" to (payload?.categories ?: emptyList<String>()),
                "isbn13" to payload?.isbn13,
                "isbn10" to payload?.isbn10
            )
            candidates.filter { (k, v) -> !has(k) && v != null }.forEach { (k, v) -> put(k, v) }

            val existingPageCount = (existing["pageCount"] as? Number)?.toInt() ?: 0
            val incomingPageCount = when {
                (pageCount ?:0) >0 -> pageCount
                (payload?.pageCount ?:0) > 0 && existingPageCount <= 0 -> payload?.pageCount
                else -> null
            }
            if (incomingPageCount != null) put("pageCount", incomingPageCount)

            if (pageInReading!=null) put("pageInReading", pageInReading)
        }
        docRef.set(patch, SetOptions.merge()).await()
    }
    private fun <T> List<T>?.orElseEmpty(): List<T> = this ?: emptyList()
}