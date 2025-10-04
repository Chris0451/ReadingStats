package com.project.readingstats.features.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatiPersonali(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = { Text("") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Torna indietro")
                }
            }
        )
        Spacer(Modifier.height(12.dp))
        AsyncImage(
            model = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png",
            contentDescription = "Profile picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Icon(Icons.Default.Person, contentDescription = "Icona dati personali")
            Spacer(Modifier.width(8.dp))
            Text("Dati Personali", color = Color.White)
        }
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.92f),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(
                Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LabelAndPlaceholder(label = "Nome Utente")
                LabelAndPlaceholder(label = "Name")
                LabelAndPlaceholder(label = "Surname")
                LabelAndPlaceholder(label = "Email")
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Text("Modifica", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun LabelAndPlaceholder(label: String) {
    Column(
        Modifier
            .padding(vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Text(label, modifier = Modifier.padding(start = 4.dp))
        Box(
            Modifier
                .padding(top = 2.dp)
                .fillMaxWidth()
                .height(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF2F2F2)),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("Value", color = Color.Gray, modifier = Modifier.padding(start = 10.dp))
        }
    }
}
