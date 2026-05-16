package com.wc2026stickers.app.ui.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wc2026stickers.app.data.db.dao.TeamWithProgress
import com.wc2026stickers.app.data.repository.StickerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamsViewModel @Inject constructor(
    private val repository: StickerRepository
) : ViewModel() {

    val teams = repository.getAllTeamsWithProgress()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun setFavorite(teamCode: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.setTeamFavorite(teamCode, isFavorite)
        }
    }
}
