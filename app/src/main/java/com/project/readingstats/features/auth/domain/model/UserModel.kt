package com.project.readingstats.features.auth.domain.model

import com.google.firebase.Timestamp

data class UserModel(
    val uid: String,
    val name: String,
    val surname: String,
    val username: String,
    val email: String,
    val friends: List<String> = emptyList(),
    val createdAt: Timestamp
)
