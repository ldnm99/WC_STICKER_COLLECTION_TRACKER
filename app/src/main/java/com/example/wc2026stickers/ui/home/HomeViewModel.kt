package com.wc2026stickers.app.ui.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wc2026stickers.app.data.backup.CollectionBackupException
import com.wc2026stickers.app.data.backup.CollectionBackupJson
import com.wc2026stickers.app.data.backup.CollectionRestoreMode
import com.wc2026stickers.app.data.backup.CollectionRestorePreview
import com.wc2026stickers.app.data.db.dao.TeamKpiStats
import com.wc2026stickers.app.data.db.dao.TeamWithProgress
import com.wc2026stickers.app.data.repository.StickerRepository
import com.wc2026stickers.app.ui.history.CollectionHistorySummary
import com.wc2026stickers.app.ui.history.calculateCollectionHistorySummary
import com.wc2026stickers.app.ui.kpi.HomeKpiUiModel
import com.wc2026stickers.app.data.db.dao.StickerCollectionHistoryRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class HomeUiState(
    val totalCount: Int = 0,
    val collectedCount: Int = 0,
    val missingCount: Int = 0,
    val duplicatesCount: Int = 0,
    val favoriteTeams: List<FavoriteTeamHighlight> = emptyList(),
    val collectionHistory: CollectionHistorySummary = CollectionHistorySummary(),
    val kpiInsights: List<HomeKpiUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isBackupInProgress: Boolean = false,
    val pendingRestorePreview: CollectionRestorePreview? = null,
    val snackbarMessage: String? = null
)

private data class HomeBackupState(
    val isBackupInProgress: Boolean = false,
    val pendingRestorePreview: CollectionRestorePreview? = null,
    val snackbarMessage: String? = null
)

private data class CollectionSummary(
    val totalCount: Int,
    val collectedCount: Int,
    val missingCount: Int,
    val duplicatesCount: Int
)

private data class FavoriteAndKpiData(
    val favoriteTeams: List<TeamWithProgress>,
    val teamKpis: List<TeamKpiStats>
)

private data class CollectionHistoryData(
    val recentUpdates: List<StickerCollectionHistoryRecord>,
    val collectedStickers: List<StickerCollectionHistoryRecord>
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: StickerRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val backupState = MutableStateFlow(HomeBackupState())

    init {
        viewModelScope.launch { repository.ensureSeeded() }
    }

    private val collectionSummary = combine(
        repository.getTotalCount(),
        repository.getCollectedCount(),
        repository.getMissingCount(),
        repository.getDuplicatesCount()
    ) { total, collected, missing, dupes ->
        CollectionSummary(
            totalCount = total,
            collectedCount = collected,
            missingCount = missing,
            duplicatesCount = dupes
        )
    }

    private val favoriteAndKpiData = combine(
        repository.getFavoriteTeamsWithProgress(),
        repository.getAllTeamKpiStats()
    ) { favoriteTeams, teamKpis ->
        FavoriteAndKpiData(
            favoriteTeams = favoriteTeams,
            teamKpis = teamKpis
        )
    }

    private val collectionHistoryData = combine(
        repository.getRecentStickerHistory(),
        repository.getCollectedStickerHistory()
    ) { recentUpdates, collectedStickers ->
        CollectionHistoryData(
            recentUpdates = recentUpdates,
            collectedStickers = collectedStickers
        )
    }

    val uiState = combine(
        collectionSummary,
        favoriteAndKpiData,
        collectionHistoryData,
        backupState
    ) { summary, favoriteData, historyData, backup ->
        HomeUiState(
            totalCount = summary.totalCount,
            collectedCount = summary.collectedCount,
            missingCount = summary.missingCount,
            duplicatesCount = summary.duplicatesCount,
            favoriteTeams = favoriteData.favoriteTeams.toFavoriteTeamHighlights(),
            collectionHistory = calculateCollectionHistorySummary(
                totalCount = summary.totalCount,
                recentUpdates = historyData.recentUpdates,
                collectedStickers = historyData.collectedStickers
            ),
            kpiInsights = favoriteData.teamKpis.toHomeKpiUiModels(),
            isLoading = false,
            isBackupInProgress = backup.isBackupInProgress,
            pendingRestorePreview = backup.pendingRestorePreview,
            snackbarMessage = backup.snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            updateBackupState { copy(isBackupInProgress = true, snackbarMessage = null) }
            runCatching {
                val payload = repository.buildBackupPayload()
                writeBackupText(uri, CollectionBackupJson.encode(payload))
                "Backup exported with ${payload.stickers.size} collected sticker${if (payload.stickers.size == 1) "" else "s"}."
            }.onSuccess { message ->
                updateBackupState {
                    copy(isBackupInProgress = false, snackbarMessage = message)
                }
            }.onFailure { error ->
                updateBackupState {
                    copy(
                        isBackupInProgress = false,
                        snackbarMessage = error.toUserMessage("Couldn't export the backup.")
                    )
                }
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            updateBackupState { copy(isBackupInProgress = true, snackbarMessage = null) }
            runCatching {
                val payload = CollectionBackupJson.decode(readBackupText(uri))
                repository.prepareRestore(payload)
            }.onSuccess { preview ->
                updateBackupState {
                    copy(
                        isBackupInProgress = false,
                        pendingRestorePreview = preview
                    )
                }
            }.onFailure { error ->
                updateBackupState {
                    copy(
                        isBackupInProgress = false,
                        pendingRestorePreview = null,
                        snackbarMessage = error.toUserMessage("Couldn't import the backup.")
                    )
                }
            }
        }
    }

    fun dismissRestorePreview() {
        updateBackupState { copy(pendingRestorePreview = null) }
    }

    fun restoreBackup(mode: CollectionRestoreMode) {
        val preview = backupState.value.pendingRestorePreview ?: return
        viewModelScope.launch {
            updateBackupState { copy(isBackupInProgress = true, snackbarMessage = null) }
            runCatching {
                repository.restoreBackup(preview, mode)
            }.onSuccess { result ->
                val actionLabel = if (mode == CollectionRestoreMode.MERGE) "Merged" else "Restored"
                val unchangedSuffix = if (result.unchangedStickerCount > 0) {
                    " ${result.unchangedStickerCount} already had the same or higher quantity."
                } else {
                    ""
                }
                val skippedSuffix = if (result.skippedStickerCount > 0) {
                    " Skipped ${result.skippedStickerCount} unknown sticker entr${if (result.skippedStickerCount == 1) "y" else "ies"}."
                } else {
                    ""
                }
                updateBackupState {
                    copy(
                        isBackupInProgress = false,
                        pendingRestorePreview = null,
                        snackbarMessage = "$actionLabel ${result.importedStickerCount} sticker${if (result.importedStickerCount == 1) "" else "s"} from backup.$unchangedSuffix$skippedSuffix"
                    )
                }
            }.onFailure { error ->
                updateBackupState {
                    copy(
                        isBackupInProgress = false,
                        snackbarMessage = error.toUserMessage("Couldn't restore the backup.")
                    )
                }
            }
        }
    }

    fun onSnackbarShown() {
        updateBackupState { copy(snackbarMessage = null) }
    }

    private fun updateBackupState(transform: HomeBackupState.() -> HomeBackupState) {
        backupState.update { current -> current.transform() }
    }

    private suspend fun writeBackupText(uri: Uri, content: String) = withContext(Dispatchers.IO) {
        val outputStream = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw IOException("Couldn't open the selected backup file.")
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(content)
        }
    }

    private suspend fun readBackupText(uri: Uri): String = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Couldn't open the selected backup file.")
        inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readText()
        }
    }
}

private fun Throwable.toUserMessage(defaultMessage: String): String = when (this) {
    is CollectionBackupException -> message ?: defaultMessage
    is IOException -> message ?: defaultMessage
    else -> defaultMessage
}
