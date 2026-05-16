package com.wc2026stickers.app.ui.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wc2026stickers.app.data.repository.StickerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ParsedEntry(
    val raw: String,
    val stickerId: String?,  // null = invalid
    val isAdded: Boolean = false
)

data class QuickAddUiState(
    val inputText: String = "",
    val entries: List<ParsedEntry> = emptyList(),
    val addedCount: Int = 0,
    val sessionSummary: QuickAddSessionSummary? = null
)

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val repository: StickerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickAddUiState())
    val uiState: StateFlow<QuickAddUiState> = _uiState.asStateFlow()

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text, addedCount = 0, sessionSummary = null) }
        viewModelScope.launch { parseInput(text) }
    }

    private suspend fun parseInput(text: String) {
        val tokens = text.trim().split(Regex("[,\\s]+")).filter { it.isNotBlank() }
        val entries = tokens.map { token ->
            val id = repository.resolveStickerId(token)
            ParsedEntry(raw = token, stickerId = id)
        }
        _uiState.update { it.copy(entries = entries) }
    }

    fun addAll() {
        val groupedStickerIds = _uiState.value.entries
            .mapNotNull { it.stickerId }
            .groupingBy { it }
            .eachCount()
        if (groupedStickerIds.isEmpty()) return
        viewModelScope.launch {
            val teamStatsBefore = repository.getTeamKpiStatsSnapshot()
            val stickerSnapshots = groupedStickerIds.map { (stickerId, quantityAdded) ->
                QuickAddStickerSnapshot(
                    stickerId = stickerId,
                    quantityBefore = repository.getOwnedQuantity(stickerId),
                    quantityAdded = quantityAdded
                )
            }
            groupedStickerIds.forEach { (stickerId, quantityAdded) ->
                repository.incrementSticker(stickerId, quantityAdded)
            }
            val teamStatsAfter = repository.getTeamKpiStatsSnapshot()
            val sessionSummary = calculateQuickAddSessionSummary(
                stickerSnapshots = stickerSnapshots,
                teamStatsBefore = teamStatsBefore,
                teamStatsAfter = teamStatsAfter
            )
            _uiState.update {
                it.copy(
                    inputText = "",
                    entries = emptyList(),
                    addedCount = groupedStickerIds.values.sum(),
                    sessionSummary = sessionSummary
                )
            }
        }
    }
}
