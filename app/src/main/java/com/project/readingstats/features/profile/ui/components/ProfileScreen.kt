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

@Composable
fun ProfileScreen(
    user: UserModelDto?,
    onLogout: () -> Unit
) {
    var currentScreen by remember { mutableStateOf("profile") }

    when (currentScreen) {
        "profile" -> ProfileRoot(
            user = user,
            onLogout = onLogout,
            onDatiPersonali = { currentScreen = "datiPersonali" },
            onListaAmici = { currentScreen = "listaAmici" },
            onInfoSupporto = { currentScreen = "infoSupporto" },
            onNotifiche = {}
        )
        "datiPersonali" -> DatiPersonali(onBack = { currentScreen = "profile" })
        "listaAmici" -> ListaAmici(onBack = { currentScreen = "profile" })
        "infoSupporto" -> InfoSupporto(onBack = { currentScreen = "profile" })
    }
}

@Composable
fun ProfileRoot(
    user: UserModelDto?,
    onLogout: () -> Unit,
    onDatiPersonali: () -> Unit = {},
    onListaAmici: () -> Unit = {},
    onInfoSupporto: () -> Unit = {},
    onNotifiche: () -> Unit = {}
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
            AsyncImage(
                model = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png",
                contentDescription = "Profile picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(24.dp))
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
                        text = user?.username ?: "Caricamento...",  // MOSTRA IL NOME DINAMICO!
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
                    ProfileMenuItem(
                        icon = Icons.Default.Mail,
                        title = "Notifiche",
                        description = "Gestisci le notifiche.",
                        onClick = onNotifiche
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onLogout,
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

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
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
