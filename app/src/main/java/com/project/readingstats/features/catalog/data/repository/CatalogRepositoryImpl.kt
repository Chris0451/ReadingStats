package com.project.readingstats.features.catalog.data.repository

import com.project.readingstats.features.catalog.data.GoogleBooksApi
import com.project.readingstats.features.catalog.data.dto.VolumeInfo
import com.project.readingstats.features.catalog.data.dto.VolumeItem
import com.project.readingstats.features.catalog.domain.repository.CatalogRepository
import com.project.readingstats.features.catalog.domain.model.Book
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val api: GoogleBooksApi
): CatalogRepository {

    private fun VolumeInfo.extractIsbns(): Pair<String?, String?> {
        val ids = industryIdentifiers.orEmpty()
        val isbn13 = ids.firstOrNull { it.type.equals("ISBN_13", true) }
            ?.identifier
            ?.filter(Char::isDigit)
            ?.takeIf { it.length == 13 }
        val isbn10 = ids.firstOrNull { it.type.equals("ISBN_10", true) }
            ?.identifier
            ?.replace("-", "")
            ?.uppercase()
            ?.takeIf { it.length == 10 && it.all { ch -> ch.isDigit() || ch == 'X' } }
        return isbn13 to isbn10
    }

    private fun VolumeItem.toDomain() = Book(
        id = id,
        title = volumeInfo?.title.orEmpty(),
        authors = volumeInfo?.authors ?: emptyList(),
        categories = volumeInfo?.categories ?: emptyList(),
        publishedDate = volumeInfo?.publishedDate,
        pageCount = volumeInfo?.pageCount,
        description = volumeInfo?.description,
        thumbnail = bestThumbnailHttps(volumeInfo),
        isbn13 = volumeInfo?.extractIsbns()?.first,
        isbn10 = volumeInfo?.extractIsbns()?.second
    )

    private fun bestThumbnailHttps(info: VolumeInfo?): String?{
        val raw = info?.imageLinks?.thumbnail
            ?: info?.imageLinks?.smallThumbnail
            ?: return null
        return raw.replace("http://", "https://")
    }

    private val itToEn = mapOf(
        "Avventura" to "Adventure",
        "Thriller"  to "Thrillers",
        "Romantico" to "Romance",
        "Horror"    to "Horror",
        "Economia"  to "Business & Economics",
        "Fantascienza" to "Science Fiction",
        "Storico"      to "History",
        "Giallo"       to "Detective and mystery stories",
        "Bambini"      to "Juvenile Fiction"
    )

    override suspend fun search(
        query: String,
        page: Int,
        pageSize: Int
    ): List<Book> {
        val resp = api.search(
            query = normalizeQuery(query),
            startIndex = page*pageSize,
            maxResults = pageSize,
            orderBy = "relevance",
            projection = "full"
        )
        return resp.items.map{it.toDomain()}
    }

    override suspend fun byCategory(category: String, page: Int, pageSize: Int): List<Book> {
        val itSubject = "subject:$category"
        val enSubject = itToEn[category]?.let { "subject:$it" }

        var items = api.search(
            query = itSubject,
            startIndex = page*pageSize,
            maxResults = pageSize,
            orderBy = "relevance",
            projection = "full"
        ).items

        if(items.isEmpty() && enSubject != null){
            items = api.search(
                query = enSubject,
                startIndex = page*pageSize,
                maxResults = pageSize,
                orderBy = "relevance",
                projection = "full"
            ).items
        }

        if(items.isEmpty()){
            items = api.search(
                query = "subject:$category",
                startIndex = page*pageSize,
                maxResults = pageSize,
                orderBy = "relevance",
                projection = "full"
            ).items
        }

        android.util.Log.d("CatalogRepository", "byCategory: $category items: ${items.size}")
        return items.map { it.toDomain() }
    }

    private val isbnQueryPattern = Regex("^\\s*isbn:\\s*[0-9Xx-]+\\s*$")
    private fun normalizeQuery(raw: String): String{
        val t = raw.trim()

        if (isbnQueryPattern.matches(t)) return t

        // Interpreta ISBN solo quando ha 10 o 13 cifre
        val digits = t.filter(Char::isDigit)
        return when {
            // ISBN-13
            digits.length == 13 && (digits.startsWith("978") || digits.startsWith("979")) ->
                "isbn:$digits OR $t"

            // ISBN-10
            digits.length == 10 ->
                "isbn:$digits OR $t"

            else -> t
        }
    }
}