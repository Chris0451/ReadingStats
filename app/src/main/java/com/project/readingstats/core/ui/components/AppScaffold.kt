package com.project.readingstats.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable

@Composable
fun AppScaffold(
    bottomBar: @Composable () -> Unit,
    topBar: (@Composable () -> Unit)? = { HeaderComponent()},
    content: @Composable (PaddingValues) -> Unit
){
    Scaffold(
        topBar = { topBar?.invoke() },
        bottomBar = bottomBar,
        content = content
    )
}