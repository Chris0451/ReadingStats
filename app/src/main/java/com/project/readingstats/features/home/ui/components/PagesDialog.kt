package com.project.readingstats.features.home.ui.components

import com.project.readingstats.features.home.domain.model.UiHomeBook
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagesDialog(
    book: UiHomeBook,
    initial: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var value by remember(book.id) { mutableStateOf(initial.toString()) }
    var error by remember { mutableStateOf<String?>(null) }
    val max = book.pageCount

    fun validate(): Boolean {
        val n = value.toIntOrNull()
        error = when {
            value.isBlank() -> "Inserisci un numero"
            n == null -> "Valore non valido"
            n < 0 -> "Deve essere ≥ 0"
            (max != null && n > max) -> "Non può superare $max"
            else -> null
        }
        return error == null
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.extraLarge, tonalElevation = 6.dp) {
            Column(Modifier.padding(20.dp).widthIn(min = 320.dp)) {
                Text(
                    text = if (max != null) "Pagine lette (0..$max)" else "Pagine lette",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = value,
                    onValueChange = { s ->
                        value = s.filter { it.isDigit() }
                        if (error != null) validate()
                    },
                    singleLine = true,
                    label = { Text("Pagine lette totali") },
                    isError = error != null,
                    supportingText = { error?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Annulla") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { if (validate()) onConfirm(value.toInt()) }) { Text("Salva") }
                }
            }
        }
    }
}
