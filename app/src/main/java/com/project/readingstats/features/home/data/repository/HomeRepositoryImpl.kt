package com.project.readingstats.features.home.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.readingstats.features.home.domain.repository.HomeRepository
import javax.inject.Inject

class HomeRepositoryImpl  @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
): HomeRepository {
    override suspend fun startBookTimer(userBookId: String) {
        TODO("Not yet implemented")
    }
    override suspend fun setBookTimer(userBookId: String, duration: Long) {
        TODO("Not yet implemented")
    }
}