package com.project.readingstats.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: (() -> Unit)? = null,
    placeholder: String = "Cerca...",
    placeholderContent: (@Composable (() -> Unit))? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable (() -> Unit))? = null,
    trailingExtra: (@Composable (() -> Unit))? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        autoCorrectEnabled = false,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Search
    ),
    keyboardActions: KeyboardActions? = null,
    debounceMillis: Long = 0L,
    onDebounceChange: ((String) -> Unit)? = null,
    textFieldColors: TextFieldColors = TextFieldDefaults.colors(),
    testTag: String = "SearchBar"
){
    val scope = rememberCoroutineScope()
    var debounceJob by remember { mutableStateOf<Job?>(null) }

    fun handleDebounce(newText: String){
        if(debounceMillis <= 0L || onDebounceChange == null) return
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(debounceMillis)
            onDebounceChange(newText)
        }
    }

    TextField(
        value = value,
        onValueChange = {
            handleDebounce(it)
            onValueChange(it)
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clip(RoundedCornerShape(20.dp))
            .testTag(testTag),
        enabled = enabled,
        singleLine = true,
        placeholder = {
            when{
                placeholderContent != null -> placeholderContent()
                else -> Text(text = placeholder)
            }
        },
        leadingIcon = leadingIcon,
        trailingIcon = {
            Row {
                trailingExtra?.invoke()
                if(isLoading){
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                }
                if(value.isNotBlank()){
                    IconButton(onClick = {
                        onClear?.invoke() ?: onValueChange("")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Pulisci"
                        )
                    }
                }
                IconButton(onClick = onSearch){
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cerca"
                    )
                }
            }
        },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions ?: KeyboardActions(onSearch = { onSearch() }),
        colors = textFieldColors
    )
}