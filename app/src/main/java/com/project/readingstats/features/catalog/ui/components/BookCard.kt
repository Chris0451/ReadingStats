package com.project.readingstats.features.catalog.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.project.readingstats.features.catalog.domain.model.Book
import coil.compose.AsyncImage

@Composable
fun BookCard(
    book: Book,
    onClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
    titleMaxLines: Int = 2
) {
    Column(
        modifier
            .width(120.dp)
            .clickable{ onClick(book) }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = book.thumbnail,
            contentDescription = book.title,
            modifier = Modifier
                .size(width = 110.dp, height = 170.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = book.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = titleMaxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}