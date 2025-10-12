package com.project.readingstats.features.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaAmici(onBack: () -> Unit) {
    // Stati della UI
    var selectedTab by remember { mutableStateOf(0) }
    var searchValue by remember { mutableStateOf("") }
    var friendsList by remember { mutableStateOf<List<Friend>>(emptyList()) }
    var requestsList by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }
    var allUsersList by remember { mutableStateOf<List<Friend>>(emptyList()) }
    var sentRequestsList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Caricamento dati quando cambia il tab
    LaunchedEffect(selectedTab) {
        isLoading = true
        errorMessage = null

        when (selectedTab) {
            0 -> {
                // Carica amici
                FriendsManager.loadFriends { friends, error ->
                    friendsList = friends
                    errorMessage = error
                    isLoading = false
                }
            }
            1 -> {
                // Carica richieste ricevute
                FriendsManager.loadReceivedRequests { requests, error ->
                    requestsList = requests
                    errorMessage = error
                    isLoading = false
                }
            }
            2 -> {
                // Carica tutti gli utenti
                FriendsManager.loadAllUsers { users, error ->
                    allUsersList = users
                    errorMessage = error
                    isLoading = false
                }
                // Carica anche le richieste inviate
                FriendsManager.loadSentRequests { sent, _ ->
                    sentRequestsList = sent
                }
            }
        }
    }

    // Filtraggio delle liste separate per tipo
    val filteredFriends = when (selectedTab) {
        0 -> friendsList.filter { it.matches(searchValue) }
        2 -> allUsersList.filter { it.matches(searchValue) }
        else -> emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista Amici") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Card contenitore per la barra di ricerca e i filtri
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(top = 14.dp, bottom = 8.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    // Riga contenente la barra di ricerca
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Campo di testo per la ricerca
                        OutlinedTextField(
                            value = searchValue,
                            onValueChange = { searchValue = it },
                            placeholder = { Text("Search") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.LightGray,
                                focusedBorderColor = Color.Gray,
                                cursorColor = Color.Black,
                                focusedPlaceholderColor = Color.DarkGray,
                                unfocusedPlaceholderColor = Color.DarkGray,
                                focusedLeadingIconColor = Color.DarkGray,
                                unfocusedLeadingIconColor = Color.DarkGray,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    // ✅ AGGIORNATO: Riga contenente i chip per filtrare le diverse sezioni
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Chip per la sezione "Amici"
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Amici") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Black,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFE0E0E0),
                                labelColor = Color.Black
                            )
                        )
                        Spacer(Modifier.width(10.dp))

                        // ✅ NUOVO: Chip per la sezione "Richieste"
                        FilterChip(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Richieste") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Black,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFE0E0E0),
                                labelColor = Color.Black
                            )
                        )
                        Spacer(Modifier.width(10.dp))

            Spacer(modifier = Modifier.height(16.dp))

            // Contenuto principale
            when {
                isLoading -> {
                    LoadingIndicator()
                }
                errorMessage != null -> {
                    CenteredMessage(
                        message = "Errore: $errorMessage",
                        color = Color.Red
                    )
                }
                else -> {
                    when (selectedTab) {
                        // Tab Richieste
                        1 -> {
                            if (requestsList.isEmpty()) {
                                CenteredMessage(message = "Nessuna richiesta ricevuta")
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(requestsList) { request ->
                                        RequestItem(
                                            request = request,
                                            onAccept = { acceptedRequest ->
                                                FriendsManager.acceptFriendRequest(acceptedRequest) { success, error ->
                                                    if (success) {
                                                        // Rimuovi dalla lista delle richieste
                                                        requestsList = requestsList.filter { it.id != acceptedRequest.id }
                                                        // Ricarica la lista degli amici
                                                        FriendsManager.loadFriends { friends, _ ->
                                                            friendsList = friends
                                                        }
                                                    } else {
                                                        errorMessage = error
                                                    }
                                                }
                                            },
                                            onReject = { rejectedRequest ->
                                                FriendsManager.rejectFriendRequest(rejectedRequest) { success, error ->
                                                    if (success) {
                                                        requestsList = requestsList.filter { it.id != rejectedRequest.id }
                                                    } else {
                                                        errorMessage = error
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        // Tab Amici e Utenti
                        else -> {
                            if (filteredFriends.isEmpty()) {
                                val message = when {
                                    searchValue.isNotEmpty() -> "Nessun risultato per '$searchValue'"
                                    selectedTab == 0 -> "Nessun amico trovato"
                                    selectedTab == 2 -> "Nessun utente trovato"
                                    else -> "Nessun elemento trovato"
                                }
                                CenteredMessage(message = message)
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(filteredFriends) { friend ->
                                        FriendItem(
                                            friend = friend,
                                            isInUsersSection = selectedTab == 2,
                                            friendsList = friendsList,
                                            sentRequestsList = sentRequestsList,
                                            onSendRequest = { friendToAdd ->
                                                FriendsManager.sendFriendRequest(friendToAdd.uid) { success, error ->
                                                    if (success) {
                                                        // Aggiungi alla lista delle richieste inviate
                                                        sentRequestsList = sentRequestsList + friendToAdd.uid
                                                    } else {
                                                        errorMessage = error
                                                    }
                                                }
                                            },
                                            onFriendClick = { clickedFriend ->
                                                // Gestione click su amico (opzionale)
                                                // Puoi implementare navigazione al profilo
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
