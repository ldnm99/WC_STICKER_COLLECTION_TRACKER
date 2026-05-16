package com.wc2026stickers.app.ui.friendmatcher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wc2026stickers.app.data.repository.StickerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

data class FriendMatcherUiState(
    val inputText: String = "",
    val result: FriendListMatcherResult = FriendListMatcherResult()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FriendMatcherViewModel @Inject constructor(
    repository: StickerRepository
) : ViewModel() {
    private val inputText = MutableStateFlow("")

    val uiState = combine(repository.getMissingStickers(), inputText) { missingStickers, rawInput ->
        missingStickers to rawInput
    }.mapLatest { (missingStickers, rawInput) ->
        FriendMatcherUiState(
            inputText = rawInput,
            result = buildFriendListMatcherResult(
                input = rawInput,
                missingStickers = missingStickers,
                resolveStickerId = repository::resolveStickerId
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = FriendMatcherUiState()
    )

    fun onInputChanged(value: String) {
        inputText.value = value
    }
}
