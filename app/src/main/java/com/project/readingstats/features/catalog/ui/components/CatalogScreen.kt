package com.project.readingstats.features.catalog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.room.util.copy
import com.project.readingstats.core.ui.components.SearchBar
import com.project.readingstats.features.catalog.CatalogViewModel
import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.core.ui.components.VerticalScrollBar

@Composable
fun CatalogScreen(
    vm: CatalogViewModel = hiltViewModel(),
    onOpenBook: (Book) -> Unit,
){
    val state by vm.state.collectAsState()

    Column(
        Modifier.fillMaxSize()
    ) {
        //SEARCH BAR
        SearchBar(
            value = state.query,
            onValueChange = vm::updateQuery,
            onSearch = vm::performSearch,
            onClear = vm::clearSearch,
            placeholder = "Cerca titolo o ISBN...",
            isLoading = state.searching,
            debounceMillis = 400L,
            onDebounceChange = {q -> vm.performLiveSearch(q)}
        )

        //LISTA FILTRI CATALOGO
        FilterBar(
            selectedCount = state.selectedCategories.size,
            onOpen = vm::openFilters
        )

        if(state.showFilters){
            FiltersDialog(
                all = state.allCategories,
                selected = state.selectedCategories,
                onToggle = vm::toggleCategory,
                onClear = vm::clearFilters,
                onConfirm = vm::confirmFilters,
                onDismiss = vm::closeFilters
            )
        }

        // BOOKS CATEGORIES OR BOOKS SEARCHED
        if(state.searchResult.isNotEmpty() || state.searching){
            if (state.searching){
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ){
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.searchResult) { book ->
                        BookCard(
                            book = book,
                            onClick = onOpenBook,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(state.categories) { row ->
                    CategoryRow(
                        title = row.category,
                        books = row.books,
                        onBookClick = onOpenBook
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterBar(
    selectedCount: Int,
    onOpen: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Text(
            text = if(selectedCount == 0) "Tutte le categorie"
                else "Selezionate $selectedCount categorie",
            style = MaterialTheme.typography.titleMedium
        )
        ElevatedButton(onClick = onOpen){
            Text("Filtri")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersDialog(
    all: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    onClear: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
){
    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ){
        //CONSTRUCTION OF THE DIALOG
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp
        ) {
            Column(
              modifier = Modifier
                  .padding(24.dp)
                  .widthIn(min = 320.dp, max = 560.dp)
            ){
                Text(
                    text = "Filtra per categoria",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.padding(top=12.dp))

                val listState = rememberLazyListState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                ){
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterStart), // limita altezza per attivare lo scroll
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = PaddingValues(bottom = 2.dp)
                    ) {
                        items(
                            items = all,
                            key = { it } // chiave stabile = nome categoria
                        ) { category ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = category in selected,
                                    onCheckedChange = { onToggle(category) }
                                )
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    VerticalScrollBar(
                        listState = listState,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(start = 4.dp)
                    )
                }


                Spacer(Modifier.padding(top=16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ){
                    TextButton(onClick = onClear){
                        Text(text="Pulisci")
                    }
                    TextButton(onClick = onDismiss) {
                        Text(text="Annulla")
                    }
                    Button(
                        onClick = onConfirm,
                        contentPadding = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
                    ) {
                        Text(text="Conferma", maxLines = 1, softWrap = false)
                    }
                }
            }
        }
    }
}