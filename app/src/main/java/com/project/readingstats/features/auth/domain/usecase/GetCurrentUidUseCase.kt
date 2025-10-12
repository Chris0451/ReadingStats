package com.project.readingstats.features.auth.domain.usecase

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject

class GetCurrentUidUseCase @Inject constructor(
    private val auth: FirebaseAuth
) {
    operator fun invoke(): String? = auth.currentUser?.uid
}