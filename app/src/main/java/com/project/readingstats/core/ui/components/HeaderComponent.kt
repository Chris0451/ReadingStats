package com.project.readingstats.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderComponent(
    modifier: Modifier = Modifier,
    title: String = "Reading Stats",
    icon: ImageVector = Icons.AutoMirrored.Outlined.MenuBook,
    onNavigationIconClick: ( () -> Unit)? = null
){
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(imageVector = icon, contentDescription = "App Logo")
                Text(text = title, fontWeight = FontWeight.SemiBold)
            }
        },
        navigationIcon = {
            if (onNavigationIconClick != null){
                IconButton(onClick = onNavigationIconClick) {
                    Icon(imageVector = icon, contentDescription = null)
                }
            }
        },
        modifier = modifier
    )
}