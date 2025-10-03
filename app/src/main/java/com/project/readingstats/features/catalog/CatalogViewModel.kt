package com.project.readingstats.features.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.readingstats.features.auth.domain.usecase.GetCurrentUidUseCase
import com.project.readingstats.features.catalog.domain.model.Book
import com.project.readingstats.features.catalog.domain.repository.UserPreferencesRepository
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
    private val getCategoryFeed: GetCategoryFeedUseCase,
    private val userPreferences: UserPreferencesRepository,
    private val getCurrentUid: GetCurrentUidUseCase
): ViewModel() {

    companion object{
        val DEFAULT_CATEGORIES = listOf(
            "Fantasy",
            "Horror",
            "Romance",
            "Thrillers",
            "Science Fiction",
            "Adventure",
            "Business & Economics",
            "History",
            "Detective and mystery stories",
            "Juvenile Fiction"
        )
    }
    //Data class for each category row state, including its books, loading state and error message
    data class CategoryRowState(
        val category: String,
        val books: List<Book> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    //Main UI state, including search query, search result and list of category rows (to add the category filter logic in the future)
    data class UiState(
        val query: String = "",
        val searching: Boolean = false,
        val searchResult: List<Book> = emptyList(),
        val allCategories: List<String> = DEFAULT_CATEGORIES,
        val categories: List<CategoryRowState> = DEFAULT_CATEGORIES.map { CategoryRowState(it) },
        val showFilters: Boolean = false,
        val selectedCategories: Set<String> = emptySet(),
        val currentUid: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val state = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = runCatching { getCurrentUid() }.getOrNull()
            _uiState.update {it.copy(currentUid = uid)}

            val saved = if (uid != null)
                runCatching { userPreferences.getSelectedCategories(uid) }.getOrDefault(emptySet())
            else emptySet()

            applyFilters(saved)
            loadAllCategories()
        }
    }

    private fun applyFilters(selected: Set<String>){
        val base = if (selected.isEmpty()) _uiState.value.allCategories else selected.toList()
        _uiState.update {
            it.copy(
                selectedCategories = selected,
                categories = base.map { category -> CategoryRowState(category) }
            )
        }
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

    fun openFilters() = _uiState.update{it.copy(showFilters = true)}
    fun closeFilters() = _uiState.update{it.copy(showFilters = false)}

    fun toggleCategory(category: String){
        _uiState.update{ state ->
            val next = state.selectedCategories.toMutableSet()
            if(!next.add(category)) next.remove(category)
            state.copy(selectedCategories = next)
        }
    }

    fun clearFilters(){
        _uiState.update{it.copy(selectedCategories = emptySet())}
    }

    fun confirmFilters(){
        val uid = _uiState.value.currentUid ?: return
        val selected = _uiState.value.selectedCategories
        viewModelScope.launch{
            runCatching { userPreferences.setSelectedCategories(uid, selected) }
            applyFilters(selected)
            _uiState.update{it.copy(showFilters = false)}
            loadAllCategories()
        }
    }
}