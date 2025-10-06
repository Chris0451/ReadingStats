package com.project.readingstats.features.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.project.readingstats.features.auth.data.model.UserModelDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificaDatiPersonali(
    user: UserModelDto?, // L'utente corrente con i dati attuali
    onSave: (String, String, String, String) -> Unit, // Callback per salvare (username, name, surname, email)
    onCancel: () -> Unit, // Callback per tornare indietro
    isLoading: Boolean = false // Stato di caricamento durante il salvataggio
) {
    // Stati per i campi di testo - inizializzati con i valori attuali dell'utente
    var usernameState by remember {
        mutableStateOf(TextFieldValue(user?.username ?: ""))
    }
    var nameState by remember {
        mutableStateOf(TextFieldValue(user?.name ?: ""))
    }
    var surnameState by remember {
        mutableStateOf(TextFieldValue(user?.surname ?: ""))
    }
    var emailState by remember {
        mutableStateOf(TextFieldValue(user?.email ?: ""))
    }
    /* Campo password fittizio (non modificabile nei dati utente reali)
    var passwordState by remember {
        mutableStateOf(TextFieldValue("••••••••"))
    }*/

    // Gestione errori di validazione
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Sfondo grigio chiaro
            .padding(16.dp)
    ) {
        // Header con pulsante indietro
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Pulsante per tornare indietro
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Torna indietro",
                    tint = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card contenitore principale con i campi
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mostra messaggio di errore se presente
                if (showError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Campo Nome Utente
                SimpleTextField(
                    label = "Nome Utente",
                    value = usernameState,
                    onValueChange = {
                        usernameState = it
                        showError = false // Nasconde errore quando modifica
                    },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Nome
                SimpleTextField(
                    label = "Name",
                    value = nameState,
                    onValueChange = {
                        nameState = it
                        showError = false
                    },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Cognome
                SimpleTextField(
                    label = "Surname",
                    value = surnameState,
                    onValueChange = {
                        surnameState = it
                        showError = false
                    },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Email
                SimpleTextField(
                    label = "Email",
                    value = emailState,
                    onValueChange = {
                        emailState = it
                        showError = false
                    },
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo Password (fittizio, non modificabile)
               /* SimpleTextField(
                    label = "Password",
                   // value = passwordState,
                    onValueChange = { /* Non fare nulla - campo non modificabile */ },
                    enabled = false // Sempre disabilitato
                )*/

                Spacer(modifier = Modifier.height(32.dp))

                // Pulsante Conferma Modifica
                Button(
                    onClick = {
                        // Validazione dei campi prima del salvataggio
                        val username = usernameState.text.trim()
                        val name = nameState.text.trim()
                        val surname = surnameState.text.trim()
                        val email = emailState.text.trim()

                        // Controllo che i campi obbligatori non siano vuoti
                        when {
                            username.isEmpty() -> {
                                errorMessage = "Il nome utente è obbligatorio"
                                showError = true
                            }
                            name.isEmpty() -> {
                                errorMessage = "Il nome è obbligatorio"
                                showError = true
                            }
                            surname.isEmpty() -> {
                                errorMessage = "Il cognome è obbligatorio"
                                showError = true
                            }
                            email.isEmpty() -> {
                                errorMessage = "L'email è obbligatoria"
                                showError = true
                            }
                            !email.contains("@") -> {
                                errorMessage = "Inserisci un'email valida"
                                showError = true
                            }
                            else -> {
                                // Validazione passata, salva i dati
                                onSave(username, name, surname, email)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3) // Blu come nell'immagine
                    ),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading // Disabilitato durante il caricamento
                ) {
                    if (isLoading) {
                        // Mostra indicatore di caricamento durante il salvataggio
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Salvando...",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            "Conferma Modifica",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Composable per i campi di testo semplici come nell'immagine
@Composable
fun SimpleTextField(
    label: String, // Etichetta del campo
    value: TextFieldValue, // Valore corrente
    onValueChange: (TextFieldValue) -> Unit, // Callback per i cambiamenti
    enabled: Boolean = true // Se il campo è abilitato
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Etichetta sopra il campo
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF424242), // Grigio scuro
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Campo di testo con stile minimale
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            placeholder = {
                Text(
                    "Value",
                    color = Color(0xFFBDBDBD) // Grigio chiaro per placeholder
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                // Colori per lo stato normale
                unfocusedBorderColor = Color(0xFFE0E0E0),
                unfocusedTextColor = Color(0xFF424242),
                // Colori per lo stato attivo/focus
                focusedBorderColor = Color(0xFF2196F3),
                focusedTextColor = Color(0xFF212121),
                // Colori per lo stato disabilitato
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledTextColor = Color(0xFF9E9E9E),
                // Colore di sfondo
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

