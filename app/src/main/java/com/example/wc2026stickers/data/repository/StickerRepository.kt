package com.wc2026stickers.app.data.repository

import androidx.room.withTransaction
import com.wc2026stickers.app.data.StickerCodeParser
import com.wc2026stickers.app.data.backup.BackupStickerQuantity
import com.wc2026stickers.app.data.backup.CollectionBackupException
import com.wc2026stickers.app.data.backup.CollectionBackupJson
import com.wc2026stickers.app.data.backup.CollectionBackupPayload
import com.wc2026stickers.app.data.backup.CollectionRestoreMode
import com.wc2026stickers.app.data.backup.CollectionRestorePreview
import com.wc2026stickers.app.data.backup.CollectionRestoreResult
import com.wc2026stickers.app.data.db.AppDatabase
import com.wc2026stickers.app.data.db.dao.StickerDao
import com.wc2026stickers.app.data.db.dao.StickerCollectionHistoryRecord
import com.wc2026stickers.app.data.db.dao.StickerWithQuantity
import com.wc2026stickers.app.data.db.dao.TeamDao
import com.wc2026stickers.app.data.db.dao.TeamKpiStats
import com.wc2026stickers.app.data.db.dao.TeamWithProgress
import com.wc2026stickers.app.data.db.entities.Team
import com.wc2026stickers.app.data.db.entities.UserSticker
import com.wc2026stickers.app.data.seed.DatabaseSeeder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StickerRepository @Inject constructor(
    private val database: AppDatabase,
    private val teamDao: TeamDao,
    private val stickerDao: StickerDao,
    private val seeder: DatabaseSeeder
) {
    suspend fun ensureSeeded() = seeder.seedIfNeeded()

    fun getAllTeamsWithProgress(): Flow<List<TeamWithProgress>> =
        teamDao.getAllTeamsWithProgress()

    fun getFavoriteTeamsWithProgress(): Flow<List<TeamWithProgress>> =
        teamDao.getFavoriteTeamsWithProgress()

    fun getAllTeamKpiStats(): Flow<List<TeamKpiStats>> =
        teamDao.getAllTeamKpiStats()

    fun getStickersForTeam(teamCode: String): Flow<List<StickerWithQuantity>> =
        stickerDao.getStickersForTeam(teamCode)

    fun getMissingStickers(): Flow<List<StickerWithQuantity>> =
        stickerDao.getMissingStickers()

    fun getDuplicateStickers(): Flow<List<StickerWithQuantity>> =
        stickerDao.getDuplicateStickers()

    fun getCollectedCount(): Flow<Int> = stickerDao.getCollectedCount()
    fun getMissingCount(): Flow<Int> = stickerDao.getMissingCount()
    fun getDuplicatesCount(): Flow<Int> = stickerDao.getDuplicatesCount()
    fun getTotalCount(): Flow<Int> = stickerDao.getTotalCount()
    fun getRecentStickerHistory(limit: Int = 6): Flow<List<StickerCollectionHistoryRecord>> =
        stickerDao.getRecentStickerHistory(limit)

    fun getCollectedStickerHistory(): Flow<List<StickerCollectionHistoryRecord>> =
        stickerDao.getCollectedStickerHistory()

    suspend fun setQuantity(stickerId: String, quantity: Int) {
        val normalizedQuantity = quantity.coerceAtLeast(0)
        val current = stickerDao.getUserSticker(stickerId)
        if ((current?.quantityOwned ?: 0) == normalizedQuantity) return

        val now = System.currentTimeMillis()
        val firstCollectedAt = when {
            normalizedQuantity > 0 -> current?.firstCollectedAt ?: now
            else -> current?.firstCollectedAt
        }
        stickerDao.upsertUserSticker(
            UserSticker(
                stickerId = stickerId,
                quantityOwned = normalizedQuantity,
                firstCollectedAt = firstCollectedAt,
                lastUpdatedAt = now
            )
        )
    }

    suspend fun getTeamByCode(code: String) = teamDao.getTeamByCode(code)

    fun observeTeamByCode(code: String): Flow<Team?> =
        teamDao.observeTeamByCode(code)

    suspend fun setTeamFavorite(code: String, isFavorite: Boolean) {
        teamDao.setTeamFavorite(code, isFavorite)
    }

    fun searchStickers(query: String): Flow<List<StickerWithQuantity>> =
        stickerDao.searchStickers(query)

    /**
     * Increment a sticker's quantity by 1 (used by Quick Add).
     */
    suspend fun incrementSticker(stickerId: String, amount: Int = 1) {
        if (amount <= 0) return
        val current = stickerDao.getUserSticker(stickerId)
        val now = System.currentTimeMillis()
        stickerDao.upsertUserSticker(
            UserSticker(
                stickerId = stickerId,
                quantityOwned = (current?.quantityOwned ?: 0) + amount,
                firstCollectedAt = current?.firstCollectedAt ?: now,
                lastUpdatedAt = now
            )
        )
    }

    /**
     * Parse a user-typed sticker ID like "ARG1", "MEX20", "FWC9".
     * Returns the canonical sticker id (e.g. "ARG-1") or null if not found.
     */
    suspend fun resolveStickerId(input: String): String? {
        val stickerCode = StickerCodeParser.parse(input) ?: return null
        return stickerDao.findSticker(stickerCode.teamCode, stickerCode.number)?.id
    }

    suspend fun getOwnedQuantity(stickerId: String): Int =
        stickerDao.getUserSticker(stickerId)?.quantityOwned ?: 0

    suspend fun getTeamKpiStatsSnapshot(): List<TeamKpiStats> =
        teamDao.getAllTeamKpiStats().first()

    suspend fun buildBackupPayload(): CollectionBackupPayload {
        ensureSeeded()
        return CollectionBackupJson.createPayload(
            stickers = stickerDao.getOwnedUserStickers().map { sticker ->
                BackupStickerQuantity(
                    stickerId = sticker.stickerId,
                    quantityOwned = sticker.quantityOwned
                )
            }
        )
    }

    suspend fun prepareRestore(backup: CollectionBackupPayload): CollectionRestorePreview {
        ensureSeeded()
        val knownStickerIds = stickerDao.getAllStickerIds().toHashSet()
        val validEntries = backup.stickers
            .filter { it.quantityOwned > 0 && it.stickerId in knownStickerIds }
            .sortedBy { it.stickerId }
        val skippedStickerIds = backup.stickers
            .filter { it.quantityOwned > 0 && it.stickerId !in knownStickerIds }
            .map { it.stickerId }
            .sorted()

        if (backup.stickers.isNotEmpty() && validEntries.isEmpty() && skippedStickerIds.isNotEmpty()) {
            throw CollectionBackupException("Backup doesn't contain any stickers from this album version.")
        }

        return CollectionRestorePreview(
            backup = backup,
            validEntries = validEntries,
            skippedStickerIds = skippedStickerIds
        )
    }

    suspend fun restoreBackup(
        preview: CollectionRestorePreview,
        mode: CollectionRestoreMode
    ): CollectionRestoreResult {
        ensureSeeded()
        return database.withTransaction {
            when (mode) {
                CollectionRestoreMode.MERGE -> {
                    val updates = mutableListOf<UserSticker>()
                    var unchangedCount = 0

                    preview.validEntries.forEach { entry ->
                        val currentSticker = stickerDao.getUserSticker(entry.stickerId)
                        val currentQuantity = currentSticker?.quantityOwned ?: 0
                        if (entry.quantityOwned > currentQuantity) {
                            val now = System.currentTimeMillis()
                            updates += UserSticker(
                                stickerId = entry.stickerId,
                                quantityOwned = entry.quantityOwned,
                                firstCollectedAt = currentSticker?.firstCollectedAt ?: now,
                                lastUpdatedAt = now
                            )
                        } else {
                            unchangedCount += 1
                        }
                    }

                    if (updates.isNotEmpty()) {
                        stickerDao.upsertUserStickers(updates)
                    }

                    CollectionRestoreResult(
                        mode = mode,
                        importedStickerCount = updates.size,
                        unchangedStickerCount = unchangedCount,
                        skippedStickerCount = preview.skippedStickerIds.size
                    )
                }

                CollectionRestoreMode.REPLACE -> {
                    stickerDao.clearUserStickers()
                    if (preview.validEntries.isNotEmpty()) {
                        stickerDao.upsertUserStickers(
                            preview.validEntries.map { entry ->
                                val now = System.currentTimeMillis()
                                UserSticker(
                                    stickerId = entry.stickerId,
                                    quantityOwned = entry.quantityOwned,
                                    firstCollectedAt = now,
                                    lastUpdatedAt = now
                                )
                            }
                        )
                    }

                    CollectionRestoreResult(
                        mode = mode,
                        importedStickerCount = preview.validEntries.size,
                        unchangedStickerCount = 0,
                        skippedStickerCount = preview.skippedStickerIds.size
                    )
                }
            }
        }
    }
}
