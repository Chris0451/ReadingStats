package com.project.readingstats.features.profile.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailsScreen(
    friend: Friend,
    onBack: () -> Unit,
    onRemoveFriend: () -> Unit
) {
    var showRemoveDialog by remember { mutableStateOf(false) }
    BackHandler { onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dettagli Amico") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { FriendProfileHeader(friend) }
            item { ReadingStatisticsCard() }
            item { CurrentReadingBooksCard() }
            item { CompletedBooksCard() }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                RemoveFriendButton(onClick = { showRemoveDialog = true })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showRemoveDialog) {
            AlertDialog(
                onDismissRequest = { showRemoveDialog = false },
                title = { Text("Rimuovi Amicizia", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Sei sicuro di voler rimuovere ${friend.username} dalla tua lista amici?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRemoveDialog = false
                            onRemoveFriend()
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Rimuovi", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveDialog = false }) {
                        Text("Annulla")
                    }
                }
            )
        }
    }
}

@Composable
private fun FriendProfileHeader(friend: Friend) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = "Avatar ${friend.username}",
                    modifier = Modifier.size(90.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = friend.username,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = friend.fullName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (friend.email.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = friend.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Membro dal: 15 Gennaio 2024",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReadingStatisticsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE3F2FD) // Blu chiaro
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Statistiche di Lettura",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color(0xFF0D47A1) // Blu scuro per contrasto
            )

            StatisticRow(
                icon = Icons.Default.Schedule,
                label = "Tempo totale di lettura",
                value = "127 ore, 34 minuti",
                valueColor = Color(0xFF1565C0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatisticRow(
                icon = Icons.Default.Book,
                label = "Libri completati",
                value = "23 libri",
                valueColor = Color(0xFF1565C0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatisticRow(
                icon = Icons.Default.TrendingUp,
                label = "Tempo medio per libro",
                value = "5 ore, 32 minuti",
                valueColor = Color(0xFF0D47A1)
            )
        }
    }
}

@Composable
private fun CurrentReadingBooksCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0) // Arancione chiaro
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Libri in Lettura (2)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color(0xFFE65100) // Arancione scuro
            )

            CurrentBookItem(
                title = "Il Nome della Rosa",
                author = "Umberto Eco",
                pagesRead = 245,
                totalPages = 612,
                timeSpent = "12 ore, 23 minuti"
            )

            Spacer(modifier = Modifier.height(12.dp))

            CurrentBookItem(
                title = "1984",
                author = "George Orwell",
                pagesRead = 87,
                totalPages = 328,
                timeSpent = "4 ore, 56 minuti"
            )
        }
    }
}

@Composable
private fun CompletedBooksCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9) // Verde chiaro
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Ultimi Libri Completati",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color(0xFF1B5E20) // Verde scuro
            )

            CompletedBookItem(
                title = "Il Piccolo Principe",
                author = "Antoine de Saint-Exupéry",
                completionTime = "3 ore, 45 minuti",
                completedDate = "5 giorni fa"
            )

            Spacer(modifier = Modifier.height(12.dp))

            CompletedBookItem(
                title = "Fahrenheit 451",
                author = "Ray Bradbury",
                completionTime = "5 ore, 12 minuti",
                completedDate = "2 settimane fa"
            )

            Spacer(modifier = Modifier.height(12.dp))

            CompletedBookItem(
                title = "L'Alchimista",
                author = "Paulo Coelho",
                completionTime = "4 ore, 28 minuti",
                completedDate = "1 mese fa"
            )
        }
    }
}



@Composable
private fun CurrentBookItem(
    title: String,
    author: String,
    pagesRead: Int,
    totalPages: Int,
    timeSpent: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121)
            )

            Text(
                text = "di $author",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF616161)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Progresso: $pagesRead/$totalPages pagine",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242)
                    )

                    LinearProgressIndicator(
                        progress = pagesRead.toFloat() / totalPages.toFloat(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        color = Color(0xFFFF6F00),
                        trackColor = Color(0xFFFFE0B2)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = timeSpent,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFFF6F00),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CompletedBookItem(
    title: String,
    author: String,
    completionTime: String,
    completedDate: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF212121)
                    )

                    Text(
                        text = "di $author",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF616161)
                    )
                }

                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Completato",
                    modifier = Modifier.size(20.dp),
                    tint = Color(0xFF2E7D32)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tempo: $completionTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32),
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = completedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }
        }
    }
}

@Composable
private fun StatisticRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = valueColor
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF424242)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun RemoveFriendButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            Icons.Default.PersonRemove,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Rimuovi Amicizia",
            color = MaterialTheme.colorScheme.onError,
            fontWeight = FontWeight.Bold
        )
    }
}
