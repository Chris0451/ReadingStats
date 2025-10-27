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
import com.project.readingstats.features.profile.data.model.Friend
import com.project.readingstats.features.shelves.domain.model.UserBook
import com.project.readingstats.features.shelves.domain.model.ReadingStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailsScreen(
    friend: Friend,
    friendBooks: List<UserBook>,
    onBack: () -> Unit,
    onRemoveFriend:  (String, () -> Unit) -> Unit
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    // Filtra i libri per stato
    val readingBooks = friendBooks.filter { it.status == ReadingStatus.READING }
    val completedBooks = friendBooks.filter { it.status == ReadingStatus.READ }
    var isLoading by remember { mutableStateOf(false)}
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
            item { ReadingStatisticsCard(friendBooks) }
            item { CurrentReadingBooksCard(readingBooks) }
            item { CompletedBooksCard(completedBooks) }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                RemoveFriendButton(onClick = { showRemoveDialog = true })
                Spacer(modifier = Modifier.height(16.dp))
            }
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
                        isLoading = true
                        onRemoveFriend(friend.uid){
                            isLoading = false
                        }
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
        }
    }
}

@Composable
private fun ReadingStatisticsCard(allBooks: List<UserBook>) {
    // Calcola statistiche dai libri reali
    val totalReadTime = allBooks.sumOf { it.totalReadSeconds ?: 0L }
    val completedBooksCount = allBooks.count { it.status == ReadingStatus.READ }
    val averageTimePerBook = if (completedBooksCount > 0) {
        totalReadTime / completedBooksCount
    } else 0L

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
                color = Color(0xFF0D47A1)
            )

            StatisticRow(
                icon = Icons.Default.Schedule,
                label = "Tempo totale di lettura",
                value = formatTime(totalReadTime),
                valueColor = Color(0xFF1565C0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatisticRow(
                icon = Icons.Default.Book,
                label = "Libri completati",
                value = "$completedBooksCount libri",
                valueColor = Color(0xFF1565C0)
            )

            Spacer(modifier = Modifier.height(12.dp))

            StatisticRow(
                icon = Icons.Default.TrendingUp,
                label = "Tempo medio per libro",
                value = formatTime(averageTimePerBook),
                valueColor = Color(0xFF0D47A1)
            )
        }
    }
}

@Composable
private fun CurrentReadingBooksCard(readingBooks: List<UserBook>) {
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
                text = "Libri in Lettura (${readingBooks.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp),
                color = Color(0xFFE65100)
            )

            if (readingBooks.isEmpty()) {
                Text(
                    text = "Nessun libro in lettura al momento",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                readingBooks.forEachIndexed { index, book ->
                    CurrentBookItem(
                        title = book.title,
                        author = book.authors.joinToString(", "),
                        pagesRead = book.pageInReading ?: 0,
                        totalPages = book.pageCount ?: 1,
                        timeSpent = formatTime(book.totalReadSeconds ?: 0L)
                    )
                    if (index < readingBooks.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedBooksCard(completedBooks: List<UserBook>) {
    // Prendi solo gli ultimi 3 libri completati
    val recentCompleted = completedBooks.take(3)

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
                color = Color(0xFF1B5E20)
            )

            if (recentCompleted.isEmpty()) {
                Text(
                    text = "Nessun libro completato ancora",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                recentCompleted.forEachIndexed { index, book ->
                    CompletedBookItem(
                        title = book.title,
                        author = book.authors.joinToString(", "),
                        completionTime = formatTime(book.totalReadSeconds ?: 0L)
                    )
                    if (index < recentCompleted.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
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
                color = Color(0xFF212121),
                maxLines = 2
            )

            Text(
                text = "di $author",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF616161),
                maxLines = 1
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

                    val progress = if (totalPages > 0) pagesRead.toFloat() / totalPages.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = progress.coerceIn(0f, 1f),
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
    completionTime: String
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
                        color = Color(0xFF212121),
                        maxLines = 2
                    )

                    Text(
                        text = "di $author",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF616161),
                        maxLines = 1
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

            Text(
                text = "Tempo: $completionTime",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF2E7D32),
                fontWeight = FontWeight.Medium
            )
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

// Funzione helper per formattare i secondi in ore e minuti
private fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return when {
        hours > 0 -> "$hours ore, $minutes minuti"
        minutes > 0 -> "$minutes minuti"
        else -> "< 1 minuto"
    }
}
