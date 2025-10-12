package com.project.readingstats.features.bookdetail.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TotalPagesDialog(
    value: String,
    onValue: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isError: Boolean,
    supportingText: String?
) {
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 320.dp, max = 560.dp)
            ) {
                Text(
                    text = "Inserisci pagine totali",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = { s -> onValue(s.filter { it.isDigit() }) },
                    singleLine = true,
                    label = { Text("Pagine totali") },
                    isError = isError,
                    supportingText = { if (supportingText != null) Text(supportingText) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Annulla") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = value.isNotBlank() && !isError
                    ) { Text("Salva") }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadPagesDialog(
    value: String,
    max: Int?,
    previousRead :Int?,
    onValue: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isError: Boolean,
    supportingText: String?
) {
    val typed = value.toIntOrNull()
    val display = typed ?: previousRead

    val title = when{
        max != null && display != null -> "Pagine lette ($display/$max)"
        max != null                   -> "Pagine lette (1..$max)"
        else                          -> "Pagine lette"
    }
    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {

            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 320.dp, max = 560.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = value,
                    onValueChange = { s -> onValue(s.filter { it.isDigit() }) },
                    singleLine = true,
                    label = { Text("Pagine lette") },
                    isError = isError,
                    supportingText = { if (supportingText != null) Text(supportingText) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Annulla") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        enabled = value.isNotBlank() && !isError
                    ) { Text("Salva") }
                }
            }
        }
    }
}