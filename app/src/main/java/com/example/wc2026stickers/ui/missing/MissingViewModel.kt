package com.example.wc2026stickers.ui.missing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wc2026stickers.data.db.dao.StickerWithQuantity
import com.example.wc2026stickers.data.repository.StickerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MissingViewModel @Inject constructor(
    repository: StickerRepository
) : ViewModel() {

    val missingStickers = repository.getMissingStickers()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}
