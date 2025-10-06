package com.project.readingstats.features.shelves.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.project.readingstats.R
import com.project.readingstats.features.shelves.ShelvesViewModel
import com.project.readingstats.features.shelves.UiShelfBook

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedShelfScreen(
    onOpenBookDetail: (UiShelfBook) -> Unit,
    viewModel: ShelvesViewModel = hiltViewModel(),
    onBack: () -> Unit
){
    val books by viewModel.books.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = viewModel.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )

        }
    ){ paddingValues ->
        if(books.isEmpty()){
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ){
                Text(text = "Nessun libro presente in lista")
            }
        }else{
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp)
            ){
                items(books.size, key = {books[it].id}){ index ->
                    ShelfBookRow(books[index]) { onOpenBookDetail(books[index]) }
                    HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
                }
            }
        }
    }
}

@Composable
private fun ShelfBookRow(book: UiShelfBook, onClick: ()->Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick=onClick)
            .padding(horizontal=16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        Column(
            Modifier
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            BookThumb(url = book.thumbnail, cd = "Copertina del libro ${book.title}")
            Text(text = book.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            val subtitle = buildList {
                if(book.authors.isNotEmpty()) add(book.authors.joinToString())
                book.pageCount?.let { add("$it pagine") }
            }.joinToString(separator = " - ")
            if (subtitle.isNotBlank()){
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
@Composable
private fun BookThumb(url: String?, cd: String) {
    val shape = RoundedCornerShape(8.dp)
    val preview = LocalInspectionMode.current
    Box(Modifier.size(64.dp).clip(shape).background(MaterialTheme.colorScheme.surfaceVariant)) {
        if (preview || url.isNullOrBlank()) {
            Image(painterResource(R.drawable.ic_book), cd, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Image(rememberAsyncImagePainter(url), cd, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
    }
}
