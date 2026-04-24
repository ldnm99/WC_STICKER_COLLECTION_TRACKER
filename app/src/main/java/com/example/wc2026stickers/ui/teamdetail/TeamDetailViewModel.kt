package com.example.wc2026stickers.ui.teamdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wc2026stickers.data.db.dao.StickerWithQuantity
import com.example.wc2026stickers.data.repository.StickerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamDetailViewModel @Inject constructor(
    private val repository: StickerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val teamCode: String = checkNotNull(savedStateHandle["teamCode"])

    private val _teamName = MutableStateFlow(teamCode)
    val teamName: StateFlow<String> = _teamName.asStateFlow()

    init {
        viewModelScope.launch {
            val team = repository.getTeamByCode(teamCode)
            if (team != null) {
                _teamName.value = "${team.flagEmoji} ${team.name}"
            }
        }
    }

    val stickers = repository.getStickersForTeam(teamCode)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setQuantity(stickerId: String, quantity: Int) {
        viewModelScope.launch { repository.setQuantity(stickerId, quantity) }
    }
}
