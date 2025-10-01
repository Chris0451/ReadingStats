package com.project.readingstats.features.catalog.data.repository

import com.project.readingstats.features.catalog.data.GoogleBooksApi
import com.project.readingstats.features.catalog.data.dto.VolumeItem
import com.project.readingstats.features.catalog.domain.repository.CatalogRepository
import com.project.readingstats.features.catalog.domain.model.Book
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val api: GoogleBooksApi
): CatalogRepository {

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
            projection = "lite"
        )
        return resp.items.map(::toDomain)
    }

    override suspend fun byCategory(category: String, page: Int, pageSize: Int): List<Book> {
        val itSubject = "subject:$category"
        val enSubject = itToEn[category]?.let { "subject:$it" }

        var items = api.search(
            query = itSubject,
            startIndex = page*pageSize,
            maxResults = pageSize,
            orderBy = "relevance",
            projection = "lite"
        ).items

        if(items.isEmpty() && enSubject != null){
            items = api.search(
                query = enSubject,
                startIndex = page*pageSize,
                maxResults = pageSize,
                orderBy = "relevance",
                projection = "lite"
            ).items
        }

        if(items.isEmpty()){
            items = api.search(
                query = "subject:$category",
                startIndex = page*pageSize,
                maxResults = pageSize,
                orderBy = "relevance",
                projection = "lite"
            ).items
        }

        return items.map(::toDomain)
    }

    private fun toDomain(it: VolumeItem) = Book(
        id = it.id,
        title = it.volumeInfo?.title.orEmpty(),
        authors = it.volumeInfo?.authors ?: emptyList(),
        categories = it.volumeInfo?.categories ?: emptyList(),
        publishedDate = it.volumeInfo?.publishedDate,
        pageCount = it.volumeInfo?.pageCount,
        description = it.volumeInfo?.description,
        thumbnail = it.volumeInfo?.imageLinks?.thumbnail,
    )

    private fun normalizeQuery(raw: String): String{
        val t = raw.trim()
        return if (t.any(Char::isDigit)){
            "isbn:${t.filter { it.isLetterOrDigit()}} OR $t" //Partial ISBN
        } else t
    }
}