package com.project.readingstats.features.catalog.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.project.readingstats.features.catalog.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
): UserPreferencesRepository {
    override suspend fun getSelectedCategories(uid: String): Set<String> {
        val snapshot = firestore
            .collection("users")
            .document(uid)
            .collection("meta")
            .document("preferences")
            .get()
            .await()

        val list = snapshot.get("selectedCategories") as? List<String> ?: emptyList()
        return list.toSet()
    }

    override suspend fun setSelectedCategories(uid: String, categories: Set<String>) {
        val data = mapOf("selectedCategories" to categories.toList())
        firestore.collection("users")
            .document(uid)
            .collection("meta")
            .document("preferences")
            .set(data).await()
    }
}