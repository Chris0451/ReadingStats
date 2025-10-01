package com.project.readingstats.features.catalog.ui.components

import android.R.attr.bottom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.project.readingstats.core.ui.components.SearchBar
import com.project.readingstats.features.catalog.CatalogViewModel
import com.project.readingstats.features.catalog.domain.model.Book

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