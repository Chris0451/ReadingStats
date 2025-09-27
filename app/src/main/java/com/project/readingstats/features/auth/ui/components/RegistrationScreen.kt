package com.project.readingstats.features.auth.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
/*
* Codice per la schermata (View) di registrazione.
 */
@Composable
fun RegistrationScreen(
    viewModel: AuthViewModel,
    onRegistered: () -> Unit,
    onLoginClick: () -> Unit
) {
    val ui by viewModel.uiState.collectAsState()

    if (ui.success) {
        onRegistered()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Registrazione utente", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = ui.name, onValueChange = viewModel::onNameChange,
            label = { Text("Nome") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = ui.surname, onValueChange = viewModel::onSurnameChange,
            label = { Text("Cognome") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = ui.username, onValueChange = viewModel::onUsernameChange,
            label = { Text("Username") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            supportingText = {
                when(ui.usernameAvailable){
                    true -> Text("Username disponibile", color = MaterialTheme.colorScheme.primary)
                    false -> Text("Username non disponibile", color = MaterialTheme.colorScheme.error)
                    else -> {}
                }
            }
        )

        OutlinedTextField(
            value = ui.email, onValueChange = viewModel::onEmailChange,
            label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = ui.password, onValueChange = viewModel::onPasswordChange,
            label = { Text("Password (min. 6 caratteri)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        OutlinedTextField(
            value = ui.confirmPassword, onValueChange = viewModel::onConfirmPasswordChange,
            label = { Text("Conferma password") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        if(ui.error != null){
            Text(ui.error!!, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = viewModel::submit,
            modifier = Modifier.fillMaxWidth(),
            enabled = ui.canSubmit && !ui.isSubmitting
        ) {
            if (ui.isSubmitting) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Registrati")
        }

        TextButton(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        ){
            Text("Hai già un account? Accedi")
        }
    }
}