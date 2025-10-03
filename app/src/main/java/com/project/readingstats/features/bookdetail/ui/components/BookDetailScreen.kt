package com.project.readingstats.features.bookdetail.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.project.readingstats.R
import com.project.readingstats.features.catalog.domain.model.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    book: Book,
    onBack: () -> Unit
){
    val categoriesText = remember(book.categories) {
        book.categories
            .map{it.trim()}
            .filter{it.isNotEmpty()}
            .distinct()
            .joinToString(" - ")
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = book.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // HEADER: cover + info principali
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(book.thumbnail)
                        .crossfade(true)
                        .listener(onError = { _, result ->
                            Log.e("BookDetail", "Image load error for ${book.title}", result.throwable)
                        })
                        .build(),
                    contentDescription = book.title,
                    modifier = Modifier
                        .size(width = 140.dp, height = 200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_book)
                )

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (book.authors.isNotEmpty()) {
                        Text(
                            text = book.authors.joinToString(),
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!book.publishedDate.isNullOrBlank()) {
                        Text(
                            text = book.publishedDate ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (categoriesText.isNotBlank()) {
                        Text(
                            text = categoriesText,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Descrizione espandibile
            ExpandableText(
                text = book.description ?: "Nessuna descrizione disponibile.",
            )
        }
    }
}

@Composable
private fun ExpandableText(
    text: String,
    collapsedLines: Int = 5
) {
    var expanded by remember { mutableStateOf(false) }
    var isOverflowing by remember (text, collapsedLines) { mutableStateOf(false) }
    Text(
        text = text,
        maxLines = if (expanded) Int.MAX_VALUE else collapsedLines,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyMedium,
        onTextLayout = { result ->
            if(!expanded) isOverflowing = result.hasVisualOverflow
        }
    )
    if(isOverflowing){
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Mostra meno" else "Mostra tutto")
        }
    }
}