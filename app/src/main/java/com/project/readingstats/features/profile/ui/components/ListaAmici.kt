package com.project.readingstats.features.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material.icons.filled.Sync
import com.google.firebase.Timestamp

// Data class per rappresentare un amico con tutti i suoi dati principali
data class Friend(
    val uid: String = "",        // UID univoco dell'utente amico
    val name: String = "",       // Nome dell'amico
    val surname: String = "",    // Cognome dell'amico
    val username: String = "",   // Username dell'amico
    val email: String = ""       // Email dell'amico
)

// ✅ NUOVA: Data class per rappresentare una richiesta di amicizia
data class FriendRequestDto(
    val fromUid: String = "",        // UID mittente
    val toUid: String = "",          // UID destinatario
    val status: String = "pending",  // Stato ("pending", "accepted", "rejected")
    val timestamp: Timestamp? = null, // Data/ora invio
    val message: String = ""         // Messaggio personalizzato (opzionale)
)

// ✅ AGGIORNATO: Enum per definire lo stato della relazione tra utenti (con PENDING)
enum class UserRelationshipStatus {
    NOT_FRIEND,      // Non è amico - mostra pulsante + per aggiungere
    PENDING,         // Richiesta inviata in attesa - mostra icona rotella
    IS_FRIEND        // È già amico - mostra spunta di conferma
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaAmici(onBack: () -> Unit) {
    // ✅ AGGIORNATO: Stati per gestire la tab selezionata (0=Amici, 1=Richieste, 2=Utenti)
    var selectedTab by remember { mutableStateOf(0) }
    // Stato per il valore della barra di ricerca
    var searchValue by remember { mutableStateOf("") }

    // ✅ AGGIORNATO: Stati per la gestione dei dati delle tre diverse liste
    var friendsList by remember { mutableStateOf<List<Friend>>(emptyList()) }     // Lista amici
    var requestsList by remember { mutableStateOf<List<FriendRequestDto>>(emptyList()) } // ✅ Lista richieste ricevute
    var allUsersList by remember { mutableStateOf<List<Friend>>(emptyList()) }    // Lista tutti utenti
    var sentRequestsList by remember { mutableStateOf<List<String>>(emptyList()) } // ✅ Lista UID richieste inviate
    var isLoading by remember { mutableStateOf(true) }                            // Stato caricamento
    var errorMessage by remember { mutableStateOf<String?>(null) }                // Messaggio errore

    // ✅ AGGIORNATO: Effetto che si attiva quando cambia la tab selezionata per caricare i dati appropriati
    LaunchedEffect(selectedTab) {
        isLoading = true    // Inizia il caricamento
        errorMessage = null // Reset del messaggio di errore

        when (selectedTab) {
            0 -> { // Tab "Amici" - carica la lista degli amici
                loadFriends { friends, error ->
                    friendsList = friends
                    errorMessage = error
                    isLoading = false
                }
            }
            1 -> { // ✅ NUOVO: Tab "Richieste" - carica le richieste di amicizia ricevute
                loadReceivedRequests { requests, error ->
                    requestsList = requests
                    errorMessage = error
                    isLoading = false
                }
            }
            2 -> { // Tab "Utenti" - carica tutti gli utenti della piattaforma
                loadAllUsers { users, error ->
                    allUsersList = users
                    errorMessage = error
                    isLoading = false
                }
                // ✅ NUOVO: Carica anche le richieste inviate per mostrare lo stato corretto
                loadSentRequests { sentRequests, _ ->
                    sentRequestsList = sentRequests
                }
            }
        }
    }

    // ✅ AGGIORNATO: Determina quale lista mostrare in base alla tab selezionata
    val currentList = when (selectedTab) {
        0 -> friendsList
        1 -> requestsList.map { request -> // Converte richieste in Friend per visualizzazione
            Friend(
                uid = request.fromUid,
                name = "", // Questi dati verranno caricati separatamente
                surname = "",
                username = "",
                email = ""
            )
        }
        2 -> allUsersList
        else -> emptyList()
    }

    // ✅ AGGIORNATO: Filtra la lista corrente in base al testo inserito nella ricerca
    val filteredList = if (selectedTab != 1) {
        currentList.filter { friend ->
            if (searchValue.isEmpty()) {
                true // Se la ricerca è vuota, mostra tutti
            } else {
                // Cerca nel nome, cognome o username (case insensitive)
                friend.name.contains(searchValue, ignoreCase = true) ||
                        friend.surname.contains(searchValue, ignoreCase = true) ||
                        friend.username.contains(searchValue, ignoreCase = true)
            }
        }
    } else {
        requestsList // Per le richieste mostriamo la lista originale
    }

    // Struttura principale della schermata con Scaffold
    Scaffold(
        topBar = {
            // Barra superiore con titolo e pulsante indietro
            TopAppBar(
                title = { Text("Lista Amici") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
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

                        // Chip per la sezione "Utenti"
                        FilterChip(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            label = { Text("Utenti") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Black,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFE0E0E0),
                                labelColor = Color.Black
                            )
                        )
                    }
                }
            }
            Spacer(Modifier.height(18.dp))

            // ✅ AGGIORNATO: Contenuto principale che cambia in base allo stato
            when {
                // Mostra indicatore di caricamento
                isLoading -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                // Mostra messaggio di errore se presente
                errorMessage != null -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Errore: $errorMessage",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // ✅ AGGIORNATO: Mostra messaggio quando la lista filtrata è vuota
                (selectedTab != 1 && filteredList.isEmpty()) || (selectedTab == 1 && requestsList.isEmpty()) -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        // Messaggio personalizzato in base al contesto
                        val message = when {
                            searchValue.isNotEmpty() -> "Nessun risultato per '$searchValue'"
                            selectedTab == 0 -> "Nessun amico trovato"
                            selectedTab == 1 -> "Nessuna richiesta ricevuta"
                            selectedTab == 2 -> "Nessun utente trovato"
                            else -> "Nessun dato trovato"
                        }
                        Text(
                            text = message,
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // ✅ AGGIORNATO: Mostra la lista degli elementi
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (selectedTab) {
                            0, 2 -> { // Tab Amici o Utenti
                                items(if (selectedTab == 0) friendsList else filteredList as List<Friend>) { friend ->
                                    FriendItem(
                                        friend = friend,
                                        isInUsersSection = selectedTab == 2,
                                        friendsList = friendsList,
                                        sentRequestsList = sentRequestsList,
                                        onSendRequest = { friendToAdd ->
                                            // ✅ NUOVO: Invia richiesta invece di aggiungere direttamente
                                            sendFriendRequest(friendToAdd.uid) { success, error ->
                                                if (success) {
                                                    // Aggiorna la lista locale delle richieste inviate per UI reattiva
                                                    sentRequestsList = sentRequestsList + friendToAdd.uid
                                                    println("Richiesta inviata con successo a: ${friendToAdd.username}")
                                                } else {
                                                    println("Errore invio richiesta: $error")
                                                    errorMessage = error
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                            1 -> { // ✅ NUOVO: Tab Richieste
                                items(requestsList) { request ->
                                    RequestItem(
                                        request = request,
                                        onAccept = { acceptedRequest ->
                                            // ✅ Accetta la richiesta
                                            acceptFriendRequest(acceptedRequest) { success, error ->
                                                if (success) {
                                                    // Rimuovi dalla lista richieste e ricarica gli amici
                                                    requestsList = requestsList.filter { it.fromUid != acceptedRequest.fromUid }
                                                    loadFriends { friends, _ ->
                                                        friendsList = friends
                                                    }
                                                    println("Richiesta accettata da: ${acceptedRequest.fromUid}")
                                                } else {
                                                    println("Errore accettazione richiesta: $error")
                                                    errorMessage = error
                                                }
                                            }
                                        },
                                        onReject = { rejectedRequest ->
                                            // ✅ Rifiuta la richiesta
                                            rejectFriendRequest(rejectedRequest) { success, error ->
                                                if (success) {
                                                    // Rimuovi dalla lista richieste
                                                    requestsList = requestsList.filter { it.fromUid != rejectedRequest.fromUid }
                                                    println("Richiesta rifiutata da: ${rejectedRequest.fromUid}")
                                                } else {
                                                    println("Errore rifiuto richiesta: $error")
                                                    errorMessage = error
                                                }
                                            }
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

// ✅ AGGIORNATO: Composable per visualizzare un singolo elemento amico
@Composable
fun FriendItem(
    friend: Friend,                                    // Dati dell'amico
    isInUsersSection: Boolean = false,                // Se siamo nella sezione "Utenti"
    friendsList: List<Friend> = emptyList(),          // Lista amici correnti
    sentRequestsList: List<String> = emptyList(),     // ✅ Lista richieste inviate
    onSendRequest: (Friend) -> Unit = {}              // ✅ Callback per inviare richiesta
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Implementa azione click */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar circolare dell'amico
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE57373)), // Colore di sfondo dell'avatar
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Colonna con le informazioni dell'amico
            Column(modifier = Modifier.weight(1f)) {
                // Nome completo in grassetto
                Text(
                    text = "${friend.name} ${friend.surname}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                // Username in grigio
                Text(
                    text = "@${friend.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Mostra pulsanti di azione solo nella sezione "Utenti"
            if (isInUsersSection) {
                Spacer(modifier = Modifier.width(8.dp))

                // ✅ Determina lo stato della relazione con questo utente (incluso PENDING)
                val relationshipStatus = getRelationshipStatus(friend, friendsList, sentRequestsList)

                // Mostra l'icona appropriata in base allo stato
                when (relationshipStatus) {
                    UserRelationshipStatus.NOT_FRIEND -> {
                        // Pulsante verde per inviare richiesta di amicizia
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
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Invia richiesta amicizia",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    UserRelationshipStatus.PENDING -> {
                        // Icona arancione per richiesta inviata in attesa
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF9800)), // Arancione
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = "Richiesta in attesa",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    UserRelationshipStatus.IS_FRIEND -> {
                        // Icona blu per amico già aggiunto
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.Blue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Già amico",
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

// ✅ NUOVO: Composable per visualizzare una richiesta ricevuta
@Composable
fun RequestItem(
    request: FriendRequestDto,
    onAccept: (FriendRequestDto) -> Unit = {},
    onReject: (FriendRequestDto) -> Unit = {}
) {
    // Stato per i dati dell'utente mittente
    var senderUser by remember { mutableStateOf<Friend?>(null) }

    // Carica i dati del mittente
    LaunchedEffect(request.fromUid) {
        loadUserById(request.fromUid) { user ->
            senderUser = user
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del mittente
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)), // Verde per le richieste
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informazioni del mittente
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = senderUser?.let { "${it.name} ${it.surname}" } ?: "Caricamento...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = senderUser?.let { "@${it.username}" } ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                if (request.message.isNotEmpty()) {
                    Text(
                        text = request.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Pulsanti Accetta/Rifiuta
            Row {
                // Pulsante Accetta
                IconButton(
                    onClick = { onAccept(request) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)), // Verde
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Accetta richiesta",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Pulsante Rifiuta
                IconButton(
                    onClick = { onReject(request) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF44336)), // Rosso
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Rifiuta richiesta",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ✅ AGGIORNATA: Funzione per determinare lo stato della relazione (con PENDING)
private fun getRelationshipStatus(
    user: Friend,
    friendsList: List<Friend>,
    sentRequestsList: List<String>
): UserRelationshipStatus {
    // Controlla se l'utente è già nella lista degli amici
    val isAlreadyFriend = friendsList.any { it.uid == user.uid }

    if (isAlreadyFriend) {
        return UserRelationshipStatus.IS_FRIEND
    }

    // Controlla se è stata inviata una richiesta a questo utente
    val hasSentRequest = sentRequestsList.contains(user.uid)

    return if (hasSentRequest) {
        UserRelationshipStatus.PENDING
    } else {
        UserRelationshipStatus.NOT_FRIEND
    }
}

// ✅ NUOVA: Funzione per inviare richiesta di amicizia
private fun sendFriendRequest(toUid: String, onResult: (Boolean, String?) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onResult(false, "Utente non autenticato")
        return
    }

    // Validazione UID
    if (toUid.isBlank() || toUid == "users" || toUid.length <= 10 || toUid.contains("/")) {
        onResult(false, "UID destinatario non valido")
        return
    }

    val db = FirebaseFirestore.getInstance()

    // Crea il documento della richiesta
    val requestData = hashMapOf(
        "fromUid" to currentUser.uid,
        "toUid" to toUid,
        "status" to "pending",
        "timestamp" to Timestamp.now(),
        "message" to "" // Messaggio vuoto per ora
    )

    // Salva nella collection "friend_requests"
    db.collection("friend_requests")
        .add(requestData)
        .addOnSuccessListener {
            onResult(true, null)
        }
        .addOnFailureListener { exception ->
            onResult(false, "Errore invio richiesta: ${exception.message}")
        }
}

// ✅ NUOVA: Funzione per caricare richieste ricevute
private fun loadReceivedRequests(onResult: (List<FriendRequestDto>, String?) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onResult(emptyList(), "Utente non autenticato")
        return
    }

    val db = FirebaseFirestore.getInstance()
    db.collection("friend_requests")
        .whereEqualTo("toUid", currentUser.uid)
        .whereEqualTo("status", "pending")
        .get()
        .addOnSuccessListener { querySnapshot ->
            val requests = querySnapshot.documents.mapNotNull { doc ->
                try {
                    FriendRequestDto(
                        fromUid = doc.getString("fromUid") ?: "",
                        toUid = doc.getString("toUid") ?: "",
                        status = doc.getString("status") ?: "pending",
                        timestamp = doc.getTimestamp("timestamp"),
                        message = doc.getString("message") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            onResult(requests, null)
        }
        .addOnFailureListener { exception ->
            onResult(emptyList(), "Errore caricamento richieste: ${exception.message}")
        }
}

// ✅ NUOVA: Funzione per caricare richieste inviate
private fun loadSentRequests(onResult: (List<String>, String?) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onResult(emptyList(), "Utente non autenticato")
        return
    }

    val db = FirebaseFirestore.getInstance()
    db.collection("friend_requests")
        .whereEqualTo("fromUid", currentUser.uid)
        .whereEqualTo("status", "pending")
        .get()
        .addOnSuccessListener { querySnapshot ->
            val sentRequests = querySnapshot.documents.mapNotNull { doc ->
                doc.getString("toUid")
            }
            onResult(sentRequests, null)
        }
        .addOnFailureListener { exception ->
            onResult(emptyList(), "Errore caricamento richieste inviate: ${exception.message}")
        }
}

// ✅ NUOVA: Funzione per accettare una richiesta di amicizia
private fun acceptFriendRequest(request: FriendRequestDto, onResult: (Boolean, String?) -> Unit) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onResult(false, "Utente non autenticato")
        return
    }

    val db = FirebaseFirestore.getInstance()

    // 1. Aggiorna lo status della richiesta a "accepted"
    db.collection("friend_requests")
        .whereEqualTo("fromUid", request.fromUid)
        .whereEqualTo("toUid", request.toUid)
        .whereEqualTo("status", "pending")
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (querySnapshot.documents.isNotEmpty()) {
                val requestDoc = querySnapshot.documents[0]

                // Aggiorna lo status
                requestDoc.reference.update("status", "accepted")
                    .addOnSuccessListener {
                        // 2. Aggiungi reciprocamente gli amici
                        addFriendsReciprocally(request.fromUid, request.toUid) { success, error ->
                            onResult(success, error)
                        }
                    }
                    .addOnFailureListener { exception ->
                        onResult(false, "Errore aggiornamento richiesta: ${exception.message}")
                    }
            } else {
                onResult(false, "Richiesta non trovata")
            }
        }
        .addOnFailureListener { exception ->
            onResult(false, "Errore ricerca richiesta: ${exception.message}")
        }
}

// ✅ NUOVA: Funzione per rifiutare una richiesta di amicizia
private fun rejectFriendRequest(request: FriendRequestDto, onResult: (Boolean, String?) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    db.collection("friend_requests")
        .whereEqualTo("fromUid", request.fromUid)
        .whereEqualTo("toUid", request.toUid)
        .whereEqualTo("status", "pending")
        .get()
        .addOnSuccessListener { querySnapshot ->
            if (querySnapshot.documents.isNotEmpty()) {
                val requestDoc = querySnapshot.documents[0]

                // Aggiorna lo status a "rejected"
                requestDoc.reference.update("status", "rejected")
                    .addOnSuccessListener {
                        onResult(true, null)
                    }
                    .addOnFailureListener { exception ->
                        onResult(false, "Errore rifiuto richiesta: ${exception.message}")
                    }
            } else {
                onResult(false, "Richiesta non trovata")
            }
        }
        .addOnFailureListener { exception ->
            onResult(false, "Errore ricerca richiesta: ${exception.message}")
        }
}

// ✅ NUOVA: Funzione per aggiungere amici reciprocamente
private fun addFriendsReciprocally(uid1: String, uid2: String, onResult: (Boolean, String?) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    // Aggiunge uid2 alla lista amici di uid1
    db.collection("users").document(uid1).get()
        .addOnSuccessListener { doc1 ->
            val friends1 = doc1.get("friends") as? MutableList<String> ?: mutableListOf()
            if (!friends1.contains(uid2)) {
                friends1.add(uid2)

                doc1.reference.update("friends", friends1)
                    .addOnSuccessListener {
                        // Aggiunge uid1 alla lista amici di uid2
                        db.collection("users").document(uid2).get()
                            .addOnSuccessListener { doc2 ->
                                val friends2 = doc2.get("friends") as? MutableList<String> ?: mutableListOf()
                                if (!friends2.contains(uid1)) {
                                    friends2.add(uid1)

                                    doc2.reference.update("friends", friends2)
                                        .addOnSuccessListener {
                                            onResult(true, null)
                                        }
                                        .addOnFailureListener { exception ->
                                            onResult(false, "Errore aggiunta amico reciproca: ${exception.message}")
                                        }
                                } else {
                                    onResult(true, null)
                                }
                            }
                            .addOnFailureListener { exception ->
                                onResult(false, "Errore caricamento utente 2: ${exception.message}")
                            }
                    }
                    .addOnFailureListener { exception ->
                        onResult(false, "Errore aggiornamento utente 1: ${exception.message}")
                    }
            } else {
                onResult(true, null)
            }
        }
        .addOnFailureListener { exception ->
            onResult(false, "Errore caricamento utente 1: ${exception.message}")
        }
}

// ✅ NUOVA: Funzione per caricare i dati di un utente specifico
private fun loadUserById(uid: String, onResult: (Friend?) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users")
        .document(uid)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                try {
                    val user = Friend(
                        uid = document.getString("uid") ?: "",
                        name = document.getString("name") ?: "",
                        surname = document.getString("surname") ?: "",
                        username = document.getString("username") ?: "",
                        email = document.getString("email") ?: ""
                    )
                    onResult(user)
                } catch (e: Exception) {
                    onResult(null)
                }
            } else {
                onResult(null)
            }
        }
        .addOnFailureListener {
            onResult(null)
        }
}

// ✅ FUNZIONE CORRETTA: Carica array di amici con validazione UID per evitare crash
private fun loadFriends(onResult: (List<Friend>, String?) -> Unit) {
    // Verifica che l'utente sia autenticato
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onResult(emptyList(), "Utente non autenticato")
        return
    }

    val db = FirebaseFirestore.getInstance()
    db.collection("users")
        .document(currentUser.uid)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                try {
                    // ✅ Legge array di stringhe dal campo "friends" con gestione sicura
                    val friendUids = document.get("friends") as? List<String> ?: emptyList()

                    // ✅ VALIDAZIONE UID FONDAMENTALE - Filtra UID non validi per evitare crash
                    val validUids = friendUids.filter { uid ->
                        uid.isNotBlank() &&           // Non deve essere vuoto
                                uid != "users" &&             // Non deve essere la stringa "users"
                                uid.length > 10 &&            // Deve avere lunghezza minima ragionevole
                                !uid.contains("/") &&         // Non deve contenere caratteri di path
                                uid != currentUser.uid        // Non deve essere l'utente stesso (evita self-reference)
                    }

                    // Se non ci sono UID validi, restituisce lista vuota senza errore
                    if (validUids.isEmpty()) {
                        onResult(emptyList(), null)
                        return@addOnSuccessListener
                    }

                    // ✅ Carica tutti gli amici da array di UID validati
                    val friendsList = mutableListOf<Friend>()
                    var completedRequests = 0        // Contatore richieste completate
                    val totalRequests = validUids.size // Totale richieste da fare

                    // Itera attraverso ogni UID valido per caricare i dati dell'amico
                    validUids.forEach { friendUid ->
                        // ✅ Query sicura con UID già validato
                        db.collection("users")
                            .document(friendUid)
                            .get()
                            .addOnSuccessListener { friendDoc ->
                                // Se il documento amico esiste, crea l'oggetto Friend
                                if (friendDoc.exists()) {
                                    try {
                                        val friend = Friend(
                                            uid = friendDoc.getString("uid") ?: "",
                                            name = friendDoc.getString("name") ?: "",
                                            surname = friendDoc.getString("surname") ?: "",
                                            username = friendDoc.getString("username") ?: "",
                                            email = friendDoc.getString("email") ?: ""
                                        )
                                        friendsList.add(friend)
                                    } catch (e: Exception) {
                                        // Log errore ma continua con gli altri amici
                                        println("Errore parsing friend $friendUid: ${e.message}")
                                    }
                                }

                                // Incrementa il contatore delle richieste completate
                                completedRequests++
                                // Se tutte le richieste sono completate, restituisce il risultato
                                if (completedRequests == totalRequests) {
                                    onResult(friendsList.toList(), null)
                                }
                            }
                            .addOnFailureListener { exception ->
                                // In caso di errore, logga e continua con gli altri
                                println("Errore caricamento friend $friendUid: ${exception.message}")
                                completedRequests++
                                // Se tutte le richieste sono completate, restituisce quello che c'è
                                if (completedRequests == totalRequests) {
                                    onResult(friendsList.toList(),
                                        if (friendsList.isEmpty()) "Errore nel caricamento degli amici" else null)
                                }
                            }
                    }
                } catch (e: Exception) {
                    // Gestisce errori di parsing del campo "friends"
                    onResult(emptyList(), "Errore parsing campo friends: ${e.message}")
                }
            } else {
                // Il documento utente non esiste
                onResult(emptyList(), "Documento utente non trovato")
            }
        }
        .addOnFailureListener { exception ->
            // Errore nel caricamento del documento utente principale
            onResult(emptyList(), "Errore nel caricamento profilo: ${exception.message}")
        }
}

// Funzione per caricare tutti gli utenti registrati nella piattaforma
private fun loadAllUsers(onResult: (List<Friend>, String?) -> Unit) {
    // Verifica che l'utente sia autenticato
    val currentUser = FirebaseAuth.getInstance().currentUser
    if (currentUser == null) {
        onResult(emptyList(), "Utente non autenticato")
        return
    }

    val db = FirebaseFirestore.getInstance()
    db.collection("users")
        .get()
        .addOnSuccessListener { querySnapshot ->
            // Mappa tutti i documenti in oggetti Friend, escludendo l'utente corrente
            val users = querySnapshot.documents.mapNotNull { doc ->
                try {
                    // Escludi l'utente corrente dalla lista
                    if (doc.id != currentUser.uid) {
                        Friend(
                            uid = doc.getString("uid") ?: "",
                            name = doc.getString("name") ?: "",
                            surname = doc.getString("surname") ?: "",
                            username = doc.getString("username") ?: "",
                            email = doc.getString("email") ?: ""
                        )
                    } else null // Se è l'utente corrente, escludilo
                } catch (e: Exception) {
                    // Se c'è un errore nel parsing di questo utente, saltalo
                    null
                }
            }
            onResult(users, null)
        }
        .addOnFailureListener { exception ->
            // Errore nel caricamento della collection utenti
            onResult(emptyList(), "Errore nel caricamento utenti: ${exception.message}")
        }
}
