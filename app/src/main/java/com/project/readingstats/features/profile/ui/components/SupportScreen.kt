package com.project.readingstats.features.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoSupporto(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var isDeleting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Info e Supporto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sezione App Info
            InfoSection(
                title = "Informazioni App",
                items = listOf(
                    InfoItem(
                        icon = Icons.Outlined.Info,
                        title = "Versione",
                        description = "1.0.0"
                    ),
                    InfoItem(
                        icon = Icons.Outlined.Android,
                        title = "Piattaforma",
                        description = "Android"
                    )
                )
            )

            // Sezione Supporto
            InfoSection(
                title = "Supporto",
                items = listOf(
                    InfoItem(
                        icon = Icons.Outlined.Email,
                        title = "Contattaci",
                        description = "support@readingstats.com"
                    ),
                    InfoItem(
                        icon = Icons.Outlined.Help,
                        title = "FAQ",
                        description = "Domande frequenti"
                    )
                )
            )

            // Sezione Privacy
            InfoSection(
                title = "Privacy e Sicurezza",
                items = listOf(
                    InfoItem(
                        icon = Icons.Outlined.Shield,
                        title = "Privacy Policy",
                        description = "Leggi la nostra politica sulla privacy"
                    ),
                    InfoItem(
                        icon = Icons.Outlined.Security,
                        title = "Termini di Servizio",
                        description = "Leggi i termini e condizioni"
                    )
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pulsante Elimina Account
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        "Zona Pericolosa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        "Questa azione è irreversibile. Tutti i tuoi dati verranno eliminati permanentemente.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF666666),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Elimina Account", fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Dialog di conferma eliminazione
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Elimina Account") },
            text = {
                Text("Sei sicuro di voler eliminare il tuo account? Questa azione è irreversibile e tutti i tuoi dati verranno persi.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        showPasswordDialog = true
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    Text("Elimina")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    // Dialog per inserire password
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showPasswordDialog = false
                    password = ""
                    errorMessage = ""
                }
            },
            title = { Text("Conferma Password") },
            text = {
                Column {
                    Text(
                        "Inserisci la tua password per confermare l'eliminazione dell'account.",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isDeleting,
                        isError = errorMessage.isNotEmpty(),
                        supportingText = if (errorMessage.isNotEmpty()) {
                            { Text(errorMessage, color = Color(0xFFD32F2F)) }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (password.isBlank()) {
                            errorMessage = "Inserisci la password"
                            return@TextButton
                        }

                        isDeleting = true
                        val user = FirebaseAuth.getInstance().currentUser
                        val db = FirebaseFirestore.getInstance()

                        if (user != null && user.email != null) {
                            val credential = EmailAuthProvider.getCredential(user.email!!, password)

                            user.reauthenticate(credential)
                                .addOnSuccessListener {
                                    db.collection("users").document(user.uid)
                                        .delete()
                                        .addOnSuccessListener {
                                            db.collection("usernames")
                                                .whereEqualTo("uid", user.uid)
                                                .get()
                                                .addOnSuccessListener { docs ->
                                                    for (doc in docs) {
                                                        doc.reference.delete()
                                                    }
                                                }
                                            user.delete()
                                                .addOnSuccessListener {
                                                    isDeleting = false
                                                    showPasswordDialog = false
                                                    onAccountDeleted()
                                                }
                                                .addOnFailureListener { e ->
                                                    isDeleting = false
                                                    errorMessage = "Errore: ${e.message}"
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            isDeleting = false
                                            errorMessage = "Errore nella rimozione dati: ${e.message}"
                                        }
                                }
                                .addOnFailureListener {
                                    isDeleting = false
                                    errorMessage = "Password errata"
                                }
                        }
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFD32F2F)
                    )
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Conferma")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        password = ""
                        errorMessage = ""
                    },
                    enabled = !isDeleting
                ) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
private fun InfoSection(
    title: String,
    items: List<InfoItem>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            items.forEachIndexed { index, item ->
                InfoItemRow(item)
                if (index < items.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = Color(0xFFE0E0E0)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoItemRow(item: InfoItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = Color(0xFF2196F3),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                item.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF666666)
            )
        }
    }
}

private data class InfoItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)
