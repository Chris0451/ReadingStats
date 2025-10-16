package com.project.readingstats.features.home.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.project.readingstats.features.home.domain.model.UiHomeBook
import com.project.readingstats.features.home.domain.repository.HomeRepository
import com.project.readingstats.features.shelves.domain.model.ReadingStatus
import com.project.readingstats.features.shelves.domain.model.UserBook
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepositoryImpl  @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
): HomeRepository {
    private fun userBooks() = firestore
        .collection("users")
        .document(requireNotNull(auth.currentUser?.uid) { "Utente non autenticato" })
        .collection("books")

    override fun observeReadingBooks(): Flow<List<UiHomeBook>> = callbackFlow {
        val ref = userBooks()
        val registration = ref
            .whereEqualTo("status", ReadingStatus.READING.name)
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("HomeRepo", "observeReadingBooks failed", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.map { d ->
                    UiHomeBook(
                        id = d.id,
                        title = d.getString("title").orEmpty(),
                        authors = (d.get("authors") as? List<*>)?.filterIsInstance<String>().orEmpty(),
                        thumbnail = d.getString("thumbnail"),
                        pageCount = (d.getLong("pageCount") ?: 0L).toInt().takeIf { it > 0 },
                        pageInReading = (d.getLong("pageInReading") ?: 0L).toInt().takeIf { it >= 0 },
                        totalReadSeconds = d.getLong("totalReadSeconds") ?: 0L,
                        description = d.getString("description"),
                        isbn10 = d.getString("isbn10"),
                        isbn13 = d.getString("isbn13"),
                    )
                }.orEmpty()
                trySend(list)
            }

        awaitClose { registration.remove() }
    }


    override suspend fun addReadingSession(bookId: String, startMillis: Long, endMillis: Long) {
        require(endMillis >= startMillis)
        val raw = (endMillis - startMillis) / 1000
        val duration = raw.coerceAtLeast(1L)
        val bookRef = userBooks().document(bookId)
        val sessionRef = bookRef.collection("sessions").document()

        try{
            firestore.runBatch { batch ->
                batch.set(
                    sessionRef,
                    mapOf("start" to startMillis, "end" to endMillis, "seconds" to duration)
                )
                batch.set(
                    bookRef,
                    mapOf(
                        "updatedAt" to System.currentTimeMillis(),
                        "totalReadSeconds" to FieldValue.increment(duration)
                    ),
                    SetOptions.merge()
                )
            }.await()
        } catch (e: Exception) {
            Log.e("HomeRepo", "addReadingSession failed", e)
            throw e
        }
    }

    override suspend fun updatePagesRead(bookId: String, pagesRead: Int) {
        val bookRef = userBooks().document(bookId)
        val now = System.currentTimeMillis()
        bookRef.set(
            mapOf("pageInReading" to pagesRead, "updatedAt" to now),
            SetOptions.merge()
        ).await()
    }

    override suspend fun setStatus(bookId: String, status: ReadingStatus, payload: UserBook?) {
        val bookRef = userBooks().document(bookId)
        val now = System.currentTimeMillis()
        val base = mutableMapOf<String, Any?>(
            "status" to status.name,
            "updatedAt" to now,
            "volumeId" to bookId
        )
        payload?.let {
            base.putIfAbsent("title", it.title)
            base.putIfAbsent("thumbnail", it.thumbnail)
            base.putIfAbsent("authors", it.authors)
            base.putIfAbsent("categories", it.categories)
            base.putIfAbsent("isbn13", it.isbn13)
            base.putIfAbsent("isbn10", it.isbn10)
            if ((it.pageCount ?: 0) > 0) base.putIfAbsent("pageCount", it.pageCount)
        }
        bookRef.set(base, SetOptions.merge()).await()
    }
}