package com.project.readingstats.features.home.domain.model

data class HomeItemState(
    val book: UiHomeBook,
    val isRunning: Boolean,
    val sessionStartMillis: Long?,
    val sessionElapsedSec: Long,
    val totalReadSec: Long
) {
    val totalWithSession: Long get() = totalReadSec + sessionElapsedSec
}