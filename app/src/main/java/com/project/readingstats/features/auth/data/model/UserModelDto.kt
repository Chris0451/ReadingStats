package com.project.readingstats.features.auth.data.model

data class UserModelDto(
    val uid: String = "",
    val name: String = "",
    val surname: String = "",
    val username: String = "",
    val email: String = "",
    val createdAt: Long = 0L
)
