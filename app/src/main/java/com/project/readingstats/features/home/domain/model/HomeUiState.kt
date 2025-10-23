package com.project.readingstats.features.home.domain.model

data class HomeUiState(
    val items: List<HomeItemState> = emptyList(),
    val pagesDialog: PagesDialogState? = null,
)