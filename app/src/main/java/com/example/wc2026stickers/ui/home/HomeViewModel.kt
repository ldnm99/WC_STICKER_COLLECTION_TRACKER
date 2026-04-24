package com.example.wc2026stickers.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wc2026stickers.data.repository.StickerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalCount: Int = 0,
    val collectedCount: Int = 0,
    val missingCount: Int = 0,
    val duplicatesCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: StickerRepository
) : ViewModel() {

    init {
        viewModelScope.launch { repository.ensureSeeded() }
    }

    val uiState = combine(
        repository.getTotalCount(),
        repository.getCollectedCount(),
        repository.getMissingCount(),
        repository.getDuplicatesCount()
    ) { total, collected, missing, dupes ->
        HomeUiState(total, collected, missing, dupes, isLoading = false)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}
