package com.project.readingstats.features.bookdetail.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.project.readingstats.features.catalog.domain.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: Book,
    onBack: () -> Unit
){
    Scaffold(
        topBar = {TopAppBar(
            title = { Text(book.title, maxLines = 1, overflow = TextOverflow.Ellipsis)},
            navigationIcon = {
                    IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack,null)
                }
            }
        )}
    ){ padding ->
        Column(
            Modifier
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
        ){
            Row(Modifier
                .fillMaxWidth()
            ){
                AsyncImage(
                    model = book.thumbnail,
                    contentDescription = book.title,
                    modifier = Modifier.size(140.dp, 200.dp).clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.size(16.dp))
                Column(Modifier.weight(1f).align(Alignment.CenterVertically)){
                    Text(book.title, style = MaterialTheme.typography.titleLarge)
                    if (book.authors.isNotEmpty()) Text(book.authors.joinToString())
                    Text(book.publishedDate.toString())
                    if (book.categories.isNotEmpty()) Text(book.categories.joinToString())
                }
                Spacer(Modifier.height(16.dp))
                Text(book.description.toString())
            }
        }

    }
}