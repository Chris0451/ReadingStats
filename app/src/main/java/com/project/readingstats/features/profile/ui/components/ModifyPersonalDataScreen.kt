package com.project.readingstats.features.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.project.readingstats.features.auth.data.model.UserModelDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModificaDatiPersonali(
    user: UserModelDto?,
    onSave: (String, String, String, String) -> Unit,
    onCancel: () -> Unit,
    isLoading: Boolean = false
) {
    var usernameState by remember { mutableStateOf(TextFieldValue(user?.username ?: "")) }
    var nameState by remember { mutableStateOf(TextFieldValue(user?.name ?: "")) }
    var surnameState by remember { mutableStateOf(TextFieldValue(user?.surname ?: "")) }
    var emailState by remember { mutableStateOf(TextFieldValue(user?.email ?: "")) }

    // Stati per password
    var currentPasswordState by remember { mutableStateOf(TextFieldValue("")) }
    var newPasswordState by remember { mutableStateOf(TextFieldValue("")) }
    var confirmPasswordState by remember { mutableStateOf(TextFieldValue("")) }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var isChangingPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .imePadding()
    ) {
        // Header con pulsante indietro
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            IconButton(onClick = onCancel) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Torna indietro",
                    tint = Color.Black
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Card Dati Personali
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
                    if (showError) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    SimpleTextField(
                        label = "Nome Utente",
                        value = usernameState,
                        onValueChange = {
                            usernameState = it
                            showError = false
                        },
                        enabled = !isLoading
                    )
                    Spacer(modifier = Modifier.height(16.dp))

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

                    SimpleTextField(
                        label = "Email",
                        value = emailState,
                        onValueChange = {
                            emailState = it
                            showError = false
                        },
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val username = usernameState.text.trim()
                            val name = nameState.text.trim()
                            val surname = surnameState.text.trim()
                            val email = emailState.text.trim()

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
                                    onSave(username, name, surname, email)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
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

            Spacer(modifier = Modifier.height(16.dp))

            // Card Cambio Password
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
                        "Cambia Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    PasswordTextField(
                        label = "Password Corrente",
                        value = currentPasswordState,
                        onValueChange = {
                            currentPasswordState = it
                            showError = false
                        },
                        isVisible = showCurrentPassword,
                        onVisibilityToggle = { showCurrentPassword = !showCurrentPassword },
                        enabled = !isChangingPassword
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    PasswordTextField(
                        label = "Nuova Password",
                        value = newPasswordState,
                        onValueChange = {
                            newPasswordState = it
                            showError = false
                        },
                        isVisible = showNewPassword,
                        onVisibilityToggle = { showNewPassword = !showNewPassword },
                        enabled = !isChangingPassword
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    PasswordTextField(
                        label = "Conferma Password",
                        value = confirmPasswordState,
                        onValueChange = {
                            confirmPasswordState = it
                            showError = false
                        },
                        isVisible = showConfirmPassword,
                        onVisibilityToggle = { showConfirmPassword = !showConfirmPassword },
                        enabled = !isChangingPassword
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val currentPwd = currentPasswordState.text.trim()
                            val newPwd = newPasswordState.text.trim()
                            val confirmPwd = confirmPasswordState.text.trim()

                            when {
                                currentPwd.isEmpty() -> {
                                    errorMessage = "Inserisci la password corrente"
                                    showError = true
                                }
                                newPwd.isEmpty() -> {
                                    errorMessage = "Inserisci la nuova password"
                                    showError = true
                                }
                                newPwd.length < 6 -> {
                                    errorMessage = "La password deve avere almeno 6 caratteri"
                                    showError = true
                                }
                                newPwd != confirmPwd -> {
                                    errorMessage = "Le password non corrispondono"
                                    showError = true
                                }
                                else -> {
                                    // Cambio password
                                    isChangingPassword = true
                                    val firebaseUser = FirebaseAuth.getInstance().currentUser

                                    if (firebaseUser != null && firebaseUser.email != null) {
                                        val credential = EmailAuthProvider.getCredential(
                                            firebaseUser.email!!,
                                            currentPwd
                                        )

                                        firebaseUser.reauthenticate(credential)
                                            .addOnSuccessListener {
                                                firebaseUser.updatePassword(newPwd)
                                                    .addOnSuccessListener {
                                                        errorMessage = "Password modificata con successo"
                                                        showError = false
                                                        currentPasswordState = TextFieldValue("")
                                                        newPasswordState = TextFieldValue("")
                                                        confirmPasswordState = TextFieldValue("")
                                                        isChangingPassword = false
                                                    }
                                                    .addOnFailureListener { e ->
                                                        errorMessage = "Errore nel cambio password: ${e.message}"
                                                        showError = true
                                                        isChangingPassword = false
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = "Password corrente errata"
                                                showError = true
                                                isChangingPassword = false
                                            }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isChangingPassword
                    ) {
                        if (isChangingPassword) {
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
                                    "Cambiando...",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            Text(
                                "Cambia Password",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SimpleTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF424242),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            placeholder = {
                Text("Value", color = Color(0xFFBDBDBD))
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                unfocusedTextColor = Color(0xFF424242),
                focusedBorderColor = Color(0xFF2196F3),
                focusedTextColor = Color(0xFF212121),
                disabledBorderColor = Color(0xFFE0E0E0),
                disabledTextColor = Color(0xFF9E9E9E),
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun PasswordTextField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    enabled: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF424242),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            singleLine = true,
            visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onVisibilityToggle) {
                    Icon(
                        imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isVisible) "Nascondi password" else "Mostra password"
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedBorderColor = Color(0xFF2196F3),
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}
