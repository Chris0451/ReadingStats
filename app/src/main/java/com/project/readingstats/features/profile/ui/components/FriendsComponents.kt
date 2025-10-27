package com.project.readingstats.features.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.project.readingstats.features.profile.data.model.Friend
import com.project.readingstats.features.profile.data.model.FriendRequest
import com.project.readingstats.features.profile.data.model.UserRelationshipStatus
import com.project.readingstats.features.profile.domain.manager.FriendsManager

/**
 * Componente per la barra di ricerca e i tab di selezione
 */
@Composable
fun SearchAndTabs(
    selectedTab: Int,
    searchValue: String,
    onSearchChange: (String) -> Unit,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Barra di ricerca
            OutlinedTextField(
                value = searchValue,
                onValueChange = onSearchChange,
                placeholder = { Text("Search") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Cerca")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tab di selezione
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    label = { Text("Amici") }
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    label = { Text("Richieste") }
                )
                FilterChip(
                    selected = selectedTab == 2,
                    onClick = { onTabSelected(2) },
                    label = { Text("Utenti") }
                )
            }
        }
    }
}

/**
 * Componente per visualizzare un singolo amico nella lista
 */
@Composable
fun FriendItem(
    friend: Friend,
    isInUsersSection: Boolean = false,
    friendsList: List<Friend> = emptyList(),
    sentRequestsList: List<String> = emptyList(),
    onSendRequest: (Friend) -> Unit = {},
    onFriendClick: (Friend) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onFriendClick(friend) },
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE57373)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informazioni amico
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (friend.username.isNotBlank()) {
                    Text(
                        text = "@${friend.username}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Pulsante azione (solo nella sezione utenti)
            if (isInUsersSection) {
                Spacer(modifier = Modifier.width(8.dp))
                val status = FriendsManager.getRelationshipStatus(friend, friendsList, sentRequestsList)

                when (status) {
                    UserRelationshipStatus.NOT_FRIEND -> {
                        IconButton(
                            onClick = { onSendRequest(friend) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Green),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Invia richiesta",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    UserRelationshipStatus.PENDING -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF9800)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = "Richiesta in attesa",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    UserRelationshipStatus.IS_FRIEND -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Blue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "GiÃ  amico",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Componente per visualizzare una richiesta di amicizia
 */
@Composable
fun RequestItem(
    request: FriendRequest,
    onAccept: (FriendRequest) -> Unit,
    onReject: (FriendRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var senderUser by remember { mutableStateOf<Friend?>(null) }

    // Carica le informazioni del mittente
    LaunchedEffect(request.fromUid) {
        FriendsManager.loadUserById(request.fromUid) { user ->
            senderUser = user
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informazioni mittente
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = senderUser?.fullName ?: "Caricamento...",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                if (senderUser?.username?.isNotBlank() == true) {
                    Text(
                        text = "@${senderUser!!.username}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Pulsanti azione
            Row {
                // Pulsante accetta
                IconButton(
                    onClick = { onAccept(request) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Accetta",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Pulsante rifiuta
                IconButton(
                    onClick = { onReject(request) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF44336)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Rifiuta",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}