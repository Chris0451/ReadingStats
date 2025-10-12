package com.project.readingstats.features.home.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import coil.compose.AsyncImage

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun HomeScreen(
    onStartReading: () -> Unit = {} // Callback per avviare il timer di lettura
) {
    // Stati per i dati del libro (modificabili in futuro)
    var bookTitle by remember { mutableStateOf("L'arte di essere felici e vivere a lungo") }
    var bookImageUrl by remember { mutableStateOf("") } // URL immagine del libro

    // Ottieni dimensioni schermo per responsività
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp

    // Calcola dimensioni responsive
    val cardPadding = (screenWidth * 0.04f).coerceIn(12.dp, 24.dp)
    val bookImageWidth = min(screenWidth * 0.5f, 200.dp)
    val bookImageHeight = bookImageWidth * 1.4f // Mantiene proporzione libro

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {

        }
    }
}

// Composable di preview per testare il layout
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen()
    }
}

/*
* // Spaziatura responsive dall'alto
        Spacer(modifier = Modifier.height((screenHeight * 0.02f).coerceIn(16.dp, 32.dp)))

        // Card contenitore principale del libro
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = (screenWidth * 0.05f).coerceIn(16.dp, 24.dp),
                        vertical = (screenHeight * 0.025f).coerceIn(16.dp, 24.dp)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Immagine di copertina del libro - dimensioni responsive
                Card(
                    modifier = Modifier
                        .width(bookImageWidth)
                        .height(bookImageHeight),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    if (bookImageUrl.isNotEmpty()) {
                        // Se c'è un URL immagine, carica l'immagine
                        AsyncImage(
                            model = bookImageUrl,
                            contentDescription = "Copertina libro",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Placeholder per l'immagine del libro (simula la copertina nell'immagine)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF0E6D2)), // Colore beige come nell'immagine
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            ) {
                                // Decorazione blu in alto (simula pattern dell'immagine)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(bookImageHeight * 0.25f) // 25% dell'altezza
                                        .background(Color(0xFF87CEEB))
                                )

                                // Spaziatura adattiva
                                Spacer(modifier = Modifier.height(8.dp))

                                // Testo simulato sulla copertina - dimensioni responsive
                                Text(
                                    text = "SENECA",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize *
                                                (bookImageWidth.value / 200f).coerceIn(0.8f, 1.2f)
                                    ),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF8B4513),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "L'arte di essere felici\ne vivere a lungo",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = MaterialTheme.typography.bodySmall.fontSize *
                                                (bookImageWidth.value / 200f).coerceIn(0.7f, 1.1f)
                                    ),
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF8B4513),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Spaziatura responsive
                Spacer(modifier = Modifier.height((screenHeight * 0.03f).coerceIn(16.dp, 28.dp)))

                // Titolo del libro - dimensioni responsive
                Text(
                    text = bookTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize *
                                (screenWidth.value / 400f).coerceIn(0.8f, 1.2f)
                    ),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF212121),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Spaziatura tra card e pulsante
                Spacer(modifier = Modifier.height((screenHeight * 0.025f).coerceIn(16.dp, 24.dp)))

                // Pulsante per iniziare a leggere - sempre visibile
                Button(
                    onClick = onStartReading,
                    modifier = Modifier
                        .height((screenHeight * 0.06f).coerceIn(48.dp, 60.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2) // Blu come nell'immagine
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "🔄 Riprendi Lettura",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize *
                                    (screenWidth.value / 400f).coerceIn(0.9f, 1.1f)
                        ),
                        fontWeight = FontWeight.Medium
                    )
                }
            }


            // Spaziatura finale per evitare che il contenuto tocchi il bottom
            Spacer(modifier = Modifier.height((screenHeight * 0.03f).coerceIn(24.dp, 40.dp)))
*
* */
