package com.project.readingstats.features.profile.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.project.readingstats.features.auth.data.model.UserModelDto

@Composable
fun ProfileScreen(
    user: UserModelDto?,
    profileViewModel: ProfileViewModel,
    onLogout: () -> Unit,
    onNavigateToFriends: () -> Unit = {}, // NUOVO PARAMETRO PER NAVIGAZIONE AMICI
    onDatiPersonali: () -> Unit = {},
    onInfoSupporto: () -> Unit = {}
) {
    var currentScreen by remember { mutableStateOf("profile") }
    val updateLoading by profileViewModel.updateLoading.collectAsState()
    val updateResult by profileViewModel.updateResult.collectAsState()

    // FIX: Gestiamo updateResult come String invece di Result
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

    BackHandler(enabled = currentScreen != "profile") {
        currentScreen = "profile"
    }

    when (currentScreen) {
        "profile" -> {
            ProfileRoot(
                user = user,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    onLogout()
                },
                onDatiPersonali = {
                    currentScreen = "datiPersonali"
                    onDatiPersonali()
                },
                onListaAmici = {
                    // ===== USA NAVIGAZIONE ESTERNA =====
                    onNavigateToFriends()
                },
                onInfoSupporto = {
                    currentScreen = "infoSupporto"
                    onInfoSupporto()
                }
            )
        }
        "datiPersonali" -> {
            ModificaDatiPersonali(
                user = user,
                profileViewModel = profileViewModel,
                onBack = { currentScreen = "profile" },
                updateLoading = updateLoading
            )
        }
        "infoSupporto" -> {
            // ===== USA LA FUNZIONE DEL FILE SEPARATO =====
            InfoSupporto(
                onBack = { currentScreen = "profile" }
            )
        }
    }
}

@Composable
fun ProfileRoot(
    user: UserModelDto?,
    onLogout: () -> Unit,
    onDatiPersonali: () -> Unit,
    onListaAmici: () -> Unit,
    onInfoSupporto: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ProfileHeader(user) }
            item {
                ProfileMenuCard(
                    user = user,
                    onDatiPersonali = onDatiPersonali,
                    onListaAmici = onListaAmici,
                    onInfoSupporto = onInfoSupporto
                )
            }
            item {
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text("Esci dal profilo", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(user: UserModelDto?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // FIX: Usa un campo che esiste davvero in UserModelDto o placeholder
        AsyncImage(
            model = user?.username ?: "", // CAMBIATO: usa username invece di avatarUrl
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(110.dp).clip(CircleShape)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            user?.username ?: "Loading...",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            user?.email ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileMenuCard(
    user: UserModelDto?,
    onDatiPersonali: () -> Unit,
    onListaAmici: () -> Unit,
    onInfoSupporto: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        LazyColumn(modifier = Modifier.padding(16.dp).heightIn(max = 300.dp)) {
            item {
                Text(
                    "Menu",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item {
                ProfileMenuItem(
                    Icons.Default.Person,
                    "Dati Personali",
                    "Visualizza e modifica i tuoi dati.",
                    onDatiPersonali
                )
            }
            item {
                ProfileMenuItem(
                    Icons.Default.Star,
                    "Lista Amici",
                    "Visualizza i tuoi amici.",
                    onListaAmici
                )
            }
            item {
                ProfileMenuItem(
                    Icons.Default.Info,
                    "Info Supporto",
                    "Guida e supporto.",
                    onInfoSupporto
                )
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
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Icon(
            icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun ModificaDatiPersonali(
    user: UserModelDto?,
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    updateLoading: Boolean
) {
    // Implementazione esistente - mantieni quello che hai già
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (updateLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Text("Dati Personali - Implementazione esistente")
            Button(onClick = onBack) {
                Text("Torna al profilo")
            }
        }
    }
}

// ===== RIMOSSA LA FUNZIONE InfoSupporto DUPLICATA =====
// La funzione InfoSupporto è ora definita solo nel file InfoSupporto.kt
