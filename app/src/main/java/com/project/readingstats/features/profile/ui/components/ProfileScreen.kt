package com.project.readingstats.features.profile.ui.components

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.project.readingstats.features.auth.data.model.UserModelDto
import com.google.firebase.auth.FirebaseAuth // <--- IMPORT NECESSARIO

@Composable
fun ProfileScreen(
    user: UserModelDto?, // L'utente corrente con i suoi dati
    profileViewModel: ProfileViewModel, // ViewModel per gestire gli aggiornamenti
    onLogout: () -> Unit // Callback per il logout (reindirizzamento)
) {
    // Stato per gestire quale schermata mostrare
    var currentScreen by remember { mutableStateOf("profile") }

    // Osserva lo stato di caricamento dell'aggiornamento dal ViewModel
    val updateLoading by profileViewModel.updateLoading.collectAsState()
    // Osserva il risultato dell'aggiornamento dal ViewModel
    val updateResult by profileViewModel.updateResult.collectAsState()

    // Effetto collaterale che reagisce ai cambiamenti del risultato aggiornamento
    LaunchedEffect(updateResult) {
        when (updateResult) {
            "success" -> {
                currentScreen = "datiPersonali"
                profileViewModel.clearUpdateResult()
            }
            "error_update_failed" -> {
                currentScreen = "datiPersonali"
                profileViewModel.clearUpdateResult()
            }
        }
    }

    // Switch per mostrare la schermata corretta basata sullo stato
    when (currentScreen) {
        "profile" -> ProfileRoot(
            user = user,
            // MODIFICA: Logout immediato con Firebase e reindirizzamento
            onLogout = {
                FirebaseAuth.getInstance().signOut() // Effettua logout reale da Firebase
                onLogout() // Richiama la callback per reindirizzare o aggiornare lo stato
            },
            onDatiPersonali = { currentScreen = "datiPersonali" },
            onListaAmici = { currentScreen = "listaAmici" },
            onInfoSupporto = { currentScreen = "infoSupporto" },
        )
        "datiPersonali" -> DatiPersonali(
            user = user,
            onBack = { currentScreen = "profile" },
            onEdit = { currentScreen = "modificaDati" }
        )
        "modificaDati" -> ModificaDatiPersonali(
            user = user,
            onSave = { username, name, surname, email ->
                profileViewModel.updateUserProfile(username, name, surname, email)
            },
            onCancel = { currentScreen = "datiPersonali" },
            isLoading = updateLoading
        )
        "listaAmici" -> ListaAmici(onBack = { currentScreen = "profile" })
        "infoSupporto" -> InfoSupporto(onBack = { currentScreen = "profile" })
    }
}

@Composable
fun ProfileRoot(
    user: UserModelDto?, // L'utente corrente
    onLogout: () -> Unit, // Callback per il logout
    onDatiPersonali: () -> Unit = {}, // Callback per andare ai dati personali
    onListaAmici: () -> Unit = {}, // Callback per andare alla lista amici
    onInfoSupporto: () -> Unit = {}, // Callback per andare alle info supporto
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
        ) {
            // Immagine profilo
            AsyncImage(
                model = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png",
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Card con dati utente e menu
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nome Utente",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                    Text(
                        text = user?.username ?: "Caricamento...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )
                    Divider()

                    ProfileMenuItem(
                        icon = Icons.Default.Person,
                        title = "Dati Personali",
                        description = "Visualizza e modifica i tuoi dati personali.",
                        onClick = onDatiPersonali
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Star,
                        title = "Lista Amici",
                        description = "Visualizza i tuoi amici.",
                        onClick = onListaAmici
                    )
                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = "Info/Supporto",
                        description = "Guida e supporto.",
                        onClick = onInfoSupporto
                    )
                    // Pulsante logout aggiornato!!
                    Button(
                        onClick = onLogout, // La callback effettua prima il signOut e poi aggiorna la navigazione
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Esci dal profilo",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// Composable per rappresentare una voce di menu del profilo
@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector, // Icona della voce di menu
    title: String, // Titolo della voce
    description: String, // Descrizione della voce
    onClick: () -> Unit // Callback quando viene cliccata
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(18.dp)
        )
    }
}
