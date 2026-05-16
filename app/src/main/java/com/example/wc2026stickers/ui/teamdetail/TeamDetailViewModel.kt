package com.wc2026stickers.app.ui.teamdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wc2026stickers.app.data.db.dao.StickerWithQuantity
import com.wc2026stickers.app.data.db.entities.Team
import com.wc2026stickers.app.data.repository.StickerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamDetailViewModel @Inject constructor(
    private val repository: StickerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val teamCode: String = checkNotNull(savedStateHandle["teamCode"])

    val team: StateFlow<Team?> = repository.observeTeamByCode(teamCode)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val teamName: StateFlow<String> = team
        .map { currentTeam -> currentTeam?.let { "${it.flagEmoji} ${it.name}" } ?: teamCode }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = teamCode
        )

    val stickers = repository.getStickersForTeam(teamCode)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setQuantity(stickerId: String, quantity: Int) {
        viewModelScope.launch { repository.setQuantity(stickerId, quantity) }
    }

    fun toggleFavorite() {
        val currentTeam = team.value ?: return
        viewModelScope.launch {
            repository.setTeamFavorite(currentTeam.code, !currentTeam.isFavorite)
        }
    }
}
