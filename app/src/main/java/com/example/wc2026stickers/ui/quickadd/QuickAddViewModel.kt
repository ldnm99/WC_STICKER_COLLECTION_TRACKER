package com.example.wc2026stickers.ui.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wc2026stickers.data.repository.StickerRepository
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
    val showSuccess: Boolean = false
)

@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val repository: StickerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickAddUiState())
    val uiState: StateFlow<QuickAddUiState> = _uiState.asStateFlow()

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text, showSuccess = false) }
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
        val validEntries = _uiState.value.entries.filter { it.stickerId != null }
        if (validEntries.isEmpty()) return
        viewModelScope.launch {
            validEntries.forEach { entry ->
                repository.incrementSticker(entry.stickerId!!)
            }
            _uiState.update {
                it.copy(
                    inputText = "",
                    entries = emptyList(),
                    addedCount = validEntries.size,
                    showSuccess = true
                )
            }
        }
    }

    fun dismissSuccess() {
        _uiState.update { it.copy(showSuccess = false) }
    }
}
