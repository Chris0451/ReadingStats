package com.project.readingstats.features.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.features.catalog.domain.usecase.GetCategoryFeedUseCase
import com.project.readingstats.features.catalog.domain.usecase.SearchBookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val searchBook: SearchBookUseCase,
    private val getCategoryFeed: GetCategoryFeedUseCase
): ViewModel() {

    data class CategoryRowState(
        val category: String,
        val books: List<Book> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    data class UiState(
        val query: String = "",
        val searching: Boolean = false,
        val searchResult: List<Book> = emptyList(),
        val categories: List<CategoryRowState> = listOf(
            CategoryRowState(category = "Fantasy"),
            CategoryRowState(category = "Horror"),
            CategoryRowState(category = "Romance"),
            CategoryRowState(category = "Thrillers"),
            CategoryRowState(category = "Science Fiction"),
            CategoryRowState(category = "Adventure"),
            CategoryRowState(category = "Business & Economics"),
            CategoryRowState(category = "History"),
            CategoryRowState(category = "Detective and mystery stories"),
            CategoryRowState(category = "Juvenile Fiction")
        )
    )

    private val _uiState = MutableStateFlow(UiState())
    val state = _uiState.asStateFlow()

    init {
        loadAllCategories()
    }

    private fun loadAllCategories(){
        _uiState.value.categories.forEachIndexed { index, categoryRowState ->
            viewModelScope.launch {
                setRow(index) {
                    it.copy(isLoading = true, error = null)
                }
                runCatching { getCategoryFeed(categoryRowState.category) }
                    .onSuccess { result ->
                        if(result.isEmpty()){
                            val second = getCategoryFeed(categoryRowState.category, 1)
                            setRow(index) {it.copy(books = second, isLoading = false)}
                        }else{
                            setRow(index) {
                                it.copy(books = result, isLoading = false)
                            }
                        }
                    }
                    .onFailure { exception ->
                        setRow(index) {
                            it.copy(error = exception.message, isLoading = false)
                        }
                    }
            }
        }
    }


    private fun setRow(index: Int, transform: (CategoryRowState) -> CategoryRowState) {
        _uiState.update { state ->
            val mutable = state.categories.toMutableList()
            mutable[index] = transform(mutable[index])
            state.copy(categories = mutable)
        }
    }

    fun updateQuery(q: String) = _uiState.update { it.copy(query = q) }

    fun performSearch() {
        val q = _uiState.value.query.trim()
        if(q.isBlank()) return
        viewModelScope.launch{
            _uiState.update {it.copy(searching = true)}
            runCatching { searchBook(q, page = 0) }
                .onSuccess { result -> _uiState.update { it.copy(searchResult = result, searching = false) } }
                .onFailure { exception -> _uiState.update { it.copy(searching = false) } }
        }
    }

    fun performLiveSearch(q: String) {
        if(q.isBlank()) return
        viewModelScope.launch{
            _uiState.update {it.copy(searching = true, query = q)}
            runCatching { searchBook(q, page = 0) }
                .onSuccess { result -> _uiState.update { it.copy(searchResult = result, searching = false) } }
                .onFailure { exception -> _uiState.update { it.copy(searching = false) } }
        }
    }

    fun clearSearch() = _uiState.update { it.copy(query = "", searchResult = emptyList(), searching = false) }
}