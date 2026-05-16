package com.wc2026stickers.app.ui.duplicates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wc2026stickers.app.data.db.dao.StickerWithQuantity
import com.wc2026stickers.app.data.repository.StickerRepository
import com.wc2026stickers.app.ui.collection.StickerCollectionFilterState
import com.wc2026stickers.app.ui.collection.StickerCollectionSortOption
import com.wc2026stickers.app.ui.collection.buildStickerCollectionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DuplicatesViewModel @Inject constructor(
    repository: StickerRepository
) : ViewModel() {
    private val defaultFilterState = StickerCollectionFilterState()
    private val filters = MutableStateFlow(defaultFilterState)

    val uiState = combine(repository.getDuplicateStickers(), filters) { stickers, filterState ->
        buildStickerCollectionUiState(stickers, filterState)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = buildStickerCollectionUiState(emptyList(), defaultFilterState)
    )

    val sortOptions = listOf(
        StickerCollectionSortOption.TEAM_NUMBER,
        StickerCollectionSortOption.OWNED_DESC,
        StickerCollectionSortOption.LABEL,
        StickerCollectionSortOption.STICKER_TYPE
    )

    fun setFilters(filterState: StickerCollectionFilterState) {
        filters.value = filterState
    }
}

/**
 * Encodes a list of duplicate stickers as a compact QR-friendly string.
 * Format: `WC2026:ARG1x2,BRA7,FWC9x3`
 * Only includes stickers with quantity > 1 (true duplicates = qty - 1 spare copies).
 */
fun generateQrContent(stickers: List<StickerWithQuantity>): String {
    val codes = stickers
        .filter { it.quantityOwned > 1 }
        .sortedBy { it.id }
        .joinToString(",") { sticker ->
            val spares = sticker.quantityOwned - 1
            if (spares > 1) "${sticker.id}x$spares" else sticker.id
        }
    return "WC2026:$codes"
}
