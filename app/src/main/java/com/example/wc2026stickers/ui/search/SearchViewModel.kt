package com.wc2026stickers.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wc2026stickers.app.data.repository.StickerRepository
import com.wc2026stickers.app.ui.collection.StickerCollectionFilterState
import com.wc2026stickers.app.ui.collection.StickerCollectionSortOption
import com.wc2026stickers.app.ui.collection.buildStickerCollectionUiState
import com.wc2026stickers.app.ui.collection.normalizeStickerSearchInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: StickerRepository
) : ViewModel() {
    private val defaultFilterState = StickerCollectionFilterState()
    private val filters = MutableStateFlow(defaultFilterState)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val uiState = combine(
        _searchQuery
        .debounce(200)
        .flatMapLatest { query ->
            val normalizedQuery = normalizeStickerSearchInput(query)
            if (normalizedQuery.length < 2) {
                flowOf(emptyList())
            } else {
                repository.searchStickers("%$normalizedQuery%")
            }
        },
        filters
    ) { stickers, filterState ->
        buildStickerCollectionUiState(stickers, filterState)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        buildStickerCollectionUiState(emptyList(), defaultFilterState)
    )

    val sortOptions = listOf(
        StickerCollectionSortOption.TEAM_NUMBER,
        StickerCollectionSortOption.OWNED_DESC,
        StickerCollectionSortOption.LABEL,
        StickerCollectionSortOption.STICKER_TYPE
    )

    fun onQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setFilters(filterState: StickerCollectionFilterState) {
        filters.value = filterState
    }
}
